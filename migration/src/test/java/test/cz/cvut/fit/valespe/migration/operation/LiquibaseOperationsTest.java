package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.impl.LiquibaseOperationsImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import java.io.StringBufferInputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class LiquibaseOperationsTest {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String MOCKED_MIGRATION_PATH = "migration-path";
    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";

    private static final String TABLE = "table-name";
    private static final String SCHEMA= "schema-name";
    private static final String CATALOG = "catalog-name";
    private static final String EXPECTED_MIGRATION_FILE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";
    private static final String ADD_COLUMN_BEFORE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";
    private static final String ADD_COLUMN_AFTER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "<changeSet>\n" +
                    "        <addColumn catalogName=\"catalog-name\" schemaName=\"schema-name\" tableName=\"table-name\">\n" +
                    "            <column name=\"column-name\" type=\"column-type\"/>\n" +
                    "        </addColumn>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";
    private static final String ADD_ID_COLUMN_AFTER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "<changeSet>\n" +
                    "        <addColumn catalogName=\"catalog-name\" schemaName=\"schema-name\" tableName=\"table-name\">\n" +
                    "            <column name=\"column-name\" type=\"column-type\"/>\n" +
                    "        </addColumn>\n" +
                    "        <addPrimaryKey columnNames=\"column-name\" constraintName=\"pk_column-name\" tableName=\"table-name\"/>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";
    private static final String ADD_TABLE_BEFORE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";
    private static final String ADD_TABLE_AFTER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "<changeSet>\n" +
                    "        <createTable tableName=\"table-name\"/>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";
    private static final String DROP_COLUMN_BEFORE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";
    private static final String DROP_COLUMN_AFTER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "<changeSet>\n" +
                    "        <dropColumn catalogName=\"catalog-name\" columnName=\"column-name\" schemaName=\"schema-name\" tableName=\"table-name\"/>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";
    private static final String DROP_TABLE_BEFORE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";
    private static final String DROP_TABLE_AFTER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "<changeSet>\n" +
                    "        <dropTable cascadeConstraints=\"false\" catalogName=\"catalog-name\" schemaName=\"schema-name\" tableName=\"table-name\"/>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";


    private final PathResolver pathResolver = mock(PathResolver.class);
    private final FileManager fileManager = mock(FileManager.class);
    private final LiquibaseOperations liquibaseOperations = new LiquibaseOperationsImpl(pathResolver, fileManager);

    @Test
    public void doesMigrationFileExistTest() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.exists(MOCKED_MIGRATION_PATH)).thenReturn(true);

        assertTrue(liquibaseOperations.doesMigrationFileExist());
    }

    @Test
    public void createMigrationFileTest() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);

        liquibaseOperations.createMigrationFile();

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH, EXPECTED_MIGRATION_FILE, false);
    }

    @Test
    public void addColumn() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(ADD_COLUMN_BEFORE));

        liquibaseOperations.createColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME, COLUMN_TYPE, false);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  ADD_COLUMN_AFTER, false);
    }

    @Test
    public void addIdColumn() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(ADD_COLUMN_BEFORE));

        liquibaseOperations.createColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME, COLUMN_TYPE, true);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  ADD_ID_COLUMN_AFTER, false);
    }

    @Test
    public void addTable() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(ADD_TABLE_BEFORE));

        liquibaseOperations.createTable(TABLE);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  ADD_TABLE_AFTER, false);
    }

    @Test
    public void dropTable() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(DROP_TABLE_BEFORE));

        liquibaseOperations.dropTable(TABLE, SCHEMA, CATALOG, false);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  DROP_TABLE_AFTER, false);
    }

    @Test
    public void dropColumn() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(DROP_COLUMN_BEFORE));

        liquibaseOperations.dropColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  DROP_COLUMN_AFTER, false);
    }


}
