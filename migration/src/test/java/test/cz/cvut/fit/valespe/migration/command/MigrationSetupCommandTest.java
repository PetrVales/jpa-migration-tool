package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.MigrationSetupCommand;
import cz.cvut.fit.valespe.migration.operation.MigrationSetupOperations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.roo.project.ProjectOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MigrationSetupCommandTest {

    ProjectOperations projectOperations = mock(ProjectOperations.class);
    MigrationSetupOperations migrationSetupOperations = mock(MigrationSetupOperations.class);
    MigrationSetupCommand command = new MigrationSetupCommand(projectOperations, migrationSetupOperations);

    @Test
    public void commandMigrationSetupIsAvailableWhenProjectIsCreatedAndMigrationFileDoesntExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(false);

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
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(true);

        assertFalse(command.isCommandAvailable());
    }

    @Test
    public void commandMigrationSetupCreatesMigrationFile() {
        command.initMigration();

        verify(migrationSetupOperations, times(1)).createMigrationFile();
    }

}
