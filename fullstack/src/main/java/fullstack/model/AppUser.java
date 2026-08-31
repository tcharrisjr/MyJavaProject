package fullstack.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;

    @JsonIgnore
    @Column(
            nullable = false,
            length = 255
    )
    private String password;

    /*
     * Existing project design uses UserRole.
     *
     * Stored in SQL Server as text such as:
     *
     * USER
     * ADMIN
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 255
    )
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(
            name = "created_date",
            nullable = false
    )
    private LocalDateTime createdDate;

    /*
     * Sequence 13A
     *
     * Inverse side of Task.assignee.
     *
     * JsonIgnore prevents recursive serialization:
     *
     * AppUser -> Task -> AppUser -> Task ...
     */
    @JsonIgnore
    @OneToMany(
            mappedBy = "assignee",
            fetch = FetchType.LAZY
    )
    private List<Task> assignedTasks =
            new ArrayList<>();

    public AppUser() {
    }

    @PrePersist
    protected void onCreate() {

        if (createdDate == null) {
            createdDate =
                    LocalDateTime.now();
        }
    }

    // =========================================================
    // SPRING SECURITY
    // =========================================================

    @Override
    public Collection<? extends GrantedAuthority>
            getAuthorities() {

        if (role == null) {

            return List.of();
        }

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + role.name()
                )
        );
    }

    @Override
    public String getUsername() {

        return email;
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return enabled;
    }

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {

        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(
            String name) {

        this.name = name;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(
            String email) {

        this.email = email;
    }

    @Override
    public String getPassword() {

        return password;
    }

    public void setPassword(
            String password) {

        this.password = password;
    }

    public UserRole getRole() {

        return role;
    }

    public void setRole(
            UserRole role) {

        this.role = role;
    }

    public void setEnabled(
            boolean enabled) {

        this.enabled = enabled;
    }

    public LocalDateTime getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(
            LocalDateTime createdDate) {

        this.createdDate =
                createdDate;
    }

    public List<Task> getAssignedTasks() {

        return assignedTasks;
    }

    public void setAssignedTasks(
            List<Task> assignedTasks) {

        this.assignedTasks =
                assignedTasks;
    }
}