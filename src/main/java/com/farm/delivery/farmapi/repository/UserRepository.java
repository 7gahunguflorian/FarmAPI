package com.farm.delivery.farmapi.repository;

import com.farm.delivery.farmapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(User.Role role);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByRole(User.Role role);
}
