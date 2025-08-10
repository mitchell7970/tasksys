-- Sample data for development/testing
INSERT INTO tasks (title, description, due_date, status) VALUES
('Setup Development Environment', 'Configure IDE, install JDK 21, setup Maven', '2025-08-12', 'DONE'),
('Design Database Schema', 'Create entity relationship diagram and table structures', '2025-08-15', 'IN_PROGRESS'),
('Implement REST API Endpoints', 'Create controllers for CRUD operations', '2025-08-18', 'TO_DO'),
('Write Unit Tests', 'Comprehensive test coverage for all components', '2025-08-20', 'TO_DO'),
('Configure CI/CD Pipeline', 'Setup GitHub Actions for automated testing and deployment', '2025-08-22', 'TO_DO'),
('Add Input Validation', 'Implement request validation and error handling', '2025-08-25', 'TO_DO'),
('Create API Documentation', 'Setup Swagger/OpenAPI documentation', '2025-08-28', 'TO_DO'),
('Implement Logging', 'Add structured logging throughout application', '2025-08-30', 'TO_DO'),
('Setup Monitoring', 'Configure application metrics and health checks', '2025-09-02', 'TO_DO'),
('Deploy to Production', 'Deploy application to production environment', '2025-09-05', 'TO_DO');