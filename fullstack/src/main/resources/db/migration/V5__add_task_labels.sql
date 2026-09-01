CREATE TABLE labels (
    id BIGINT IDENTITY(1,1) NOT NULL,
    name VARCHAR(100) NOT NULL,

    CONSTRAINT pk_labels
        PRIMARY KEY (id),

    CONSTRAINT uq_labels_name
        UNIQUE (name)
);

CREATE TABLE task_labels (
    task_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,

    CONSTRAINT pk_task_labels
        PRIMARY KEY (task_id, label_id),

    CONSTRAINT fk_task_labels_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_labels_label
        FOREIGN KEY (label_id)
        REFERENCES labels(id)
        ON DELETE CASCADE
);

CREATE INDEX ix_task_labels_label_id
    ON task_labels(label_id);