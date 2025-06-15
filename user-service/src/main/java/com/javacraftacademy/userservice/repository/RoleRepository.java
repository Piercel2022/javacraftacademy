
package com.javacraftacademy.userservice.repository;

import com.javacraftacademy.userservice.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    Set<Role> findByNameIn(@Param("names") List<String> names);

    @Query("SELECT r FROM Role r WHERE r.description LIKE %:keyword%")
    List<Role> findByDescriptionContaining(@Param("keyword") String keyword);

    @Query("SELECT r FROM Role r ORDER BY r.name ASC")
    List<Role> findAllOrderByName();

    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    Set<Role> findRolesByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.email = :email")
    Set<Role> findRolesByUserEmail(@Param("email") String email);

    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.username = :username")
    Set<Role> findRolesByUserUsername(@Param("username") String username);

    @Query("SELECT r.name, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r.id, r.name")
    List<Object[]> getRoleUserCounts();

    @Query("SELECT COUNT(u) FROM Role r JOIN r.users u WHERE r.name = :roleName")
    long countUsersByRoleName(@Param("roleName") String roleName);

    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0")
    List<Role> findUnusedRoles();

    @Query("SELECT r FROM Role r WHERE SIZE(r.users) > 0")
    List<Role> findUsedRoles();

    @Query("SELECT DISTINCT r FROM Role r JOIN r.users u WHERE u.isActive = true")
    List<Role> findRolesWithActiveUsers();

    @Query("SELECT r FROM Role r WHERE r.name LIKE %:searchTerm% OR r.description LIKE %:searchTerm%")
    List<Role> findRolesBySearchTerm(@Param("searchTerm") String searchTerm);

    // Method to find default roles (assuming there's a field to mark default roles)
    @Query("SELECT r FROM Role r WHERE r.isDefault = true")
    List<Role> findDefaultRoles();

    // Method to find roles by permission level (if you have a permission system)
    @Query("SELECT r FROM Role r WHERE r.permissionLevel >= :minLevel")
    List<Role> findRolesByMinimumPermissionLevel(@Param("minLevel") Integer minLevel);

    @Query("SELECT r FROM Role r WHERE r.permissionLevel = :level")
    List<Role> findRolesByPermissionLevel(@Param("level") Integer level);
}