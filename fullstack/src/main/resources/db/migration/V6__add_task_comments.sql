CREATE TABLE task_comments (
    id BIGINT IDENTITY(1,1) NOT NULL,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment_text VARCHAR(2000) NOT NULL,
    created_at DATETIME2 NOT NULL
        CONSTRAINT df_task_comments_created_at DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NULL,

    CONSTRAINT pk_task_comments
        PRIMARY KEY (id),

    CONSTRAINT fk_task_comments_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_comments_user
    FOREIGN KEY (user_id)
    REFERENCES app_users(id)
);

CREATE INDEX ix_task_comments_task_id
    ON task_comments(task_id);

CREATE INDEX ix_task_comments_user_id
    ON task_comments(user_id);

CREATE INDEX ix_task_comments_created_at
    ON task_comments(created_at);