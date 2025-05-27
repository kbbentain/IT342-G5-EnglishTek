-- Drop the old user_badges table if it exists
DROP TABLE IF EXISTS user_badges;

-- Create the new user_badges table with date_obtained
CREATE TABLE user_badges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    badge_id BIGINT NOT NULL REFERENCES badges(id),
    date_obtained TIMESTAMP NOT NULL,
    UNIQUE(user_id, badge_id)
);
