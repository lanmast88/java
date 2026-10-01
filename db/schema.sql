CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    last_name VARCHAR(60) NOT NULL,
    first_name VARCHAR(60) NOT NULL,
    phone VARCHAR(20) NOT NULL
);

CREATE TABLE policies (
    id SERIAL PRIMARY KEY,
    number VARCHAR(20) NOT NULL UNIQUE,
    client_id INT NOT NULL REFERENCES clients (id),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    insured_sum NUMERIC(12, 2) NOT NULL CHECK (insured_sum > 0),
    premium NUMERIC(12, 2) NOT NULL CHECK (premium >= 0),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CHECK (end_date > start_date)
);

CREATE TABLE claims (
    id SERIAL PRIMARY KEY,
    policy_id INT NOT NULL REFERENCES policies (id) ON DELETE CASCADE,
    event_date DATE NOT NULL,
    submitted_at DATE NOT NULL DEFAULT current_date,
    description TEXT NOT NULL,
    claimed_amount NUMERIC(12, 2) NOT NULL CHECK (claimed_amount > 0),
    payout NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (payout >= 0),
    status VARCHAR(20) NOT NULL,
    CHECK (submitted_at >= event_date)
);

CREATE INDEX idx_policies_client_id ON policies (client_id);

CREATE INDEX idx_claims_policy_id ON claims (policy_id);
