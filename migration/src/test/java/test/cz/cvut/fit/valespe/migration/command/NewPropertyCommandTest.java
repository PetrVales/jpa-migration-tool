package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.NewPropertyCommands;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.FieldOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import org.junit.Test;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Element;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NewPropertyCommandTest {

    private static final JavaType CLASS = new JavaType("test.Class");
    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final JavaType PROPERTY_TYPE = new JavaType("test.Type");
    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";
    private static final String TABLE = "table";
    private static final String AUTHOR = "author";
    private static final String ID = "id";
    private static final String PK_SUFFIX = "_pk";
    private static final String PK = COLUMN_NAME + PK_SUFFIX;


    private static final JavaSymbolName ID_PROPERTY = new JavaSymbolName("id");
    private static final JavaType ID_PROPERTY_TYPE = new JavaType("java.lang.Long");
    private static final String ID_COLUMN_NAME = "id";
    private static final String ID_COLUMN_TYPE = "bigint";
    private static final String ID_PK = ID_COLUMN_NAME + PK_SUFFIX;

    private static final JavaType STRING_PROPERTY_TYPE = new JavaType("java.lang.String");
    private static final String STRING_COLUMN_TYPE = "varchar2(255)";

    private static final JavaType INTEGER_PROPERTY_TYPE = new JavaType("java.lang.Integer");
    private static final String INTEGER_COLUMN_TYPE = "integer";

    private static final JavaType BOOLEAN_PROPERTY_TYPE = new JavaType("java.lang.Boolean");
    private static final String BOOLEAN_COLUMN_TYPE = "boolean";

    private FieldOperations fieldOperations = mock(FieldOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private ClassCommons classCommons = mock(ClassCommons.class);
    private NewPropertyCommands newPropertyCommands = new NewPropertyCommands(fieldOperations, projectOperations, liquibaseOperations, classCommons);

    @Test
    public void commandNewPropertyIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(newPropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandNewPropertyIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(newPropertyCommands.isCommandAvailable());
    }

    @Test
    public void commandNewPropertyIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(newPropertyCommands.isCommandAvailable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validationFailWhenPropertyAlreadyExistsInClass() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(true);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        newPropertyCommands.newProperty(CLASS, PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, false, false, false, false, false, AUTHOR, ID);
    }

    @Test
    public void commandNewPropertyAddNewPropertyToClassAndGeneratesMigrationChangeSet() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        Element addColumn = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumn);

        newPropertyCommands.newProperty(CLASS, PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, false, false, false, false, false, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, CLASS, false);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn), AUTHOR, ID);
    }

    @Test
    public void commandNewPropertyAddNewIdPropertyToClassAndGeneratesMigrationChangeSet() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        Element addColumn = mock(Element.class);
        Element addPrimaryKey = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumn);
        when(liquibaseOperations.addPrimaryKey(Arrays.asList(COLUMN_NAME), TABLE, PK)).thenReturn(addPrimaryKey);

        newPropertyCommands.newProperty(CLASS, PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, true, false, false, false, false, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, CLASS, true);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn, addPrimaryKey), AUTHOR, ID);
    }

    @Test
    public void addIdCommandTest() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, ID_PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        Element addColumn = mock(Element.class);
        Element addPrimaryKey = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, ID_COLUMN_NAME, ID_COLUMN_TYPE)).thenReturn(addColumn);
        when(liquibaseOperations.addPrimaryKey(Arrays.asList(ID_COLUMN_NAME), TABLE, ID_PK)).thenReturn(addPrimaryKey);

        newPropertyCommands.addId(CLASS, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(ID_PROPERTY, ID_PROPERTY_TYPE, ID_COLUMN_NAME, ID_COLUMN_TYPE, CLASS, true);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, ID_COLUMN_NAME, ID_COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn, addPrimaryKey), AUTHOR, ID);
    }

    @Test
    public void addStringCommandTest() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        Element addColumn = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, STRING_COLUMN_TYPE)).thenReturn(addColumn);

        newPropertyCommands.addString(PROPERTY, CLASS, COLUMN_NAME, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, STRING_PROPERTY_TYPE, COLUMN_NAME, STRING_COLUMN_TYPE, CLASS);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, COLUMN_NAME, STRING_COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn), AUTHOR, ID);
    }

    @Test
    public void addIntegerCommandTest() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        Element addColumn = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, INTEGER_COLUMN_TYPE)).thenReturn(addColumn);

        newPropertyCommands.addInteger(PROPERTY, CLASS, COLUMN_NAME, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, INTEGER_PROPERTY_TYPE, COLUMN_NAME, INTEGER_COLUMN_TYPE, CLASS);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, COLUMN_NAME, INTEGER_COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn), AUTHOR, ID);
    }

    @Test
    public void addBooleanCommandTest() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        Element addColumn = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, BOOLEAN_COLUMN_TYPE)).thenReturn(addColumn);

        newPropertyCommands.addBoolean(PROPERTY, CLASS, COLUMN_NAME, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, BOOLEAN_PROPERTY_TYPE, COLUMN_NAME, BOOLEAN_COLUMN_TYPE, CLASS);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, COLUMN_NAME, BOOLEAN_COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn), AUTHOR, ID);
    }

}
