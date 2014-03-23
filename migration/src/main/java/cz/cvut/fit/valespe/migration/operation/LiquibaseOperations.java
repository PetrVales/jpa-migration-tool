package cz.cvut.fit.valespe.migration.operation;

import org.w3c.dom.Element;

import java.util.List;

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

    Element addColumn(String table, String columnName, String columnType);
    Element createTable(String table);

    Element addPrimaryKey(List<String> columnName, String tableName, String constraintName);

    /**
     * Create drop table record in migration.xml
     */
    Element dropTable(String table, boolean cascade);

    /**
     * Create dropColumn record in migration.xml
     * @param table
     * @param columnName
     */
    Element dropColumn(String table, String columnName);

    void createChangeSet(List<Element> elements, String user, String id);

}
