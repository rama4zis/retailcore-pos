package com.retailcore.pos.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEntity> findByEmailIgnoreCase(String email);
}
