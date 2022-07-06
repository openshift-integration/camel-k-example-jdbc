Feature: JDBC datasource

  Background:
    Given Camel K resource polling configuration
      | maxAttempts          | 200   |
      | delayBetweenAttempts | 2000  |

  Scenario: Start PostgreSQL
    Given Database init script
    """
    CREATE TABLE IF NOT EXISTS test (data TEXT PRIMARY KEY);

    INSERT INTO test(data) VALUES ('hello'), ('world');
    """
    Then start PostgreSQL container

  Scenario: Create data integrations and verify
    # Create select data integration
    Given Camel K integration property file datasource-test.properties
    When load Camel K integration JDBCSelect.java
    Then Camel K integration jdbcselect should be running

    # Create insert data integration
    Given Camel K integration property file datasource-test.properties
    When load Camel K integration JDBCInsert.java
    Then Camel K integration jdbcinsert should be running

    # Verify select integration to receive db entries (limit 5)
    Then Camel K integration jdbcselect should print [{data=hello}, {data=world}, {data=message #1}, {data=message #2}, {data=message #3}]

  Scenario: Remove Camel K integrations
    Given delete Camel K integration jdbcinsert
    Given delete Camel K integration jdbcselect
