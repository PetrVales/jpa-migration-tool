package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.RemovePropertyCommands;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.PropertyOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
import cz.cvut.fit.valespe.migration.util.impl.ClassCommonsImpl;
import cz.cvut.fit.valespe.migration.util.impl.FieldCommonsImpl;
import org.junit.Test;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Element;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class RemovePropertyCommandTest {

    private static final JavaType CLASS = new JavaType("test.Class");
    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final String COLUMN_NAME = "column-name";
    private static final String TABLE = "table";
    public static final String AUTHOR = "author";
    public static final String ID = "id";

    private PropertyOperations propertyOperations = mock(PropertyOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private FieldCommons fieldCommons = mock(FieldCommons.class);
    private ClassCommons classCommons = mock(ClassCommons.class);
    private RemovePropertyCommands removePropertyCommands = new RemovePropertyCommands(propertyOperations, projectOperations, liquibaseOperations, fieldCommons, classCommons);

    @Test
    public void commandRemovePropertyIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(removePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandRemovePropertyIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(removePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandRemovePropertyIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(removePropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandRemovePropertyToClassAndGeneratesMigrationChangeSet() {
        FieldMetadata fieldMock = mock(FieldMetadata.class);
        Element dropColumn = mock(Element.class);

        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(true);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);
        when(classCommons.field(CLASS, PROPERTY)).thenReturn(fieldMock);
        when(fieldCommons.columnName(fieldMock)).thenReturn(COLUMN_NAME);
        when(liquibaseOperations.dropColumn(TABLE, COLUMN_NAME)).thenReturn(dropColumn);

        removePropertyCommands.removeProperty(CLASS, PROPERTY, AUTHOR, ID);

        verify(propertyOperations, times(1)).removeField(PROPERTY, CLASS);
        verify(liquibaseOperations, times(1)).dropColumn(TABLE, COLUMN_NAME);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(dropColumn), AUTHOR, ID);
    }

}
