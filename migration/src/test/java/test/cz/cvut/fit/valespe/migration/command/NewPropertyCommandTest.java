package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.command.NewPropertyCommands;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.NewPropertyOperations;
import org.junit.Test;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NewPropertyCommandTest {

    private static final JavaType CLASS = new JavaType("test.Class");
    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final JavaType PROPERTY_TYPE = new JavaType("test.Type");
    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";
    private static final String TABLE = "table";
    private static final String SCHEMA = "schema";
    private static final String CATALOG = "catalog";

    private static final JavaSymbolName ID_PROPERTY = new JavaSymbolName("id");
    private static final JavaType ID_PROPERTY_TYPE = new JavaType("java.lang.Long");
    private static final String ID_COLUMN_NAME = "id";
    private static final String ID_COLUMN_TYPE = "bigint";

    private static final JavaType STRING_PROPERTY_TYPE = new JavaType("java.lang.String");
    private static final String STRING_COLUMN_TYPE = "varchar2(255)";

    private static final JavaType INTEGER_PROPERTY_TYPE = new JavaType("java.lang.Integer");
    private static final String INTEGER_COLUMN_TYPE = "integer";

    private static final JavaType BOOLEAN_PROPERTY_TYPE = new JavaType("java.lang.Boolean");
    private static final String BOOLEAN_COLUMN_TYPE = "boolean";

    private NewPropertyOperations newPropertyOperations = mock(NewPropertyOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private NewPropertyCommands newPropertyCommands = new NewPropertyCommands(newPropertyOperations, projectOperations, typeLocationService, liquibaseOperations);

    @Test
    public void commandNewPropertyIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(newPropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandNewPropertyIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(newPropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandNewPropertyIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(newPropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandNewPropertyAddNewPropertyToClassAndGeneratesMigrationChangeSet() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mockClassWithTable();

        newPropertyCommands.newProperty(CLASS, PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, false);

        verify(newPropertyOperations, times(1)).addFieldToClass(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, classOrInterfaceTypeDetails, false);
        verify(liquibaseOperations, times(1)).createColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME, COLUMN_TYPE, false);
    }

    @Test
    public void commandNewPropertyAddNewIdPropertyToClassAndGeneratesMigrationChangeSet() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mockClassWithTable();

        newPropertyCommands.newProperty(CLASS, PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, true);

        verify(newPropertyOperations, times(1)).addFieldToClass(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, classOrInterfaceTypeDetails, true);
        verify(liquibaseOperations, times(1)).createColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME, COLUMN_TYPE, true);
    }

    @Test
    public void addIdCommandTest() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mockClassWithTable();

        newPropertyCommands.addId(CLASS);

        verify(newPropertyOperations, times(1)).addFieldToClass(ID_PROPERTY, ID_PROPERTY_TYPE, ID_COLUMN_NAME, ID_COLUMN_TYPE, classOrInterfaceTypeDetails, true);
        verify(liquibaseOperations, times(1)).createColumn(TABLE, SCHEMA, CATALOG, ID_COLUMN_NAME, ID_COLUMN_TYPE, true);
    }

    @Test
    public void addStringCommandTest() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mockClassWithTable();

        newPropertyCommands.addString(PROPERTY, CLASS, COLUMN_NAME);

        verify(newPropertyOperations, times(1)).addFieldToClass(PROPERTY, STRING_PROPERTY_TYPE, COLUMN_NAME, STRING_COLUMN_TYPE, classOrInterfaceTypeDetails);
        verify(liquibaseOperations, times(1)).createColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME, STRING_COLUMN_TYPE, false);
    }

    @Test
    public void addIntegerCommandTest() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mockClassWithTable();

        newPropertyCommands.addInteger(PROPERTY, CLASS, COLUMN_NAME);

        verify(newPropertyOperations, times(1)).addFieldToClass(PROPERTY, INTEGER_PROPERTY_TYPE, COLUMN_NAME, INTEGER_COLUMN_TYPE, classOrInterfaceTypeDetails);
        verify(liquibaseOperations, times(1)).createColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME, INTEGER_COLUMN_TYPE, false);
    }

    @Test
    public void addBooleanCommandTest() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mockClassWithTable();

        newPropertyCommands.addBoolean(PROPERTY, CLASS, COLUMN_NAME);

        verify(newPropertyOperations, times(1)).addFieldToClass(PROPERTY, BOOLEAN_PROPERTY_TYPE, COLUMN_NAME, BOOLEAN_COLUMN_TYPE, classOrInterfaceTypeDetails);
        verify(liquibaseOperations, times(1)).createColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME, BOOLEAN_COLUMN_TYPE, false);
    }

    private ClassOrInterfaceTypeDetails mockClassWithTable() {
        AnnotationAttributeValue tableMock = mock(AnnotationAttributeValue.class);
        when(tableMock.getValue()).thenReturn(TABLE);
        AnnotationAttributeValue schemaMock = mock(AnnotationAttributeValue.class);
        when(schemaMock.getValue()).thenReturn(SCHEMA);
        AnnotationAttributeValue catalogMock = mock(AnnotationAttributeValue.class);
        when(catalogMock.getValue()).thenReturn(CATALOG);
        AnnotationMetadata annotationMetadata = mock(AnnotationMetadata.class);
        when(annotationMetadata.getAttribute("table")).thenReturn(tableMock);
        when(annotationMetadata.getAttribute("schema")).thenReturn(schemaMock);
        when(annotationMetadata.getAttribute("catalog")).thenReturn(catalogMock);
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getAnnotation(new JavaType(MigrationEntity.class.getName()))).thenReturn(annotationMetadata);
        when(typeLocationService.getTypeDetails(CLASS)).thenReturn(classOrInterfaceTypeDetails);
        return classOrInterfaceTypeDetails;
    }

}
