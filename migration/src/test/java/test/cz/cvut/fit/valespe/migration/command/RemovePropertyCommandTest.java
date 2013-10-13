package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.command.RemovePropertyCommands;
import cz.cvut.fit.valespe.migration.operation.MigrationSetupOperations;
import cz.cvut.fit.valespe.migration.operation.RemovePropertyOperations;
import org.junit.Test;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class RemovePropertyCommandTest {

    private static final JavaType CLASS = new JavaType("test.Class");
    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final String COLUMN_NAME = "column-name";
    private static final String TABLE = "table";
    private static final String SCHEMA = "schema";
    private static final String CATALOG = "catalog";

    private RemovePropertyOperations removePropertyOperations = mock(RemovePropertyOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private MigrationSetupOperations migrationSetupOperations = mock(MigrationSetupOperations.class);
    private TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private RemovePropertyCommands removePropertyCommands = new RemovePropertyCommands(removePropertyOperations, projectOperations, migrationSetupOperations, typeLocationService);

    @Test
    public void commandNewClassIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(removePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandNewClassIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(removePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandNewClassIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(removePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandAddNewPropertyToClassAndGeneratesMigrationChangeSet() {
        AnnotationAttributeValue tableMock = mock(AnnotationAttributeValue.class);
        when(tableMock.getValue()).thenReturn(TABLE);
        AnnotationAttributeValue schemaMock = mock(AnnotationAttributeValue.class);
        when(schemaMock.getValue()).thenReturn(SCHEMA);
        AnnotationAttributeValue catalogMock = mock(AnnotationAttributeValue.class);
        when(catalogMock.getValue()).thenReturn(CATALOG);
        AnnotationMetadata migrationEntityAnnotationMetadata = mock(AnnotationMetadata.class);
        when(migrationEntityAnnotationMetadata.getAttribute("table")).thenReturn(tableMock);
        when(migrationEntityAnnotationMetadata.getAttribute("schema")).thenReturn(schemaMock);
        when(migrationEntityAnnotationMetadata.getAttribute("catalog")).thenReturn(catalogMock);
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getAnnotation(new JavaType(MigrationEntity.class.getName()))).thenReturn(migrationEntityAnnotationMetadata);
        AnnotationAttributeValue columnMock = mock(AnnotationAttributeValue.class);
        when(columnMock.getValue()).thenReturn(COLUMN_NAME);
        AnnotationMetadata columnAnnotationMetadata = mock(AnnotationMetadata.class);
        when(columnAnnotationMetadata.getAttribute("name")).thenReturn(columnMock);
        FieldMetadata declaredField = mock(FieldMetadata.class);
        when(declaredField.getAnnotation( new JavaType("javax.persistence.Column"))).thenReturn(columnAnnotationMetadata);
        when(classOrInterfaceTypeDetails.getDeclaredField(PROPERTY)).thenReturn(declaredField);
        when(typeLocationService.getTypeDetails(CLASS)).thenReturn(classOrInterfaceTypeDetails);

        removePropertyCommands.removeProperty(CLASS, PROPERTY);

        verify(removePropertyOperations, times(1)).removeFieldFromClass(PROPERTY, classOrInterfaceTypeDetails);
        verify(removePropertyOperations, times(1)).dropColumn(TABLE, SCHEMA, CATALOG, COLUMN_NAME);
    }

}
