package com.javacraftacademy.userservice.repository;

import com.javacraftacademy.userservice.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    @Query("SELECT p FROM Profile p WHERE p.user.email = :email")
    Optional<Profile> findByUserEmail(@Param("email") String email);

    @Query("SELECT p FROM Profile p WHERE p.user.username = :username")
    Optional<Profile> findByUserUsername(@Param("username") String username);

    @Query("SELECT p FROM Profile p WHERE p.firstName LIKE %:searchTerm% OR p.lastName LIKE %:searchTerm%")
    List<Profile> findProfilesByName(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Profile p WHERE p.city = :city")
    List<Profile> findProfilesByCity(@Param("city") String city);

    @Query("SELECT p FROM Profile p WHERE p.country = :country")
    List<Profile> findProfilesByCountry(@Param("country") String country);

    @Query("SELECT p FROM Profile p WHERE p.city = :city AND p.country = :country")
    List<Profile> findProfilesByCityAndCountry(@Param("city") String city, 
                                             @Param("country") String country);

    @Query("SELECT p FROM Profile p WHERE p.dateOfBirth BETWEEN :startDate AND :endDate")
    List<Profile> findProfilesByDateOfBirthRange(@Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Profile p WHERE p.phoneNumber = :phoneNumber")
    Optional<Profile> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT p FROM Profile p WHERE p.profilePictureUrl IS NOT NULL")
    List<Profile> findProfilesWithPicture();

    @Query("SELECT p FROM Profile p WHERE p.bio IS NOT NULL AND p.bio != ''")
    List<Profile> findProfilesWithBio();

    @Query("SELECT p FROM Profile p WHERE p.user.isActive = true")
    List<Profile> findActiveProfiles();

    @Query("SELECT COUNT(p) FROM Profile p WHERE p.country = :country")
    long countProfilesByCountry(@Param("country") String country);

    @Query("SELECT p.country, COUNT(p) FROM Profile p GROUP BY p.country ORDER BY COUNT(p) DESC")
    List<Object[]> getProfileCountByCountry();

    @Query("SELECT p FROM Profile p WHERE p.updatedDate >= :date")
    List<Profile> findRecentlyUpdatedProfiles(@Param("date") LocalDate date);

    boolean existsByUserId(Long userId);

    @Query("SELECT p FROM Profile p WHERE p.firstName = :firstName AND p.lastName = :lastName")
    List<Profile> findByFullName(@Param("firstName") String firstName, 
                               @Param("lastName") String lastName);
}