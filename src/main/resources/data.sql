-- Create test users first
-- Password: password123 (BCrypt encoded)
INSERT INTO users (username, email, password, created_at, enabled) VALUES
('john_doe', 'john@example.com', '$2a$10$vsVI6ohWJU2hNXgn4vOBYO2dKsPHPW4XUhA2hHHq2e8LiFQpZcVMa', '2025-08-01 10:00:00', true),
('jane_smith', 'jane@example.com', '$2a$10$vsVI6ohWJU2hNXgn4vOBYO2dKsPHPW4XUhA2hHHq2e8LiFQpZcVMa', '2025-08-01 11:00:00', true),
('admin', 'admin@example.com', '$2a$10$vsVI6ohWJU2hNXgn4vOBYO2dKsPHPW4XUhA2hHHq2e8LiFQpZcVMa', '2025-08-01 09:00:00', true);

-- Sample tasks for john_doe (user_id = 1)
INSERT INTO tasks (title, description, due_date, status, user_id) VALUES
('Setup Development Environment', 'Configure IDE, install JDK 21, setup Maven', '2025-08-12', 'DONE', 1),
('Design Database Schema', 'Create entity relationship diagram and table structures', '2025-08-15', 'IN_PROGRESS', 1),
('Implement REST API Endpoints', 'Create controllers for CRUD operations', '2025-08-18', 'TO_DO', 1),
('Write Unit Tests', 'Comprehensive test coverage for all components', '2025-08-20', 'TO_DO', 1),
('Configure CI/CD Pipeline', 'Setup GitHub Actions for automated testing and deployment', '2025-08-22', 'TO_DO', 1);

-- Sample tasks for jane_smith (user_id = 2)
INSERT INTO tasks (title, description, due_date, status, user_id) VALUES
('Create UI Mockups', 'Design user interface mockups for task management system', '2025-08-14', 'DONE', 2),
('Frontend Development', 'Implement React frontend components', '2025-08-17', 'IN_PROGRESS', 2),
('User Experience Testing', 'Conduct usability testing with target users', '2025-08-19', 'TO_DO', 2),
('Documentation Writing', 'Write user documentation and help guides', '2025-08-21', 'TO_DO', 2);

-- Sample tasks for admin (user_id = 3)
INSERT INTO tasks (title, description, due_date, status, user_id) VALUES
('System Monitoring Setup', 'Configure application monitoring and alerting', '2025-08-16', 'IN_PROGRESS', 3),
('Security Audit', 'Perform comprehensive security audit', '2025-08-23', 'TO_DO', 3),
('Performance Optimization', 'Optimize database queries and API response times', '2025-08-25', 'TO_DO', 3);