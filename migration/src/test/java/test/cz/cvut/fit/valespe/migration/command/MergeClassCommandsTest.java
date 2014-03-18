package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.MergeClassCommands;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.MergeClassOperations;
import org.junit.Test;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import test.cz.cvut.fit.valespe.migration.MigrationTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MergeClassCommandsTest extends MigrationTest {

    private static final JavaType CLASS = new JavaType("test.Class");
    private static final JavaType A_CLASS = new JavaType("test.AClass");
    private static final JavaType B_CLASS = new JavaType("test.BClass");
    private static final String TABLE = "table";
    private static final String SCHEMA = "schema";
    private static final String CATALOG = "catalog";
    private static final String TABLESPACE = "tablespace";

    private MergeClassOperations mergeClassOperations = mock(MergeClassOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private MergeClassCommands mergeClassCommands = new MergeClassCommands(mergeClassOperations, projectOperations, liquibaseOperations, typeLocationService);

    @Test
    public void commandRemovePropertyIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(mergeClassCommands.isCommandAvailable());
    }

    @Test
    public void commandRemovePropertyIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(mergeClassCommands.isCommandAvailable());
    }

    @Test
    public void commandRemovePropertyIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(mergeClassCommands.isCommandAvailable());
    }

    @Test
    public void commandMergeTwoClassesIntoOneNewClass() {
        final ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(typeLocationService.getTypeDetails(A_CLASS)).thenReturn(mock(ClassOrInterfaceTypeDetails.class));
        when(typeLocationService.getTypeDetails(B_CLASS)).thenReturn(mock(ClassOrInterfaceTypeDetails.class));
        when(typeLocationService.getTypeDetails(CLASS)).thenReturn(classOrInterfaceTypeDetails);

        mergeClassCommands.mergeClass(CLASS, A_CLASS, B_CLASS, TABLE, SCHEMA, CATALOG, TABLESPACE);

        verify(mergeClassOperations, times(1)).mergeClasses(CLASS, A_CLASS, B_CLASS, TABLE, SCHEMA, CATALOG, TABLESPACE);
        verify(mergeClassOperations, times(1)).createTable(classOrInterfaceTypeDetails);
    }


}
