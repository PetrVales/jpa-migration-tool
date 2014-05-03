package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.model.JavaType;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Provides functionality for generating Liquibase migration changesets.
 */
public interface LiquibaseOperations {

    boolean doesMigrationFileExist();
    void createMigrationFile();
    void createChangeSet(List<Element> elements, String author, String id);

    Element createTable(String table);
    Element dropTable(String table, boolean cascade);
    Element addColumn(String table, String columnName, String columnType);
    Element dropColumn(String table, String columnName);
    Element addPrimaryKey(List<String> columnName, String tableName, String constraintName);
    Element addForeignKey(String table, String columnName, String referencedTable, String referencedColumn, String s);
    Element copyColumnData(String tableFrom, String tableTo, String columnName, String query);
    Element mergeTables(String target, String tableA, String tableB, List<String> columns, String query);
    Element copyData(String origin, String target, List<String> columns, String query);
    Element sql(String query);

    Element introduceParent(String target, String parent);

}
