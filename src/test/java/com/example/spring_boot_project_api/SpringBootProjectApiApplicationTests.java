package com.example.spring_boot_project_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@EnabledIfEnvironmentVariable(named = "TOURISM_LINK_TEST_DB", matches = ".+")
@SpringBootTest
class SpringBootProjectApiApplicationTests extends DisposablePostgresTest {

	@Test
	void contextLoads() {
	}

}
