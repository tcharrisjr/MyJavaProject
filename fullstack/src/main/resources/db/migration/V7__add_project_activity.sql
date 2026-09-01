CREATE TABLE project_activity (
    id BIGINT IDENTITY(1,1) NOT NULL,

    project_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    user_id BIGINT NOT NULL,

    activity_type VARCHAR(50) NOT NULL,

    field_name VARCHAR(100) NULL,
    old_value VARCHAR(2000) NULL,
    new_value VARCHAR(2000) NULL,

    description VARCHAR(2000) NOT NULL,

    created_at DATETIME2 NOT NULL
        CONSTRAINT df_project_activity_created_at
        DEFAULT SYSUTCDATETIME(),

    CONSTRAINT pk_project_activity
        PRIMARY KEY (id),

    CONSTRAINT fk_project_activity_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_project_activity_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_project_activity_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
);

CREATE INDEX ix_project_activity_project_id
    ON project_activity(project_id);

CREATE INDEX ix_project_activity_task_id
    ON project_activity(task_id);

CREATE INDEX ix_project_activity_user_id
    ON project_activity(user_id);

CREATE INDEX ix_project_activity_created_at
    ON project_activity(created_at);

CREATE INDEX ix_project_activity_project_created_at
    ON project_activity(project_id, created_at DESC);