package fullstack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fullstack.model.AppUser;

public interface UserRepository
        extends JpaRepository<AppUser, Long> {

    /*
     * Find a user during authentication.
     */
    Optional<AppUser>
        findByEmailIgnoreCase(
            String email
        );

    /*
     * Prevent duplicate accounts during registration.
     */
    boolean
        existsByEmailIgnoreCase(
            String email
        );
}