/*
=========================================================
V2__align_existing_schema.sql

Align the existing FullStackDemo SQL Server schema
with the current JPA entity model.

The database was originally managed by Hibernate.
Flyway owns schema changes going forward.
=========================================================
*/

---------------------------------------------------------
-- app_users.name
---------------------------------------------------------

ALTER TABLE dbo.app_users
ALTER COLUMN name VARCHAR(255) NOT NULL;


---------------------------------------------------------
-- app_users.email
--
-- uk_app_users_email is a UNIQUE KEY constraint.
-- SQL Server requires the constraint to be dropped
-- before changing the underlying column.
---------------------------------------------------------

ALTER TABLE dbo.app_users
DROP CONSTRAINT uk_app_users_email;

ALTER TABLE dbo.app_users
ALTER COLUMN email VARCHAR(255) NOT NULL;

ALTER TABLE dbo.app_users
ADD CONSTRAINT uk_app_users_email UNIQUE (email);


---------------------------------------------------------
-- app_users.role
---------------------------------------------------------

ALTER TABLE dbo.app_users
ALTER COLUMN role VARCHAR(255) NOT NULL;


---------------------------------------------------------
-- tasks
---------------------------------------------------------

ALTER TABLE dbo.tasks
ALTER COLUMN title VARCHAR(200) NOT NULL;

ALTER TABLE dbo.tasks
ALTER COLUMN status VARCHAR(30) NOT NULL;

ALTER TABLE dbo.tasks
ALTER COLUMN priority VARCHAR(30) NOT NULL;