package com.example.archetype;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DynamicClassGeneratorTest {

    private static final Path OUTPUT_FILE = Path.of("target/generated-test-sources/GreetingFromDb.java");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(OUTPUT_FILE);
    }

    @Test
    void shouldGenerateJavaClassUsingCodePropertyAndDatabaseValues() throws IOException {
        jdbcTemplate.execute("DROP TABLE IF EXISTS ARCHETYPE_TEMPLATE");
        jdbcTemplate.execute("CREATE TABLE ARCHETYPE_TEMPLATE (CODE VARCHAR(40), CLASS_NAME VARCHAR(80), MESSAGE VARCHAR(255))");
        jdbcTemplate.update(
                "INSERT INTO ARCHETYPE_TEMPLATE (CODE, CLASS_NAME, MESSAGE) VALUES (?, ?, ?)",
                "ABC123",
                "GreetingFromDb",
                "Hello from DB2 archetype"
        );

        // Trigger a second call explicitly to use freshly inserted test data in this method.
        new DynamicClassGenerator(jdbcTemplate, "ABC123", "target/generated-test-sources",
                "SELECT CLASS_NAME, MESSAGE FROM ARCHETYPE_TEMPLATE WHERE CODE = ?")
                .generateClassFromDatabase();

        assertThat(Files.exists(OUTPUT_FILE)).isTrue();
        String content = Files.readString(OUTPUT_FILE);
        assertThat(content).contains("public class GreetingFromDb");
        assertThat(content).contains("return \"Hello from DB2 archetype\"");
    }
}
