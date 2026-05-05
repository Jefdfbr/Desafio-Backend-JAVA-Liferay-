-- Todo List Portlet - Schema
-- Database: PostgreSQL
-- Run this script once on the target database (desafio2)

CREATE TABLE IF NOT EXISTS Todo_Task (
    taskId            BIGINT       PRIMARY KEY,
    userId            BIGINT       NOT NULL,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    completed         BOOLEAN      NOT NULL DEFAULT FALSE,
    imageFileEntryId  BIGINT       NOT NULL DEFAULT 0,
    createdAt         TIMESTAMP    NOT NULL,
    modifiedAt        TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_todo_task_userId
    ON Todo_Task (userId);

CREATE INDEX IF NOT EXISTS idx_todo_task_userId_completed
    ON Todo_Task (userId, completed);

CREATE TABLE IF NOT EXISTS Todo_SubTask (
    subTaskId  BIGINT       PRIMARY KEY,
    taskId     BIGINT       NOT NULL REFERENCES Todo_Task (taskId) ON DELETE CASCADE,
    title      VARCHAR(200) NOT NULL,
    completed  BOOLEAN      NOT NULL DEFAULT FALSE,
    createdAt  TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_todo_subtask_taskId
    ON Todo_SubTask (taskId);
