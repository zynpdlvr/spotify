package com.example.spotify.repository;
import com.example.spotify.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, Long> { //Jpa repo findbyid,save,del
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByPublicId(UUID publicId);
}
