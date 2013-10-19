package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.command.RemoveClassCommands;
import cz.cvut.fit.valespe.migration.operation.MigrationSetupOperations;
import cz.cvut.fit.valespe.migration.operation.RemoveClassOperations;
import org.junit.Test;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class RemoveClassCommandTest {

    private static final JavaType CLASS_TO_REMOVE = new JavaType("test.Type");
    private static final String TABLE = "table";
    private static final String SCHEMA = "schema";
    private static final String CATALOG = "catalog";

    private RemoveClassOperations removeClassOperations = mock(RemoveClassOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private MigrationSetupOperations migrationSetupOperations = mock(MigrationSetupOperations.class);
    private TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private RemoveClassCommands removeClassCommand =
            new RemoveClassCommands(removeClassOperations, projectOperations, migrationSetupOperations, typeLocationService);

    @Test
    public void commandRemoveClassIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(removeClassCommand.isCommandAvailable());
    }

    @Test
    public void commandRemoveClassIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(removeClassCommand.isCommandAvailable());
    }

    @Test
    public void commandRemoveClassIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(removeClassCommand.isCommandAvailable());
    }

    @Test
    public void commandRemoveClassRemovesClassAndGeneratesMigrationChangeSet() {
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
        when(typeLocationService.getTypeDetails(CLASS_TO_REMOVE)).thenReturn(classOrInterfaceTypeDetails);

        removeClassCommand.removeClass(CLASS_TO_REMOVE);

        verify(removeClassOperations, times(1)).removeClass(CLASS_TO_REMOVE);
    }

}
