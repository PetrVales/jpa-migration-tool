package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.NewClassCommands;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.NewClassOperations;
import org.junit.Test;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NewClassCommandsTest {

    private static final String CLASS_NAME = "ClassName";
    private static final JavaType CLASS = new JavaType("test." + CLASS_NAME);
    private static final String ENTITY_NAME = "entity-name";
    private static final String TABLE = "table";
    private static final String SCHEMA = "schema";
    private static final String CATALOG = "catalog";
    private static final String TABLESPACE = "tablespace";

    private NewClassOperations newclassOperations = mock(NewClassOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private NewClassCommands newclassCommands = new NewClassCommands(newclassOperations, projectOperations, liquibaseOperations);

    @Test
    public void commandNewClassIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(newclassCommands.isCommandAvailable());
    }

    @Test
    public void commandNewClassIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(newclassCommands.isCommandAvailable());
    }

    @Test
    public void commandNewClassIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(newclassCommands.isCommandAvailable());
    }

    @Test
    public void commandNewClassGeneratesClassFileAndChangeSetRecord() {
        newclassCommands.newClass(CLASS, TABLE, ENTITY_NAME);

        verify(newclassOperations, times(1)).createEntity(CLASS, ENTITY_NAME, TABLE);
        verify(liquibaseOperations, times(1)).createTable(TABLE);
    }

    @Test
    public void commandNewClassUseClassNameWhenTableNameIsNotSpecify() {
        newclassCommands.newClass(CLASS, null, ENTITY_NAME);

        verify(newclassOperations, times(1)).createEntity(CLASS, ENTITY_NAME, null);
        verify(liquibaseOperations, times(1)).createTable(CLASS_NAME);
    }

}
