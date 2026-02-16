-- REMOVE THIS (Postgres doesn't support IF NOT EXISTS for CREATE DATABASE)
-- CREATE DATABASE IF NOT EXISTS hr_management;

-- Add this (needed for gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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
    is_active BOOLEAN NOT NULL DEFAULT true,
    profile_image_url TEXT
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(80) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
-- Employee profile main table (1:1 with users)
CREATE TABLE employee_profiles (
  user_id UUID PRIMARY KEY,
  date_of_birth DATE,
  phone VARCHAR(50),
  address TEXT,
  nrc VARCHAR(100),
  gender VARCHAR(20),

  job_title VARCHAR(100),
  department VARCHAR(100),
  join_date DATE,
  manager_user_id UUID,

  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP,

  CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_profile_manager FOREIGN KEY (manager_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Bank info (1:1 with users)
CREATE TABLE employee_bank_accounts (
  user_id UUID PRIMARY KEY,
  bank_name VARCHAR(100) NOT NULL,
  account_name VARCHAR(100),
  account_number VARCHAR(100) NOT NULL,
  branch VARCHAR(100),

  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP,

  CONSTRAINT fk_bank_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Emergency contacts (1:N)
CREATE TABLE employee_emergency_contacts (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id UUID NOT NULL,
  name VARCHAR(100) NOT NULL,
  relationship VARCHAR(50),
  phone VARCHAR(50) NOT NULL,
  address TEXT,

  created_at TIMESTAMP NOT NULL DEFAULT now(),

  CONSTRAINT fk_emergency_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_emergency_user_id ON employee_emergency_contacts(user_id);
