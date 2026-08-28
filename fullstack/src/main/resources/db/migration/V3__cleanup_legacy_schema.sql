/*
=========================================================
V3__cleanup_legacy_schema.sql

Remove legacy schema objects that are no longer mapped
or used by the current application.

Changes:
1. Drop legacy dbo.projects table.
2. Drop obsolete dbo.tasks.completed_date column.
=========================================================
*/


-- =========================================================
-- DROP LEGACY PROJECTS TABLE
-- =========================================================

IF OBJECT_ID('dbo.projects', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.projects;
END;


-- =========================================================
-- DROP OBSOLETE TASK COMPLETED_DATE COLUMN
-- =========================================================

IF COL_LENGTH('dbo.tasks', 'completed_date') IS NOT NULL
BEGIN
    ALTER TABLE dbo.tasks
    DROP COLUMN completed_date;
END;