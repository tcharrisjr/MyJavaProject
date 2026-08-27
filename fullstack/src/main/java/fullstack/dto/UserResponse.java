package fullstack.dto;

import fullstack.model.AppUser;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private String role;

    private LocalDateTime createdDate;

    public UserResponse() {
    }

    public UserResponse(
            Long id,
            String name,
            String email,
            String role,
            LocalDateTime createdDate) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdDate = createdDate;
    }

    public static UserResponse from(
            AppUser user) {

        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.getCreatedDate()
        );
    }

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

    public String getRole() {

        return role;
    }

    public void setRole(
            String role) {

        this.role = role;
    }

    public LocalDateTime
        getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(
            LocalDateTime createdDate) {

        this.createdDate =
            createdDate;
    }
}