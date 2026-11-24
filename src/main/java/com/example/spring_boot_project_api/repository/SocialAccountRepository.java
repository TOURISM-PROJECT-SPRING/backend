package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.SocialAccount;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    @Query("select distinct a.provider from SocialAccount a where a.user.id = :userId order by a.provider")
    List<String> findProvidersByUserId(Long userId);

    @Query(value = "select id from users where id = :userId for update", nativeQuery = true)
    Optional<Long> lockUserForLinking(Long userId);

    @Modifying
    @Query(value = """
            insert into social_accounts (user_id, provider, provider_user_id, created_at, updated_at)
            values (:userId, 'facebook', :providerUserId, current_timestamp, current_timestamp)
            on conflict (provider, provider_user_id) do nothing
            """, nativeQuery = true)
    int insertFacebookLink(Long userId, String providerUserId);

    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}
