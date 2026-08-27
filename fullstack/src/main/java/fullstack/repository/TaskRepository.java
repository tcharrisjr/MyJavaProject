package fullstack.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fullstack.model.Task;

@Repository
public interface TaskRepository
        extends JpaRepository<Task, Long> {

    // =========================================================
    // FULL PROJECT TASK LIST
    //
    // Temporarily retained until Step 8B removes summaryTasks
    // from the React dashboard.
    // =========================================================

    @Query("""
        SELECT t
        FROM Task t
        WHERE t.project.id = :projectId
          AND LOWER(t.project.owner.email) = LOWER(:email)
        ORDER BY t.createdDate DESC
        """)
    List<Task> findTasksForOwnedProject(

            @Param("projectId")
            Long projectId,

            @Param("email")
            String email
    );

    // =========================================================
    // PAGED / FILTERED TASK QUERY
    // =========================================================

    @Query(
        value = """
            SELECT t
            FROM Task t
            WHERE t.project.id = :projectId

              AND LOWER(t.project.owner.email)
                  = LOWER(:email)

              AND (
                    :status IS NULL
                    OR LOWER(t.status)
                        = LOWER(:status)
                  )

              AND (
                    :priority IS NULL
                    OR LOWER(t.priority)
                        = LOWER(:priority)
                  )

              AND (
                    :search IS NULL

                    OR LOWER(t.title)
                        LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                        )

                    OR LOWER(
                        COALESCE(
                            t.description,
                            ''
                        )
                    )
                        LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                        )
                  )

              AND (
                    :dueDateFilter IS NULL

                    OR (
                        :dueDateFilter = 'OVERDUE'
                        AND t.dueDate < :today
                        AND t.status <> 'COMPLETED'
                    )

                    OR (
                        :dueDateFilter = 'DUE_TODAY'
                        AND t.dueDate = :today
                    )

                    OR (
                        :dueDateFilter = 'DUE_SOON'
                        AND t.dueDate > :today
                        AND t.dueDate <= :dueSoonEnd
                        AND t.status <> 'COMPLETED'
                    )

                    OR (
                        :dueDateFilter = 'NO_DUE_DATE'
                        AND t.dueDate IS NULL
                    )
                  )
            """,

        countQuery = """
            SELECT COUNT(t)
            FROM Task t
            WHERE t.project.id = :projectId

              AND LOWER(t.project.owner.email)
                  = LOWER(:email)

              AND (
                    :status IS NULL
                    OR LOWER(t.status)
                        = LOWER(:status)
                  )

              AND (
                    :priority IS NULL
                    OR LOWER(t.priority)
                        = LOWER(:priority)
                  )

              AND (
                    :search IS NULL

                    OR LOWER(t.title)
                        LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                        )

                    OR LOWER(
                        COALESCE(
                            t.description,
                            ''
                        )
                    )
                        LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                        )
                  )

              AND (
                    :dueDateFilter IS NULL

                    OR (
                        :dueDateFilter = 'OVERDUE'
                        AND t.dueDate < :today
                        AND t.status <> 'COMPLETED'
                    )

                    OR (
                        :dueDateFilter = 'DUE_TODAY'
                        AND t.dueDate = :today
                    )

                    OR (
                        :dueDateFilter = 'DUE_SOON'
                        AND t.dueDate > :today
                        AND t.dueDate <= :dueSoonEnd
                        AND t.status <> 'COMPLETED'
                    )

                    OR (
                        :dueDateFilter = 'NO_DUE_DATE'
                        AND t.dueDate IS NULL
                    )
                  )
            """
    )
    Page<Task> searchTasksForOwnedProject(

            @Param("projectId")
            Long projectId,

            @Param("email")
            String email,

            @Param("status")
            String status,

            @Param("priority")
            String priority,

            @Param("search")
            String search,

            @Param("dueDateFilter")
            String dueDateFilter,

            @Param("today")
            LocalDate today,

            @Param("dueSoonEnd")
            LocalDate dueSoonEnd,

            Pageable pageable
    );

    // =========================================================
    // OWNED TASK
    // =========================================================

    Optional<Task>
        findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(

            Long taskId,

            Long projectId,

            String email
        );

    // =========================================================
    // GLOBAL USER STATISTICS
    // =========================================================

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE LOWER(t.project.owner.email)
            = LOWER(:email)
        """)
    long countTasksForUser(

            @Param("email")
            String email
    );

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE LOWER(t.project.owner.email)
            = LOWER(:email)

          AND LOWER(t.status)
            = LOWER(:status)
        """)
    long countTasksForUserByStatus(

            @Param("email")
            String email,

            @Param("status")
            String status
    );

    // =========================================================
    // STEP 8
    // PROJECT-SPECIFIC HEALTH QUERIES
    // =========================================================

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
          AND LOWER(t.project.owner.email) = LOWER(:email)
        """)
    long countTasksForOwnedProject(

            @Param("projectId")
            Long projectId,

            @Param("email")
            String email
    );

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
          AND LOWER(t.project.owner.email) = LOWER(:email)
          AND LOWER(t.status) = LOWER(:status)
        """)
    long countTasksForOwnedProjectByStatus(

            @Param("projectId")
            Long projectId,

            @Param("email")
            String email,

            @Param("status")
            String status
    );

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
          AND LOWER(t.project.owner.email) = LOWER(:email)
          AND t.dueDate < :today
          AND t.status <> 'COMPLETED'
        """)
    long countOverdueTasksForOwnedProject(

            @Param("projectId")
            Long projectId,

            @Param("email")
            String email,

            @Param("today")
            LocalDate today
    );

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
          AND LOWER(t.project.owner.email) = LOWER(:email)
          AND t.dueDate >= :today
          AND t.dueDate <= :dueSoonEnd
          AND t.status <> 'COMPLETED'
        """)
    long countDueSoonTasksForOwnedProject(

            @Param("projectId")
            Long projectId,

            @Param("email")
            String email,

            @Param("today")
            LocalDate today,

            @Param("dueSoonEnd")
            LocalDate dueSoonEnd
    );
}