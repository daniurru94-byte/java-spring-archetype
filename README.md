# java-spring-archetype

Basic Spring Boot archetype with Spring Batch + Groovy-driven Java class generation.

## What it does

At startup (`@PostConstruct`), the app:
1. Reads a property called `archetype.code`.
2. Executes a query against a local DB2 database.
3. Uses Groovy to build a Java source string.
4. Writes the generated class to `archetype.output-dir`.

## Main configuration

```properties
archetype.code=DEFAULT_CODE
archetype.output-dir=generated-sources
archetype.query=SELECT CLASS_NAME, MESSAGE FROM ARCHETYPE_TEMPLATE WHERE CODE = ?
```

## DB2 table example

```sql
CREATE TABLE ARCHETYPE_TEMPLATE (
  CODE VARCHAR(40) NOT NULL,
  CLASS_NAME VARCHAR(80) NOT NULL,
  MESSAGE VARCHAR(255) NOT NULL
);

INSERT INTO ARCHETYPE_TEMPLATE (CODE, CLASS_NAME, MESSAGE)
VALUES ('DEFAULT_CODE', 'GeneratedGreeting', 'Hello from local DB2');
```

## Run tests

```bash
mvn test
```

The test uses H2 in DB2 compatibility mode to validate the flow end-to-end.
