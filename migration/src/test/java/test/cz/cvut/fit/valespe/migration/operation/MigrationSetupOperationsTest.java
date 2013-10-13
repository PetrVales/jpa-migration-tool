package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.impl.MigrationSetupOperationsImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MigrationSetupOperationsTest {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String MOCKED_MIGRATION_PATH = "migration-path";
    private static final String EXPECTED_MIGRATION_FILE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                    "\n" +
                    "</databaseChangeLog>\n";

    private PathResolver pathResolver = mock(PathResolver.class);
    private FileManager fileManager = mock(FileManager.class);
    private MigrationSetupOperationsImpl operations = new MigrationSetupOperationsImpl(pathResolver, fileManager);

    @Test
    public void doesMigrationFileExistTest() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.exists(MOCKED_MIGRATION_PATH)).thenReturn(true);

        assertTrue(operations.doesMigrationFileExist());
    }

    @Test
    public void createMigrationFileTest() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);

        operations.createMigrationFile();

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH, EXPECTED_MIGRATION_FILE, false);
    }

}
