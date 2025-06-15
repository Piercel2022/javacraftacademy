
package com.javacraftacademy.userservice.repository;

import com.javacraftacademy.userservice.model.entity.RefreshToken;
import com.javacraftacademy.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(Long userId);

    boolean existsByToken(String token);

    boolean existsByUserId(Long userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.expiryDate > :currentTime")
    Optional<RefreshToken> findByValidToken(@Param("token") String token, 
                                          @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiryDate > :currentTime")
    Optional<RefreshToken> findValidTokenByUserId(@Param("userId") Long userId, 
                                                @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate <= :currentTime")
    List<RefreshToken> findExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.email = :email")
    List<RefreshToken> findTokensByUserEmail(@Param("email") String email);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.username = :username")
    List<RefreshToken> findTokensByUserUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate <= :currentTime")
    int deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
    int deleteByToken(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    int deleteByUser(@Param("user") User user);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId")
    long countTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiryDate > :currentTime")
    long countActiveTokens(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.createdDate BETWEEN :startDate AND :endDate")
    List<RefreshToken> findTokensByCreationDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.lastUsedDate < :cutoffDate")
    List<RefreshToken> findUnusedTokensSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT rt FROM RefreshToken rt ORDER BY rt.lastUsedDate DESC")
    List<RefreshToken> findAllOrderByLastUsedDesc();

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId ORDER BY rt.createdDate DESC")
    List<RefreshToken> findTokensByUserIdOrderByCreatedDate(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.lastUsedDate = :lastUsedDate WHERE rt.token = :token")
    int updateLastUsedDate(@Param("token") String token, @Param("lastUsedDate") LocalDateTime lastUsedDate);

    // Method to find tokens that will expire soon (useful for notifications)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate BETWEEN :now AND :warningTime")
    List<RefreshToken> findTokensExpiringBetween(@Param("now") LocalDateTime now, 
                                               @Param("warningTime") LocalDateTime warningTime);
}