package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.RemovePropertyOperations;
import cz.cvut.fit.valespe.migration.operation.impl.RemovePropertyOperationsImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RemovePropertyOperationsTest {

    private static final JavaType CLASS = new JavaType("test.Class");
    private static final PhysicalTypeCategory PHYSICAL_TYPE_CATEGORY = PhysicalTypeCategory.CLASS;
    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final JavaSymbolName PROPERTY_NOT_TO_REMOVE = new JavaSymbolName("propertyNotToRemove");
    private static final JavaType PROPERTY_TYPE = new JavaType("test.Type");
    private static final String PHYSICAL_TYPE_IDENTIFIER = "MID:" + PhysicalTypeIdentifier.class.getName() + "#?";
    private static final String MIGRATION_XML = "migration.xml";
    private static final String MOCKED_MIGRATION_PATH = "migration-path";
    private static final String TABLE = "table-name";
    private static final String SCHEMA= "schema-name";
    private static final String CATALOG = "catalog-name";
    private static final String COLUMN_NAME = "column-name";
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
                    "        <dropColumn catalogName=\"catalog-name\" columnName=\"column-name\" schemaName=\"schema-name\" tableName=\"table-name\"/>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>\n";

    private TypeManagementService typeManagementService = mock(TypeManagementService.class);
    private FileManager fileManager = mock(FileManager.class);
    private PathResolver pathResolver = mock(PathResolver.class);
    private RemovePropertyOperations removePropertyOperations = new RemovePropertyOperationsImpl(typeManagementService, fileManager, pathResolver);

    @Test
    public void removeFieldFromClass() {
        List fields = new ArrayList<FieldMetadata>();
        final FieldMetadata fieldMetadata1 = mock(FieldMetadata.class);
        when(fieldMetadata1.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);
        when(fieldMetadata1.getFieldName()).thenReturn(PROPERTY);
        when(fieldMetadata1.getFieldType()).thenReturn(PROPERTY_TYPE);
        final FieldMetadata fieldMetadata2 = mock(FieldMetadata.class);
        when(fieldMetadata2.getFieldName()).thenReturn(PROPERTY_NOT_TO_REMOVE);
        when(fieldMetadata2.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);
        when(fieldMetadata2.getFieldType()).thenReturn(PROPERTY_TYPE);
        fields.add(fieldMetadata1);
        fields.add(fieldMetadata2);
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);
        when(classOrInterfaceTypeDetails.getDeclaredFields()).thenReturn(fields);
        when(classOrInterfaceTypeDetails.getName()).thenReturn(CLASS);
        when(classOrInterfaceTypeDetails.getPhysicalTypeCategory()).thenReturn(PHYSICAL_TYPE_CATEGORY);

        removePropertyOperations.removeFieldFromClass(PROPERTY, classOrInterfaceTypeDetails);

        ArgumentCaptor<ClassOrInterfaceTypeDetails> argument = ArgumentCaptor.forClass(ClassOrInterfaceTypeDetails.class);
        verify(typeManagementService, times(1)).createOrUpdateTypeOnDisk(argument.capture());

        assertEquals(1, argument.getValue().getDeclaredFields().size());
        assertEquals(PROPERTY_NOT_TO_REMOVE, argument.getValue().getDeclaredFields().get(0).getFieldName());
    }

    @Test
    public void dropColumn() {
        when(pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML)).thenReturn(MOCKED_MIGRATION_PATH);
        when(fileManager.getInputStream(MOCKED_MIGRATION_PATH)).thenReturn(new StringBufferInputStream(MIGRATION_FILE_CONTENT_BEFORE));

        removePropertyOperations.dropColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME);

        verify(fileManager, times(1)).createOrUpdateTextFileIfRequired(MOCKED_MIGRATION_PATH,  MIGRATION_FILE_CONTENT_AFTER, false);
    }

}
