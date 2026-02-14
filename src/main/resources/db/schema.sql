CREATE DATABASE IF NOT EXISTS hr_management;

CREATE TABLE roles (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

INSERT INTO roles (name, description) values ('ROLE_ADMIN', 'Administrator to handle HR system.');
INSERT INTO roles (name, description) values ('ROLE_MANAGER', 'Manager role responsible for things such as pre approving leave request');
INSERT INTO roles (name, description) values ('ROLE_EMPLOYEE', 'Employees working within organization.');


CREATE TABLE users(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,

    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT_NULL DEFAULT true,
    profile_image_url TEXT
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id INTEGER NOT NULL, -- Role PK က SERIAL မို့ INTEGER သုံးထားသည်
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);