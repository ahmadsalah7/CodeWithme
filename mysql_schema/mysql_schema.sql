CREATE DATABASE IF NOT EXISTS code_editor_db;

USE code_editor_db;

CREATE TABLE IF NOT EXISTS user (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255),            
  provider_type VARCHAR(20),            
  provider_id VARCHAR(100) UNIQUE  
);

CREATE TABLE userprojects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        
    user_email VARCHAR(100),            
    project_id VARCHAR(100),            
    role ENUM('OWNER', 'EDITOR', 'VIEWER'),  
    UNIQUE (user_email, project_id),           
    FOREIGN KEY (user_email) REFERENCES user(email) ON DELETE CASCADE -- for consistency
);

GRANT ALL PRIVILEGES ON code_editor_db.* TO 'admin1'@'%';
