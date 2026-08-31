/*
=========================================================
V4__add_task_assignee.sql

Adds optional task assignment support.

A task may be:
- assigned to an application user
- left unassigned

Existing tasks remain valid because assignee_id is nullable.
=========================================================
*/

ALTER TABLE dbo.tasks
ADD assignee_id BIGINT NULL;

ALTER TABLE dbo.tasks
ADD CONSTRAINT FK_tasks_assignee
FOREIGN KEY (assignee_id)
REFERENCES dbo.app_users(id);