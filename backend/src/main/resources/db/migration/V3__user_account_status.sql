ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN deactivated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN deactivated_by UUID REFERENCES users(id);

ALTER TABLE users ADD CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'));
ALTER TABLE users ADD CONSTRAINT ck_users_deactivation CHECK (
    (status = 'ACTIVE' AND deactivated_at IS NULL AND deactivated_by IS NULL)
    OR
    (status = 'INACTIVE' AND deactivated_at IS NOT NULL AND deactivated_by IS NOT NULL)
);

CREATE INDEX ix_users_status ON users(status);
