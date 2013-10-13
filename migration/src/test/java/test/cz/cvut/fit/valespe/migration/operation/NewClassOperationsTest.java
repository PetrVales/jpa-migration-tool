package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.NewClassOperations;
import cz.cvut.fit.valespe.migration.operation.impl.NewClassOperationsImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import java.io.StringBufferInputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NewClassOperationsTest {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String MOCKED_MIGRATION_PATH = "migration-path";
    private static final LogicalPath SRC_MAIN = mock(LogicalPath.class);
    private static final JavaType CLASS = new JavaType("className");
    private static final String ENTITY = "entity-name";
    private static final String TABLE = "table-name";
    private static final String SCHEMA= "schema-name";
    private static final String CATALOG = "catalog-name";
    private static final String TABLESPACE = "tablespace";
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
                    "        <createTable catalogName=\"catalog-name\" schemaName=\"schema-name\" tableName=\"table-name\" tablespace=\"tablespace\"/>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";

    private final PathResolver pathResolver = mock(PathResolver.class);
    private final FileManager fileManager = mock(FileManager.class);
    private final TypeManagementService typeManagementService = mock(TypeManagementService.class);
    private final NewClassOperations newclassOperations = new NewClassOperationsImpl(pathResolver, fileManager, typeManagementService);

    @Test
    public void generatesMigrationRecord() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_CONTENT_BEFORE));

        newclassOperations.createTable(TABLE, SCHEMA, CATALOG, TABLESPACE);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  MIGRATION_FILE_CONTENT_AFTER, false);
    }

    @Test
    public void createClass() {
        when(pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA)).thenReturn(SRC_MAIN);

        newclassOperations.createEntity(CLASS, ENTITY, TABLE, SCHEMA, CATALOG);

        ArgumentCaptor<ClassOrInterfaceTypeDetails> argument = ArgumentCaptor.forClass(ClassOrInterfaceTypeDetails.class);
        verify(typeManagementService, times(1)).createOrUpdateTypeOnDisk(argument.capture());

        assertFalse(argument.getValue().isAbstract());
        assertEquals(argument.getValue().getName(), CLASS);
        assertTrue(argument.getValue().getDeclaredFields().isEmpty());
        assertFalse(argument.getValue().getAnnotations().isEmpty());
    }


}
