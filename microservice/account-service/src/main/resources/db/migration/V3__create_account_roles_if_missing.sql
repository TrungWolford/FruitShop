CREATE TABLE IF NOT EXISTS account_roles (
    account_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (account_id, role_id)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_account_roles_account'
          AND table_name = 'account_roles'
    ) THEN
        ALTER TABLE account_roles
            ADD CONSTRAINT fk_account_roles_account
            FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_account_roles_role'
          AND table_name = 'account_roles'
    ) THEN
        ALTER TABLE account_roles
            ADD CONSTRAINT fk_account_roles_role
            FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_account_roles_role_id ON account_roles(role_id);
