package cz.cvut.fit.valespe.migration.operation;

/**
 * Provides functionality for generating Liquibase migration changesets.
 */
public interface LiquibaseOperations {

    /**
     * Indicate commands should be available
     *
     * @return true when migration.xml exists
     */
    boolean doesMigrationFileExist();

    /**
     * Creates migration.xml in MAIN_RESOURCES folder
     */
    void createMigrationFile();

    String createNewChangeSet(String user);

    void createColumn(String table, String schema, String catalog, String columnName, String columnType);
    void createTable(String table, String schema, String catalog, String tablespace);

    /**
     * Create drop table record in migration.xml
     */
    void dropTable(String table, String schema, String catalog, boolean cascade);

    /**
     * Create dropColumn record in migration.xml
     * @param table
     * @param schema
     * @param catalog
     * @param columnName
     */
    void dropColumn(String table, String schema, String catalog, String columnName);

}
