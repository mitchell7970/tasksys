package dev.tasksys;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TasksysApplicationTest {

    @Test
    void contextLoads() { /* Application context should load without issues. */ }
}