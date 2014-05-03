package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.RemoveClassCommands;
import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import org.junit.Test;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Element;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class RemoveClassCommandTest {

    private static final JavaType CLASS_TO_REMOVE = new JavaType("test.Type");
    private static final String TABLE = "table";
    private static final String AUTHOR = "author";
    private static final String ID = "ID";
    private static final Boolean DONT_SKIP = false;

    private ClassOperations classOperations = mock(ClassOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private ClassCommons classCommons = mock(ClassCommons.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private RemoveClassCommands removeClassCommand =
            new RemoveClassCommands(classOperations, projectOperations, classCommons, liquibaseOperations);

    @Test
    public void commandRemoveClassIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

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
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(removeClassCommand.isCommandAvailable());
    }

    @Test
    public void commandRemoveClassRemovesClassAndGeneratesMigrationChangeSet() {
        when(classCommons.exist(CLASS_TO_REMOVE)).thenReturn(true);
        when(classCommons.tableName(CLASS_TO_REMOVE)).thenReturn(TABLE);

        Element dropTable = mock(Element.class);
        when(liquibaseOperations.dropTable(TABLE, false)).thenReturn(dropTable);

        removeClassCommand.removeClass(CLASS_TO_REMOVE, DONT_SKIP, AUTHOR, ID);

        verify(classOperations, times(1)).removeClass(CLASS_TO_REMOVE);
        verify(liquibaseOperations, times(1)).dropTable(TABLE, false);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(dropTable), AUTHOR, ID);
    }

}
