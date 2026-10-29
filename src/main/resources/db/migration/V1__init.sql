CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(500) NOT NULL,
    date_of_birth DATE         NOT NULL,
    password      VARCHAR(500) NOT NULL
);

CREATE TABLE account (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT         NOT NULL UNIQUE REFERENCES users (id),
    balance         NUMERIC(19, 2) NOT NULL CONSTRAINT balance_non_negative CHECK (balance >= 0),
    initial_balance NUMERIC(19, 2) NOT NULL CHECK (initial_balance >= 0)
);

CREATE TABLE email_data (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT       NOT NULL REFERENCES users (id),
    email   VARCHAR(200) NOT NULL UNIQUE
);
CREATE INDEX idx_email_user ON email_data (user_id);

CREATE TABLE phone_data (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT      NOT NULL REFERENCES users (id),
    phone   VARCHAR(13) NOT NULL UNIQUE
);
CREATE INDEX idx_phone_user ON phone_data (user_id);
CREATE INDEX idx_users_name ON users (name varchar_pattern_ops);
CREATE INDEX idx_users_dob ON users (date_of_birth);
