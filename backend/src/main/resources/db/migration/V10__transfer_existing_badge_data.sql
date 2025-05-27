-- Transfer existing badge assignments to the new user_badges table with current timestamp
INSERT INTO user_badges (user_id, badge_id, date_obtained)
SELECT DISTINCT u.user_id, u.badge_id, NOW()
FROM (
    SELECT user_id, badge_id 
    FROM user_badges_old
) u
ON CONFLICT (user_id, badge_id) DO NOTHING;
