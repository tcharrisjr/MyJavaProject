package fullstack.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdDate;

    /*
     * Many projects can belong to one AppUser.
     *
     * nullable = true temporarily allows the schema
     * migration while older data is being cleaned up.
     */
    @JsonIgnore
    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "user_id",
        nullable = true
    )
    private AppUser owner;

    public Project() {
    }

    public Project(
            String name,
            String description) {

        this.name =
            name;

        this.description =
            description;
    }

    @PrePersist
    protected void onCreate() {

        if (createdDate == null) {

            createdDate =
                LocalDateTime.now();
        }
    }

    public Long getId() {

        return id;
    }

    public void setId(
            Long id) {

        this.id =
            id;
    }

    public String getName() {

        return name;
    }

    public void setName(
            String name) {

        this.name =
            name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(
            String description) {

        this.description =
            description;
    }

    public LocalDateTime getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(
            LocalDateTime createdDate) {

        this.createdDate =
            createdDate;
    }

    public AppUser getOwner() {

        return owner;
    }

    public void setOwner(
            AppUser owner) {

        this.owner =
            owner;
    }
}