CREATE EXTENSION IF NOT EXISTS pgcrypto;

WITH u AS (
    INSERT INTO users (name, date_of_birth, password) VALUES
        ('Alice Ivanova', DATE '1993-05-01', crypt('password123', gen_salt('bf', 10))),
        ('Bob Petrov',    DATE '1990-03-15', crypt('password123', gen_salt('bf', 10)))
    RETURNING id, name
)
INSERT INTO account (user_id, balance, initial_balance)
SELECT id, CASE name WHEN 'Alice Ivanova' THEN 100.00 ELSE 200.00 END,
          CASE name WHEN 'Alice Ivanova' THEN 100.00 ELSE 200.00 END
FROM u;

INSERT INTO email_data (user_id, email)
SELECT id, CASE name WHEN 'Alice Ivanova' THEN 'alice@example.com' ELSE 'bob@example.com' END
FROM users WHERE name IN ('Alice Ivanova', 'Bob Petrov');

INSERT INTO phone_data (user_id, phone)
SELECT id, CASE name WHEN 'Alice Ivanova' THEN '79207865432' ELSE '79207865433' END
FROM users WHERE name IN ('Alice Ivanova', 'Bob Petrov');
