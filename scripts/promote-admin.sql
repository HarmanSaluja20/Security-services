-- Run manually as a trusted database administrator after the target user registers.
-- Replace the email value. This does not create an account or expose admin signup over HTTP.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM app_users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'replace-with-your-email@example.com'
ON CONFLICT DO NOTHING;
