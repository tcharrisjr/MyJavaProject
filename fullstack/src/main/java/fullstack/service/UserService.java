package fullstack.service;

import fullstack.dto.RegisterRequest;
import fullstack.exception.BadRequestException;
import fullstack.model.AppUser;
import fullstack.model.UserRole;
import fullstack.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService
        implements UserDetailsService {

    private final UserRepository
        userRepository;

    private final PasswordEncoder
        passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository =
            userRepository;

        this.passwordEncoder =
            passwordEncoder;
    }

    /*
     * =====================================================
     * LOAD USER FOR SPRING SECURITY
     * =====================================================
     *
     * Spring Security calls this method
     * loadUserByUsername(), but our username is
     * actually the user's email address.
     */
    @Override
    public UserDetails loadUserByUsername(
            String email) {

        return getByEmail(
            email
        );
    }

    /*
     * =====================================================
     * FIND USER BY EMAIL
     * =====================================================
     */

    public AppUser getByEmail(
            String email) {

        String normalizedEmail =
            normalizeEmail(
                email
            );

        return userRepository
            .findByEmailIgnoreCase(
                normalizedEmail
            )
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        "User not found."
                    )
            );
    }

    /*
     * =====================================================
     * REGISTER USER
     * =====================================================
     */

    public AppUser register(
            RegisterRequest request) {

        String normalizedEmail =
            normalizeEmail(
                request.getEmail()
            );

        if (
            userRepository
                .existsByEmailIgnoreCase(
                    normalizedEmail
                )
        ) {

            throw new BadRequestException(
                "An account already exists with that email address."
            );
        }

        AppUser user =
            new AppUser();

        user.setName(
            request
                .getName()
                .trim()
        );

        user.setEmail(
            normalizedEmail
        );

        /*
         * Never store the raw password.
         */
        user.setPassword(
            passwordEncoder
                .encode(
                    request
                        .getPassword()
                )
        );

        user.setRole(
            UserRole.USER
        );

        user.setEnabled(
            true
        );

        return userRepository
            .save(
                user
            );
    }

    /*
     * =====================================================
     * EMAIL NORMALIZATION
     * =====================================================
     */

    private String normalizeEmail(
            String email) {

        return email
            .trim()
            .toLowerCase();
    }
}