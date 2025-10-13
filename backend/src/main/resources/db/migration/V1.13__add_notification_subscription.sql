CREATE TABLE notification_subscription_table
(
    id                 UUID PRIMARY KEY,
    user_id            UUID REFERENCES user_table (id) ON DELETE CASCADE,
    subscription_token VARCHAR(255) NOT NULL
);

ALTER TABLE user_to_spot_table
    ADD COLUMN withNotification boolean NOT NULL DEFAULT false;
