ALTER TABLE user_table
ADD CONSTRAINT unique_extId UNIQUE (extId),
ADD CONSTRAINT unique_email UNIQUE (email);
