ALTER TABLE notification_subscription_table
    ADD CONSTRAINT unique_user_subscription UNIQUE (user_id, subscription_token);
