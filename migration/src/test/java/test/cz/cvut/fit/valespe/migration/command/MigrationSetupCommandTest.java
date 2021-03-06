package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.MigrationSetupCommand;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import org.junit.Test;
import org.springframework.roo.project.ProjectOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MigrationSetupCommandTest {

    ProjectOperations projectOperations = mock(ProjectOperations.class);
    LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    MigrationSetupCommand command = new MigrationSetupCommand(projectOperations, liquibaseOperations);

    @Test
    public void commandMigrationSetupIsAvailableWhenProjectIsCreatedAndMigrationFileDoesntExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertTrue(command.isCommandAvailable());
    }

    @Test
    public void commandMigrationSetupIsNotAvailableWhenProjectIsNotCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(command.isCommandAvailable());
    }

    @Test
    public void commandMigrationSetupIsNotAvailableWhenMigrationFileDoesExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

        assertFalse(command.isCommandAvailable());
    }

    @Test
    public void commandMigrationSetupCreatesMigrationFile() {
        command.initMigration();

        verify(projectOperations, times(1)).addDependencies(anyString(), anyList());
        verify(liquibaseOperations, times(1)).createMigrationFile();
    }

}
