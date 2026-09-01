package fullstack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fullstack.model.Label;

public interface LabelRepository
        extends JpaRepository<Label, Long> {

    Optional<Label> findByNameIgnoreCase(
            String name);

}