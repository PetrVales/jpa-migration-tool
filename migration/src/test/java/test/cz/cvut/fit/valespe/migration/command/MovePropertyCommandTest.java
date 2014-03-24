package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.command.MovePropertyCommands;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.MovePropertyOperations;
import cz.cvut.fit.valespe.migration.operation.PropertyOperations;
import org.junit.Test;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Element;

import java.util.Arrays;

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
    private static final String TO_TABLE = "to-table";
    private static final String QUERY = "query";
    private static final String AUTHOR = "author";
    private static final String ID = "id";


    private PropertyOperations propertyOperations = mock(PropertyOperations.class);
    private MovePropertyOperations movePropertyOperations = mock(MovePropertyOperations.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private MovePropertyCommands movePropertyCommands = new MovePropertyCommands(propertyOperations, movePropertyOperations, liquibaseOperations, typeLocationService, projectOperations);

    @Test
    public void commandMovePropertyIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

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
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(movePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandMovePropertyRemovesOldPropertyFromOriginalClass() {
        ClassOrInterfaceTypeDetails fromCLass = mockFromClass();
        ClassOrInterfaceTypeDetails toCLass = mockToClass();

        Element addColumn = mock(Element.class);
        Element copyColumnData = mock(Element.class);
        Element dropColumn = mock(Element.class);
        when(liquibaseOperations.addColumn(TO_TABLE, COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumn);
        when(liquibaseOperations.copyColumnData(FROM_TABLE, TO_TABLE, COLUMN_NAME, QUERY)).thenReturn(copyColumnData);
        when(liquibaseOperations.dropColumn(FROM_TABLE, COLUMN_NAME)).thenReturn(dropColumn);

        movePropertyCommands.moveProperty(FROM_CLASS, TO_CLASS, PROPERTY, QUERY, AUTHOR, ID);

        verify(propertyOperations, times(1)).addField(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, toCLass);
        verify(propertyOperations, times(1)).removeField(PROPERTY, fromCLass);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn, copyColumnData, dropColumn), AUTHOR, ID);
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
        when(classAnnotationMetadata.getAttribute("table")).thenReturn(tableMock);

        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getField(PROPERTY)).thenReturn(fieldMetadata);
        when(classOrInterfaceTypeDetails.getAnnotation(new JavaType(MigrationEntity.class.getName()))).thenReturn(classAnnotationMetadata);

        when(typeLocationService.getTypeDetails(FROM_CLASS)).thenReturn(classOrInterfaceTypeDetails);

        return classOrInterfaceTypeDetails;
    }

    private ClassOrInterfaceTypeDetails mockToClass() {
        AnnotationAttributeValue tableMock = mock(AnnotationAttributeValue.class);
        when(tableMock.getValue()).thenReturn(TO_TABLE);
        AnnotationMetadata annotationMetadata = mock(AnnotationMetadata.class);
        when(annotationMetadata.getAttribute("table")).thenReturn(tableMock);
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getAnnotation(new JavaType(MigrationEntity.class.getName()))).thenReturn(annotationMetadata);
        when(typeLocationService.getTypeDetails(TO_CLASS)).thenReturn(classOrInterfaceTypeDetails);

        return classOrInterfaceTypeDetails;
    }


}
