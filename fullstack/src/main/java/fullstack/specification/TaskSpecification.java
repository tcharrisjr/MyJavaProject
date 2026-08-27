package fullstack.specification;

import fullstack.model.Task;
import fullstack.model.TaskPriority;
import fullstack.model.TaskStatus;

import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecification {

    /*
     * Utility class only.
     */
    private TaskSpecification() {
    }

    /*
     * =====================================================
     * PROJECT FILTER
     * =====================================================
     */

    public static Specification<Task>
        hasProjectId(Long projectId) {

        return (root, query, criteriaBuilder) -> {

            if (projectId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                root.get("project").get("id"),
                projectId
            );
        };
    }

    /*
     * =====================================================
     * STATUS FILTER
     * =====================================================
     */

    public static Specification<Task>
        hasStatus(TaskStatus status) {

        return (root, query, criteriaBuilder) -> {

            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                root.get("status"),
                status
            );
        };
    }

    /*
     * =====================================================
     * PRIORITY FILTER
     * =====================================================
     */

    public static Specification<Task>
        hasPriority(TaskPriority priority) {

        return (root, query, criteriaBuilder) -> {

            if (priority == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                root.get("priority"),
                priority
            );
        };
    }

    /*
     * =====================================================
     * SEARCH FILTER
     * =====================================================
     *
     * Searches:
     *
     * task title
     * task description
     * project name
     */

    public static Specification<Task>
        containsSearchText(String search) {

        return (root, query, criteriaBuilder) -> {

            if (
                search == null ||
                search.trim().isEmpty()
            ) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern =
                "%" +
                search.trim().toLowerCase() +
                "%";

            return criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(
                        root.get("title")
                    ),
                    searchPattern
                ),

                criteriaBuilder.like(
                    criteriaBuilder.lower(
                        root.get("description")
                    ),
                    searchPattern
                ),

                criteriaBuilder.like(
                    criteriaBuilder.lower(
                        root.get("project")
                            .get("name")
                    ),
                    searchPattern
                )
            );
        };
    }
}