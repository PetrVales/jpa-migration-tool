package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.MovePropertyOperations;
import cz.cvut.fit.valespe.migration.operation.impl.MovePropertyOperationsImpl;
import org.junit.Test;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import java.io.StringBufferInputStream;

import static org.mockito.Mockito.*;

public class MovePropertyOperationsTest {

    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";
    private static final String FROM_TABLE = "from-table";
    private static final String FROM_SCHEMA = "from-schema";
    private static final String FROM_CATALOG = "from-catalog";
    private static final String TO_TABLE = "to-table";
    private static final String TO_SCHEMA = "to-schema";
    private static final String TO_CATALOG = "to-catalog";
    private static final String MIGRATION_XML = "migration.xml";
    private static final String MOCKED_MIGRATION_PATH = "migration-path";
    private static final String MIGRATION_FILE_CONTENT_BEFORE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";
    private static final String MIGRATION_FILE_CONTENT_AFTER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "<changeSet>\n" +
                    "        <addColumn catalogName=\"to-catalog\" schemaName=\"to-schema\" tableName=\"to-table\">\n" +
                    "            <column name=\"column-name\" type=\"column-type\"/>\n" +
                    "        </addColumn>\n" +
                    "        <dropColumn catalogName=\"from-catalog\" columnName=\"column-name\" schemaName=\"from-schema\" tableName=\"from-table\"/>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";

    private final PathResolver pathResolver = mock(PathResolver.class);
    private final FileManager fileManager = mock(FileManager.class);
    private final MovePropertyOperations movePropertyOperations = new MovePropertyOperationsImpl(pathResolver, fileManager);

    @Test
    public void moveColumn() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_CONTENT_BEFORE));

        movePropertyOperations.moveColumn(COLUMN_NAME, COLUMN_TYPE, FROM_TABLE, FROM_SCHEMA, FROM_CATALOG, TO_TABLE, TO_SCHEMA, TO_CATALOG);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  MIGRATION_FILE_CONTENT_AFTER, false);
    }

}
