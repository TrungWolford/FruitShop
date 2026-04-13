CREATE TABLE IF NOT EXISTS roles (
    role_id VARCHAR(36) PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id VARCHAR(36) PRIMARY KEY,
    account_name VARCHAR(150) NOT NULL,
    account_phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS account_roles (
    account_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (account_id, role_id),
    CONSTRAINT fk_account_roles_account
        FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_account_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_accounts_phone ON accounts(account_phone);
CREATE INDEX IF NOT EXISTS idx_account_roles_role_id ON account_roles(role_id);
