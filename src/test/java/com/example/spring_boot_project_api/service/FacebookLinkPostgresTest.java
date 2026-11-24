package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.WebApplicationContext;

import com.example.spring_boot_project_api.DisposablePostgresTest;
import com.example.spring_boot_project_api.repository.SocialAccountRepository;
import com.example.spring_boot_project_api.security.JwtService;
import com.example.spring_boot_project_api.security.TokenBlacklistService;

import tools.jackson.databind.ObjectMapper;

@EnabledIfEnvironmentVariable(named = "TOURISM_LINK_TEST_DB", matches = ".+")
@SpringBootTest(properties = {
        "social.facebook.app-id=123",
        "social.facebook.app-secret=mock-secret",
        "social.facebook.graph-version=v24.0"
})
class FacebookLinkPostgresTest extends DisposablePostgresTest {

    private static final String CONNECTIONS = "/api/auth/social/connections";
    private static final String FACEBOOK = "/api/auth/social/facebook";
    private static final String LINK = FACEBOOK + "/link";

    @Autowired private WebApplicationContext context;
    @Autowired private AuthService authService;
    @Autowired private SocialAccountRepository accounts;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper mapper;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private TokenBlacklistService blacklist;

    private MockMvc mvc;
    private String facebookId;

    @BeforeEach
    void configureMockMeta() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        facebookId = Long.toUnsignedString(UUID.randomUUID().getLeastSignificantBits());
        RestClient client = RestClient.builder().requestInterceptor((request, bytes, execution) -> {
            String body = request.getURI().getPath().endsWith("/debug_token")
                    ? """
                      {"data":{"is_valid":true,"app_id":"123","user_id":"%s"}}
                      """.formatted(facebookId)
                    : """
                      {"id":"%s","name":"Facebook Test User"}
                      """.formatted(facebookId);
            var response = new MockClientHttpResponse(body.getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response;
        }).build();
        ReflectionTestUtils.setField(AopTestUtils.getUltimateTargetObject(authService), "socialRestClient", client);
    }

    @Test
    void databaseHasUniqueProviderIdentityConstraint() {
        assertThat(jdbc.queryForObject("""
                select exists (
                    select 1 from pg_constraint
                    where conrelid = 'social_accounts'::regclass
                      and contype = 'u'
                      and pg_get_constraintdef(oid) = 'UNIQUE (provider, provider_user_id)'
                )
                """, Boolean.class)).isTrue();
    }

    @Test
    void routesRequireApplicationJwtAndRecentAuthentication() throws Exception {
        mvc.perform(post(LINK).contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"provider-token\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(CONNECTIONS)).andExpect(status().isUnauthorized());
        assertThat(link("bogus.jwt.signature").getResponse().getStatus()).isEqualTo(401);

        String username = newUsername();
        String registrationToken = register(username);
        assertThat(link(registrationToken).getResponse().getStatus()).isEqualTo(403);
        String token = passwordLogin(username);
        mvc.perform(get(CONNECTIONS).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.providers").isEmpty());
    }

    @Test
    void concurrentClaimsHaveOneWinnerAndTheWinnerCanRetryAndSignIn() throws Exception {
        String firstUsername = newUsername();
        String secondUsername = newUsername();
        register(firstUsername);
        register(secondUsername);
        String first = passwordLogin(firstUsername);
        String second = passwordLogin(secondUsername);

        var start = new CyclicBarrier(2);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var firstResult = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return link(first);
            });
            var secondResult = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return link(second);
            });
            int firstStatus = firstResult.get(20, TimeUnit.SECONDS).getResponse().getStatus();
            int secondStatus = secondResult.get(20, TimeUnit.SECONDS).getResponse().getStatus();
            assertThat(new int[] {firstStatus, secondStatus}).containsExactlyInAnyOrder(200, 409);

            String winner = firstStatus == 200 ? first : second;
            String loser = firstStatus == 200 ? second : first;
            assertThat(link(winner).getResponse().getStatus()).isEqualTo(200);
            assertThat(link(loser).getResponse().getStatus()).isEqualTo(409);
            mvc.perform(get(CONNECTIONS).header("Authorization", "Bearer " + winner))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.providers[0]").value("facebook"));

            MvcResult login = mvc.perform(post(FACEBOOK).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"provider-token\"}"))
                    .andExpect(status().isOk()).andReturn();
            assertThat(jwtService.extractUserId(readToken(login))).isEqualTo(jwtService.extractUserId(winner));
            assertThat(accounts.findProvidersByUserId(jwtService.extractUserId(loser))).isEmpty();
            assertThat(jdbc.queryForObject("""
                    select count(*) from social_accounts
                    where provider = 'facebook' and provider_user_id = ?
                    """, Long.class, facebookId)).isEqualTo(1L);
        }
    }

    private MvcResult link(String jwt) throws Exception {
        return mvc.perform(post(LINK).header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"provider-token\"}")).andReturn();
    }

    private static String newUsername() {
        return "fb_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String register(String username) throws Exception {
        return readToken(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@example.com","fullname":"Test User","password":"Secret123!"}
                                """.formatted(username, username)))
                .andExpect(status().isCreated()).andReturn());
    }

    private String passwordLogin(String username) throws Exception {
        return readToken(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Secret123!"}
                                """.formatted(username)))
                .andExpect(status().isOk()).andReturn());
    }

    private String readToken(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString()).path("accessToken").asText();
    }
}
