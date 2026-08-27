package fullstack.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "app_users")
public class AppUser
        implements UserDetails {

    private static final long serialVersionUID = 1L;

    /*
     * =====================================================
     * DATABASE ID
     * =====================================================
     */

    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    private Long id;

    /*
     * =====================================================
     * DISPLAY NAME
     * =====================================================
     */

    @Column(
        nullable = false
    )
    private String name;

    /*
     * =====================================================
     * EMAIL / LOGIN IDENTIFIER
     * =====================================================
     *
     * This application authenticates users by email.
     *
     * There is intentionally NO username field in the
     * database.
     */
    @Column(
        nullable = false,
        unique = true
    )
    private String email;

    /*
     * =====================================================
     * PASSWORD
     * =====================================================
     *
     * The password stored here should already be encoded.
     *
     * JsonIgnore prevents it from being serialized and
     * returned to the React frontend.
     */
    @JsonIgnore
    @Column(
        nullable = false
    )
    private String password;

    /*
     * =====================================================
     * ROLE
     * =====================================================
     */

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false
    )
    private UserRole role =
        UserRole.USER;

    /*
     * =====================================================
     * ENABLED
     * =====================================================
     */

    @Column(
        nullable = false
    )
    private boolean enabled =
        true;

    /*
     * =====================================================
     * CREATED DATE
     * =====================================================
     *
     * Records when the account was first created.
     */
    @Column(
        name = "created_date",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdDate;

    /*
     * =====================================================
     * DEFAULT CONSTRUCTOR
     * =====================================================
     *
     * Required by JPA.
     */
    public AppUser() {
    }

    /*
     * =====================================================
     * CONSTRUCTOR
     * =====================================================
     */

    public AppUser(
            String name,
            String email,
            String password,
            UserRole role,
            boolean enabled) {

        this.name =
            name;

        this.email =
            email;

        this.password =
            password;

        this.role =
            role;

        this.enabled =
            enabled;
    }

    /*
     * =====================================================
     * PRE-PERSIST
     * =====================================================
     *
     * Automatically assigns createdDate before Hibernate
     * inserts a new AppUser.
     */
    @PrePersist
    protected void onCreate() {

        if (createdDate == null) {

            createdDate =
                LocalDateTime.now();
        }
    }

    /*
     * =====================================================
     * ID
     * =====================================================
     */

    public Long getId() {

        return id;
    }

    public void setId(
            Long id) {

        this.id =
            id;
    }

    /*
     * =====================================================
     * NAME
     * =====================================================
     */

    public String getName() {

        return name;
    }

    public void setName(
            String name) {

        this.name =
            name;
    }

    /*
     * =====================================================
     * EMAIL
     * =====================================================
     */

    public String getEmail() {

        return email;
    }

    public void setEmail(
            String email) {

        this.email =
            email;
    }

    /*
     * =====================================================
     * PASSWORD
     * =====================================================
     */

    @Override
    public String getPassword() {

        return password;
    }

    public void setPassword(
            String password) {

        this.password =
            password;
    }

    /*
     * =====================================================
     * ROLE
     * =====================================================
     */

    public UserRole getRole() {

        return role;
    }

    public void setRole(
            UserRole role) {

        this.role =
            role;
    }

    /*
     * =====================================================
     * CREATED DATE
     * =====================================================
     */

    public LocalDateTime getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(
            LocalDateTime createdDate) {

        this.createdDate =
            createdDate;
    }

    /*
     * =====================================================
     * SPRING SECURITY USERNAME
     * =====================================================
     *
     * Spring Security requires UserDetails.getUsername().
     *
     * In this application, the user's EMAIL is the login
     * identifier.
     *
     * @Transient explicitly tells Hibernate/JPA:
     *
     * DO NOT create a "username" database column for this
     * method.
     */
    @Transient
    @Override
    public String getUsername() {

        return email;
    }

    /*
     * =====================================================
     * AUTHORITIES
     * =====================================================
     *
     * Spring Security expects roles in this form:
     *
     * ROLE_USER
     * ROLE_ADMIN
     */
    @Transient
    @Override
    public Collection<? extends GrantedAuthority>
        getAuthorities() {

        return List.of(
            new SimpleGrantedAuthority(
                "ROLE_" + role.name()
            )
        );
    }

    /*
     * =====================================================
     * ENABLED
     * =====================================================
     */

    @Override
    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(
            boolean enabled) {

        this.enabled =
            enabled;
    }

    /*
     * =====================================================
     * SPRING SECURITY ACCOUNT STATUS
     * =====================================================
     */

    @Transient
    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Transient
    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Transient
    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }
}