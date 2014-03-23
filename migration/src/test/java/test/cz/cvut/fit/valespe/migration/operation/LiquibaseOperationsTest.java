package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.impl.LiquibaseOperationsImpl;
import org.junit.Test;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.StringBufferInputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class LiquibaseOperationsTest {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String MOCKED_MIGRATION_PATH = "migration-path";
    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";

    private static final String TABLE = "table-name";
    private static final String USER = "user";
    private static final String ID = "id";
    private static final String EXPECTED_MIGRATION_FILE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";
    private static final String MIGRATION_FILE_BEFORE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";
    private static final String MIGRATION_FILE_AFTER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
            "\n" +
            "<changeSet author=\"" + USER + "\" id=\"" + ID + "\">\n" +
            "        <createTable tableName=\"table-name\"/>\n" +
            "        <addColumn tableName=\"table-name\">\n" +
            "            <column name=\"column-name\" type=\"column-type\"/>\n" +
            "        </addColumn>\n" +
            "        <addPrimaryKey columnNames=\"column-name\" constraintName=\"PK\" tableName=\"table-name\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>\n";
    public static final String PK_CONSTRAINT_NAME = "PK";


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
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_BEFORE));

        final Element element = liquibaseOperations.addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE);

        assertEquals(TABLE, element.getAttribute("tableName"));
        final Node item = element.getChildNodes().item(0);
        assertEquals(COLUMN_NAME, item.getAttributes().getNamedItem("name").getTextContent());
        assertEquals(COLUMN_TYPE, item.getAttributes().getNamedItem("type").getTextContent());
    }

    @Test
    public void addPrimaryKey() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_BEFORE));

        final Element element = liquibaseOperations.addPrimaryKey(Arrays.asList("a", "b", "c"), TABLE, PK_CONSTRAINT_NAME);

        assertEquals(TABLE, element.getAttribute("tableName"));
        assertEquals(PK_CONSTRAINT_NAME, element.getAttribute("constraintName"));
        assertEquals("a, b, c", element.getAttribute("columnNames"));
    }

    @Test
    public void addTable() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_BEFORE));

        final Element element = liquibaseOperations.createTable(TABLE);

        assertEquals(TABLE, element.getAttribute("tableName"));
    }

    @Test
    public void dropTable() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_BEFORE));

        final Element element = liquibaseOperations.dropTable(TABLE, false);

        assertEquals(TABLE, element.getAttribute("tableName"));
        assertEquals("false", element.getAttribute("cascadeConstraints"));
    }

    @Test
    public void dropColumn() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_BEFORE));

        final Element element = liquibaseOperations.dropColumn(TABLE, COLUMN_NAME);

        assertEquals(COLUMN_NAME, element.getAttribute("columnName"));
        assertEquals(TABLE, element.getAttribute("tableName"));
    }

    @Test
    public void createChangeSet() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(
                new StringBufferInputStream(MIGRATION_FILE_BEFORE),
                new StringBufferInputStream(MIGRATION_FILE_BEFORE),
                new StringBufferInputStream(MIGRATION_FILE_BEFORE),
                new StringBufferInputStream(MIGRATION_FILE_BEFORE));

        final Element createTable = liquibaseOperations.createTable(TABLE);
        final Element addColumn = liquibaseOperations.addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE);
        final Element addPrimaryKey = liquibaseOperations.addPrimaryKey(Arrays.asList(COLUMN_NAME), TABLE, PK_CONSTRAINT_NAME);

        liquibaseOperations.createChangeSet(Arrays.asList(createTable, addColumn, addPrimaryKey), USER, ID);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  MIGRATION_FILE_AFTER, false);
    }


}
