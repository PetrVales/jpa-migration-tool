package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.NewClassCommands;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.NewClassOperations;
import org.junit.Test;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Element;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NewClassCommandsTest {

    private static final String CLASS_NAME = "ClassName";
    private static final JavaType CLASS = new JavaType("test." + CLASS_NAME);
    private static final String ENTITY_NAME = "entity-name";
    private static final String TABLE = "table";
    private static final String AUTHOR = "author";
    private static final String ID = "id";

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
        Element createTable = mock(Element.class);
        when(liquibaseOperations.createTable(TABLE)).thenReturn(createTable);

        newclassCommands.newClass(CLASS, TABLE, ENTITY_NAME, AUTHOR, ID);

        verify(newclassOperations, times(1)).createEntity(CLASS, ENTITY_NAME, TABLE);
        verify(liquibaseOperations, times(1)).createTable(TABLE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(createTable), AUTHOR, ID);
    }

    @Test
    public void commandNewClassUseTableNameWhenEntityNameIsNotSpecify() {
        Element createTable = mock(Element.class);
        when(liquibaseOperations.createTable(TABLE)).thenReturn(createTable);

        newclassCommands.newClass(CLASS, TABLE, null, AUTHOR, ID);

        verify(newclassOperations, times(1)).createEntity(CLASS, TABLE, TABLE);
        verify(liquibaseOperations, times(1)).createTable(TABLE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(createTable), AUTHOR, ID);
    }

}
