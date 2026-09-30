
package com.project.vision.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.vision.entity.AdminOAuthAccount;

public interface AdminOAuthAccountRepo extends JpaRepository<AdminOAuthAccount, String> {

    @Query("""
        SELECT a FROM AdminOAuthAccount a
        JOIN FETCH a.admin
        WHERE a.provider = :provider
        AND a.providerUserId = :providerUserId
        """)
    Optional<AdminOAuthAccount> findByProviderAndProviderUserId(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId
    );
}
