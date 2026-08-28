/*
=========================================================
V1__initial_schema.sql

Initial schema for the Full Stack Project Manager.

Tables:
    app_users
    project
    tasks

Relationships:
    app_users 1 ---- * project
    project   1 ---- * tasks
=========================================================
*/


/*
=========================================================
APP USERS
=========================================================
*/

CREATE TABLE dbo.app_users
(
    id BIGINT IDENTITY(1,1) NOT NULL,

    created_date DATETIME2 NOT NULL,

    email VARCHAR(255) NOT NULL,

    enabled BIT NOT NULL,

    name VARCHAR(255) NOT NULL,

    password VARCHAR(255) NOT NULL,

    role VARCHAR(255) NOT NULL,

    CONSTRAINT PK_app_users
        PRIMARY KEY (id),

    CONSTRAINT uk_app_users_email
        UNIQUE (email)
);


/*
=========================================================
PROJECT
=========================================================
*/

CREATE TABLE dbo.project
(
    id BIGINT IDENTITY(1,1) NOT NULL,

    created_date DATETIME2 NULL,

    description VARCHAR(255) NULL,

    name VARCHAR(255) NULL,

    user_id BIGINT NULL,

    CONSTRAINT PK_project
        PRIMARY KEY (id),

    CONSTRAINT FK_project_app_users
        FOREIGN KEY (user_id)
        REFERENCES dbo.app_users(id)
);


/*
=========================================================
TASKS
=========================================================
*/

CREATE TABLE dbo.tasks
(
    id BIGINT IDENTITY(1,1) NOT NULL,

    completed_date DATETIME2 NULL,

    created_date DATETIME2 NOT NULL,

    description VARCHAR(2000) NULL,

    due_date DATE NULL,

    priority VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL,

    title VARCHAR(200) NOT NULL,

    updated_date DATETIME2 NOT NULL,

    project_id BIGINT NOT NULL,

    CONSTRAINT PK_tasks
        PRIMARY KEY (id),

    CONSTRAINT FK_tasks_project
        FOREIGN KEY (project_id)
        REFERENCES dbo.project(id),

    CONSTRAINT CK_tasks_priority
        CHECK (
            priority IN (
                'LOW',
                'MEDIUM',
                'HIGH'
            )
        ),

    CONSTRAINT CK_tasks_status
        CHECK (
            status IN (
                'OPEN',
                'IN_PROGRESS',
                'COMPLETED'
            )
        )
);