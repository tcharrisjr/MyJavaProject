package fullstack.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.activity.ProjectActivityResponse;

import fullstack.model.ActivityType;
import fullstack.model.AppUser;
import fullstack.model.Project;
import fullstack.model.ProjectActivity;
import fullstack.model.Task;

import fullstack.repository.ProjectActivityRepository;
import fullstack.repository.ProjectRepository;
import fullstack.repository.UserRepository;

@Service
@Transactional
public class ProjectActivityService {

    private final ProjectActivityRepository
            projectActivityRepository;

    private final ProjectRepository
            projectRepository;

    private final UserRepository
            userRepository;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProjectActivityService(
            ProjectActivityRepository projectActivityRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.projectActivityRepository =
                projectActivityRepository;

        this.projectRepository =
                projectRepository;

        this.userRepository =
                userRepository;
    }

    // =========================================================
    // GET PROJECT ACTIVITY
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProjectActivityResponse>
            getProjectActivity(
                    Long projectId,
                    String email) {

        verifyProjectOwnership(
                projectId,
                email
        );

        return projectActivityRepository
                .findByProject_IdOrderByCreatedAtDesc(
                        projectId
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    // =========================================================
    // GET PROJECT ACTIVITY COUNT
    // =========================================================

    @Transactional(readOnly = true)
    public long getProjectActivityCount(
            Long projectId,
            String email) {

        verifyProjectOwnership(
                projectId,
                email
        );

        return projectActivityRepository
                .countByProject_Id(
                        projectId
                );
    }

    // =========================================================
    // RECORD PROJECT ACTIVITY
    //
    // Example:
    // PROJECT_CREATED
    // =========================================================

    public void recordActivity(
            Project project,
            String email,
            ActivityType activityType,
            String description) {

        recordActivity(
                project,
                null,
                email,
                activityType,
                null,
                null,
                null,
                description
        );
    }

    // =========================================================
    // RECORD PROJECT / TASK FIELD ACTIVITY
    //
    // Used by ProjectService for:
    // - project name changes
    // - project description changes
    //
    // Also supports task-associated activity.
    // =========================================================

    public void recordActivity(
            Project project,
            Task task,
            String email,
            ActivityType activityType,
            String fieldName,
            String oldValue,
            String newValue,
            String description) {

        AppUser user =
                getUserByEmail(
                        email
                );

        ProjectActivity activity =
                new ProjectActivity();

        activity.setProject(
                project
        );

        activity.setTask(
                task
        );

        activity.setUser(
                user
        );

        activity.setActivityType(
                activityType
        );

        activity.setFieldName(
                normalizeNullableValue(
                        fieldName
                )
        );

        activity.setOldValue(
                normalizeNullableValue(
                        oldValue
                )
        );

        activity.setNewValue(
                normalizeNullableValue(
                        newValue
                )
        );

        activity.setDescription(
                normalizeDescription(
                        description
                )
        );

        projectActivityRepository.save(
                activity
        );
    }

    // =========================================================
    // RECORD TASK ACTIVITY
    //
    // Used for:
    // - TASK_CREATED
    // - TASK_DELETED
    // =========================================================

    public void recordTaskActivity(
            Project project,
            Task task,
            String email,
            ActivityType activityType,
            String description) {

        recordActivity(
                project,
                task,
                email,
                activityType,
                null,
                null,
                null,
                description
        );
    }

    // =========================================================
    // RECORD TASK FIELD CHANGE
    //
    // Used for:
    // - TASK_UPDATED
    // - TASK_STATUS_CHANGED
    // - TASK_ASSIGNED
    // - TASK_UNASSIGNED
    // - TASK_LABELS_CHANGED
    // =========================================================

    public void recordTaskFieldChange(
            Project project,
            Task task,
            String email,
            ActivityType activityType,
            String fieldName,
            String oldValue,
            String newValue,
            String description) {

        recordActivity(
                project,
                task,
                email,
                activityType,
                fieldName,
                oldValue,
                newValue,
                description
        );
    }

    // =========================================================
    // VERIFY PROJECT OWNERSHIP
    // =========================================================

    private void verifyProjectOwnership(
            Long projectId,
            String email) {

        if (
            projectId == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Project id is required."
            );
        }

        String normalizedEmail =
                normalizeEmail(
                        email
                );

        projectRepository
                .findByIdAndOwner_EmailIgnoreCase(
                        projectId,
                        normalizedEmail
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Project not found."
                                )
                );
    }

    // =========================================================
    // GET AUTHENTICATED USER
    // =========================================================

    private AppUser getUserByEmail(
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
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user not found."
                                )
                );
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private ProjectActivityResponse toResponse(
            ProjectActivity activity) {

        ProjectActivityResponse response =
                new ProjectActivityResponse();

        response.setId(
                activity.getId()
        );

        if (
            activity.getProject() != null
        ) {

            response.setProjectId(
                    activity
                            .getProject()
                            .getId()
            );
        }

        if (
            activity.getTask() != null
        ) {

            response.setTaskId(
                    activity
                            .getTask()
                            .getId()
            );
        }

        if (
            activity.getUser() != null
        ) {

            response.setUserId(
                    activity
                            .getUser()
                            .getId()
            );

            response.setUserName(
                    activity
                            .getUser()
                            .getName()
            );

            response.setUserEmail(
                    activity
                            .getUser()
                            .getEmail()
            );
        }

        response.setActivityType(
                activity.getActivityType()
        );

        response.setFieldName(
                activity.getFieldName()
        );

        response.setOldValue(
                activity.getOldValue()
        );

        response.setNewValue(
                activity.getNewValue()
        );

        response.setDescription(
                activity.getDescription()
        );

        response.setCreatedAt(
                activity.getCreatedAt()
        );

        return response;
    }

    // =========================================================
    // NORMALIZE EMAIL
    // =========================================================

    private String normalizeEmail(
            String email) {

        if (
            email == null
            || email.isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required."
            );
        }

        return email.trim();
    }

    // =========================================================
    // NORMALIZE DESCRIPTION
    // =========================================================

    private String normalizeDescription(
            String description) {

        if (
            description == null
        ) {

            return "";
        }

        return description.trim();
    }

    // =========================================================
    // NORMALIZE OPTIONAL VALUE
    // =========================================================

    private String normalizeNullableValue(
            String value) {

        if (
            value == null
        ) {

            return null;
        }

        return value.trim();
    }
}