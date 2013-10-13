package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.RemoveClassOperations;
import cz.cvut.fit.valespe.migration.operation.impl.RemoveClassOperationsImpl;
import org.junit.Test;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

import java.io.StringBufferInputStream;

import static org.mockito.Mockito.*;

public class RemoveClassOperationsTest {

    private static final JavaType CLASS_TYPE = new JavaType("test.Class");
    private static final String CLASS_PATH = "class-path";
    private static final String MIGRATION_XML = "migration.xml";
    private static final String MOCKED_MIGRATION_PATH = "migration-path";
    private static final String TABLE = "table-name";
    private static final String SCHEMA= "schema-name";
    private static final String CATALOG = "catalog-name";
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
                    "        <dropTable cascadeConstraints=\"false\" catalogName=\"catalog-name\" schemaName=\"schema-name\" tableName=\"table-name\"/>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";

    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private PathResolver pathResolver = mock(PathResolver.class);
    private FileManager fileManager = mock(FileManager.class);
    private TypeManagementService typeManagementService = mock(TypeManagementService.class);
    private RemoveClassOperations removeClassOperations =
            new RemoveClassOperationsImpl(projectOperations, pathResolver, fileManager, typeManagementService);

    @Test
    public void removesClass() {
        when(pathResolver.getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, CLASS_TYPE)).thenReturn(CLASS_PATH);

        removeClassOperations.removeClass(CLASS_TYPE);

        verify(fileManager, times(1)).delete(CLASS_PATH);
    }

    @Test
    public void dropTable() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_CONTENT_BEFORE));

        removeClassOperations.dropTable(TABLE, SCHEMA, CATALOG, false);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  MIGRATION_FILE_CONTENT_AFTER, false);
    }

}
