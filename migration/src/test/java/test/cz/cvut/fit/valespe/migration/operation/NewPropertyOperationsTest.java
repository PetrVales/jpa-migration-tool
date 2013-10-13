package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.NewPropertyOperations;
import cz.cvut.fit.valespe.migration.operation.impl.NewPropertyOperationsImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import java.io.StringBufferInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class NewPropertyOperationsTest {

    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final JavaType PROPERTY_TYPE = new JavaType("test.Type");
    private static final String PHYSICAL_TYPE_IDENTIFIER = "MID:" + PhysicalTypeIdentifier.class.getName() + "#?";
    private static final String MIGRATION_XML = "migration.xml";
    private static final String MOCKED_MIGRATION_PATH = "migration-path";
    private static final LogicalPath SRC_MAIN = mock(LogicalPath.class);
    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";
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
                    "        <addColumn catalogName=\"catalog-name\" schemaName=\"schema-name\" tableName=\"table-name\">\n" +
                    "            <column name=\"column-name\" type=\"column-type\"/>\n" +
                    "        </addColumn>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";

    private final TypeManagementService typeManagementService = mock(TypeManagementService.class);
    private final TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private final PathResolver pathResolver = mock(PathResolver.class);
    private final FileManager fileManager = mock(FileManager.class);
    private final NewPropertyOperations newPropertyOperations = new NewPropertyOperationsImpl(typeManagementService, typeLocationService, pathResolver, fileManager);

    @Test
    public void generatesMigrationRecord() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_CONTENT_BEFORE));

        newPropertyOperations.createColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME, COLUMN_TYPE);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  MIGRATION_FILE_CONTENT_AFTER, false);
    }

    @Test
    public void createClass() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);
        when(pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA)).thenReturn(SRC_MAIN);

        newPropertyOperations.addFieldToClass(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, classOrInterfaceTypeDetails);

        ArgumentCaptor<FieldMetadata> argument = ArgumentCaptor.forClass(FieldMetadata.class);
        verify(typeManagementService, times(1)).addField(argument.capture());

        assertEquals(PROPERTY.getSymbolName(), argument.getValue().getFieldName().getSymbolName());
        assertEquals(PROPERTY_TYPE.getSimpleTypeName(), argument.getValue().getFieldType().getSimpleTypeName());
        assertFalse(argument.getValue().getAnnotations().isEmpty());
    }

}
