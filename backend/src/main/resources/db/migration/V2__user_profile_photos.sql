ALTER TABLE users ADD COLUMN profile_photo_version BIGINT;

CREATE TABLE user_profile_photos (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    content_type VARCHAR(32) NOT NULL,
    content BYTEA NOT NULL,
    size_bytes INTEGER NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
