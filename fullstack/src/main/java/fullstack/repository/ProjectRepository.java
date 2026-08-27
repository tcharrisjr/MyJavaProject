package fullstack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fullstack.model.Project;

@Repository
public interface ProjectRepository
        extends JpaRepository<Project, Long> {

    // =========================================================
    // PROJECTS OWNED BY USER
    // =========================================================

    List<Project>
        findAllByOwner_EmailIgnoreCaseOrderByCreatedDateDesc(
            String email
        );

    // =========================================================
    // SINGLE PROJECT OWNED BY USER
    // =========================================================

    Optional<Project>
        findByIdAndOwner_EmailIgnoreCase(
            Long projectId,
            String email
        );

    // =========================================================
    // PROJECT COUNT FOR USER
    // =========================================================

    long countByOwner_EmailIgnoreCase(
            String email
    );
}