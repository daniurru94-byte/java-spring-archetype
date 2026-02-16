package com.example.archetype;

import groovy.lang.GroovyShell;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "archetype.auto-generate", havingValue = "true", matchIfMissing = true)
public class DynamicClassGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final String code;
    private final String outputDir;
    private final String query;

    public DynamicClassGenerator(
            JdbcTemplate jdbcTemplate,
            @Value("${archetype.code}") String code,
            @Value("${archetype.output-dir:generated-sources}") String outputDir,
            @Value("${archetype.query:SELECT CLASS_NAME, MESSAGE FROM ARCHETYPE_TEMPLATE WHERE CODE = ?}") String query) {
        this.jdbcTemplate = jdbcTemplate;
        this.code = code;
        this.outputDir = outputDir;
        this.query = query;
    }

    @PostConstruct
    public void generateClassFromDatabase() throws IOException {
        Map<String, Object> row = jdbcTemplate.queryForMap(query, code);

        String className = String.valueOf(row.get("CLASS_NAME"));
        String message = String.valueOf(row.get("MESSAGE"));

        String javaSource = buildJavaSourceViaGroovy(className, message);

        Path targetDir = Path.of(outputDir);
        Files.createDirectories(targetDir);
        Files.writeString(targetDir.resolve(className + ".java"), javaSource);
    }

    private String buildJavaSourceViaGroovy(String className, String message) {
        String escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"");

        String script = """
            def cName = '%s'
            def msg = '%s'
            return """
            public class ${cName} {
                public String message() {
                    return \"${msg}\";
                }
            }
            """
            """.formatted(className, escapedMessage.replace("'", "\\'"));

        return (String) new GroovyShell().evaluate(script);
    }
}
