
-- Init DB
CREATE DATABASE chatdb;
USE chatdb;

CREATE TABLE user_account (
    user_id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(30) UNIQUE NOT NULL,
    user_password VARCHAR(255) NOT NULL
);

CREATE TABLE chat_group (
    group_id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36) NOT NULL,
    group_name VARCHAR(50) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_userid FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);

INSERT INTO user_account (username, user_password)
VALUES 
('nhien', 'anhnhienvodichh'),
('john_doe', '123456'),
('jane_smith', 'password'),
('michael_jackson', 'qwerty'),
('emma_watson', 'letmein'),
('robert_downey', 'abc123'),
('chris_evans', '123123'),
('scarlett_johansson', 'iloveyou'),
('mark_zuckerberg', 'admin'),
('elon_musk', 'welcome'),
('bill_gates', 'monkey'),
('steve_jobs', 'football'),
('jeff_bezos', 'dragon'),
('larry_page', 'sunshine'),
('sergey_brin', 'princess'),
('tim_cook', 'password1'),
('jack_dorsey', '123qwe'),
('sundar_pichai', 'baseball'),
('satya_nadella', 'shadow'),
('linus_torvalds', '123abc'),
('richard_stallman', 'hunter2');

-- Create Group 1 (Tech Enthusiasts) with John Doe & Elon Musk
INSERT INTO chat_group (user_id, group_name) VALUES
((SELECT user_id FROM user_account WHERE username = 'john_doe'), 'Tech Enthusiasts'),
((SELECT user_id FROM user_account WHERE username = 'elon_musk'), 'Tech Enthusiasts');

-- Create Group 2 (Movie Lovers) with Scarlett Johansson & Robert Downey
INSERT INTO chat_group (user_id, group_name) VALUES
((SELECT user_id FROM user_account WHERE username = 'scarlett_johansson'), 'Movie Lovers'),
((SELECT user_id FROM user_account WHERE username = 'robert_downey'), 'Movie Lovers');

-- Create Group 3 (Billionaire Club) with Bill Gates, Jeff Bezos, and Elon Musk
INSERT INTO chat_group (user_id, group_name) VALUES
((SELECT user_id FROM user_account WHERE username = 'bill_gates'), 'Billionaire Club'),
((SELECT user_id FROM user_account WHERE username = 'jeff_bezos'), 'Billionaire Club'),
((SELECT user_id FROM user_account WHERE username = 'elon_musk'), 'Billionaire Club');