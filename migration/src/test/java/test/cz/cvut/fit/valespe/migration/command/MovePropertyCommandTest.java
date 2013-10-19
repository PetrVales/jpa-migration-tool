package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.command.MovePropertyCommands;
import cz.cvut.fit.valespe.migration.operation.MigrationSetupOperations;
import cz.cvut.fit.valespe.migration.operation.MovePropertyOperations;
import cz.cvut.fit.valespe.migration.operation.NewPropertyOperations;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MovePropertyCommandTest {

    private static final JavaType FROM_CLASS = new JavaType("test.FromClass");
    private static final JavaType TO_CLASS = new JavaType("test.ToClass");
    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final JavaType PROPERTY_TYPE = new JavaType("test.Type");
    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";
    private static final String FROM_TABLE = "from-table";
    private static final String FROM_SCHEMA = "from-schema";
    private static final String FROM_CATALOG = "from-catalog";
    private static final String TO_TABLE = "to-table";
    private static final String TO_SCHEMA = "to-schema";
    private static final String TO_CATALOG = "to-catalog";

    private NewPropertyOperations newpropertyOperations = mock(NewPropertyOperations.class);
    private RemovePropertyOperations removepropertyOperations = mock(RemovePropertyOperations.class);
    private MovePropertyOperations movePropertyOperations = mock(MovePropertyOperations.class);
    private MigrationSetupOperations migrationSetupOperations = mock(MigrationSetupOperations.class);
    private TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private MovePropertyCommands movePropertyCommands = new MovePropertyCommands(newpropertyOperations, removepropertyOperations, movePropertyOperations, migrationSetupOperations, typeLocationService, projectOperations);

    @Test
    public void commandMovePropertyIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(movePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandMovePropertyIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(movePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandMovePropertyIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(movePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandMovePropertyRemovesOldPropertyFromOriginalClass() {
        ClassOrInterfaceTypeDetails fromCLass = mockFromClass();
        ClassOrInterfaceTypeDetails toCLass = mockToClass();

        movePropertyCommands.moveProperty(FROM_CLASS, TO_CLASS, PROPERTY);

        verify(newpropertyOperations, times(1)).addFieldToClass(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, toCLass);
        verify(movePropertyOperations, times(1)).moveColumn(COLUMN_NAME, COLUMN_TYPE, FROM_TABLE, FROM_SCHEMA, FROM_CATALOG, TO_TABLE, TO_SCHEMA, TO_CATALOG);
        verify(removepropertyOperations, times(1)).removeFieldFromClass(PROPERTY, fromCLass);
    }

    private ClassOrInterfaceTypeDetails mockFromClass() {
        AnnotationAttributeValue columnNameMock = mock(AnnotationAttributeValue.class);
        when(columnNameMock.getValue()).thenReturn(COLUMN_NAME);
        AnnotationAttributeValue columnTypeMock = mock(AnnotationAttributeValue.class);
        when(columnTypeMock.getValue()).thenReturn(COLUMN_TYPE);

        AnnotationMetadata fieldAnnotationMetadata = mock(AnnotationMetadata.class);
        when(fieldAnnotationMetadata.getAttribute("name")).thenReturn(columnNameMock);
        when(fieldAnnotationMetadata.getAttribute("columnDefinition")).thenReturn(columnTypeMock);

        FieldMetadata fieldMetadata = mock(FieldMetadata.class);
        when(fieldMetadata.getAnnotation(new JavaType("javax.persistence.Column"))).thenReturn(fieldAnnotationMetadata);
        when(fieldMetadata.getFieldType()).thenReturn(PROPERTY_TYPE);

        AnnotationMetadata classAnnotationMetadata = mock(AnnotationMetadata.class);
        AnnotationAttributeValue tableMock = mock(AnnotationAttributeValue.class);
        when(tableMock.getValue()).thenReturn(FROM_TABLE);
        AnnotationAttributeValue schemaMock = mock(AnnotationAttributeValue.class);
        when(schemaMock.getValue()).thenReturn(FROM_SCHEMA);
        AnnotationAttributeValue catalogMock = mock(AnnotationAttributeValue.class);
        when(catalogMock.getValue()).thenReturn(FROM_CATALOG);
        when(classAnnotationMetadata.getAttribute("table")).thenReturn(tableMock);
        when(classAnnotationMetadata.getAttribute("schema")).thenReturn(schemaMock);
        when(classAnnotationMetadata.getAttribute("catalog")).thenReturn(catalogMock);

        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getField(PROPERTY)).thenReturn(fieldMetadata);
        when(classOrInterfaceTypeDetails.getAnnotation(new JavaType(MigrationEntity.class.getName()))).thenReturn(classAnnotationMetadata);

        when(typeLocationService.getTypeDetails(FROM_CLASS)).thenReturn(classOrInterfaceTypeDetails);

        return classOrInterfaceTypeDetails;
    }

    private ClassOrInterfaceTypeDetails mockToClass() {
        AnnotationAttributeValue tableMock = mock(AnnotationAttributeValue.class);
        when(tableMock.getValue()).thenReturn(TO_TABLE);
        AnnotationAttributeValue schemaMock = mock(AnnotationAttributeValue.class);
        when(schemaMock.getValue()).thenReturn(TO_SCHEMA);
        AnnotationAttributeValue catalogMock = mock(AnnotationAttributeValue.class);
        when(catalogMock.getValue()).thenReturn(TO_CATALOG);
        AnnotationMetadata annotationMetadata = mock(AnnotationMetadata.class);
        when(annotationMetadata.getAttribute("table")).thenReturn(tableMock);
        when(annotationMetadata.getAttribute("schema")).thenReturn(schemaMock);
        when(annotationMetadata.getAttribute("catalog")).thenReturn(catalogMock);
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getAnnotation(new JavaType(MigrationEntity.class.getName()))).thenReturn(annotationMetadata);
        when(typeLocationService.getTypeDetails(TO_CLASS)).thenReturn(classOrInterfaceTypeDetails);

        return classOrInterfaceTypeDetails;
    }


}
