package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.MergeClassCommands;
import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.FieldOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
import org.junit.Test;
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
    private static final String ENTITY = "entity";
    private static final String AUTOR = "author";
    private static final String ID = "id";
    private static final String QUERY = "query";

    private ClassOperations classOperations = mock(ClassOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private FieldOperations fieldOperations = mock(FieldOperations.class);
    private ClassCommons classCommons = mock(ClassCommons.class);
    private FieldCommons fieldCommons = mock(FieldCommons.class);
    private MergeClassCommands mergeClassCommands = new MergeClassCommands(classOperations, projectOperations, liquibaseOperations, fieldOperations, classCommons, fieldCommons);

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
        when(classCommons.exist(A_CLASS)).thenReturn(true);
        when(classCommons.exist(B_CLASS)).thenReturn(true);

        mergeClassCommands.mergeClass(CLASS, A_CLASS, B_CLASS, TABLE, ENTITY, QUERY, AUTOR, ID);

        verify(classOperations, times(1)).createClass(CLASS, ENTITY, TABLE);
//        verify(classOperations, times(1)).createTable(classOrInterfaceTypeDetails);
    }


}
