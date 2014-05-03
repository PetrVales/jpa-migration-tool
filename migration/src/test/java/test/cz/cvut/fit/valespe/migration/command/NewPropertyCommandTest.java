package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.NewPropertyCommands;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.FieldOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
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
    private static final String PK = TABLE + PK_SUFFIX;
    private static final Boolean DONT_SKIP = false;

    private static final JavaType REF_CLASS = new JavaType("test.RefClass");
    private static final String REF_TABLE = "ref-table";
    private static final String REF_COLUMN = "ref-column";
    private static final String MAPPED_BY = "refProperty";
    private static final String FK = TABLE + "_" + REF_TABLE + "_fk";


    private static final JavaSymbolName ID_PROPERTY = new JavaSymbolName("id");
    private static final JavaType ID_PROPERTY_TYPE = new JavaType("java.lang.Long");
    private static final String ID_COLUMN_NAME = "id";
    private static final String ID_COLUMN_TYPE = "bigint";
    private static final String ID_PK = TABLE + PK_SUFFIX;

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
    private FieldCommons fieldCommons = mock(FieldCommons.class);
    private NewPropertyCommands newPropertyCommands = new NewPropertyCommands(fieldOperations, projectOperations, liquibaseOperations, classCommons, fieldCommons);

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

        newPropertyCommands.newProperty(CLASS, PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, false, false, false, false, false, null, null, DONT_SKIP, AUTHOR, ID);
    }

    @Test
    public void commandNewPropertyAddNewPropertyToClassAndGeneratesMigrationChangeSet() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        Element addColumn = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumn);

        newPropertyCommands.newProperty(CLASS, PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, false, false, false, false, false, null, null, DONT_SKIP, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, CLASS, false, false, false, false, false, null);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn), AUTHOR, ID);
    }

    @Test
    public void addIdProperty() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);

        Element addColumn = mock(Element.class);
        Element addPrimaryKey = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumn);
        when(liquibaseOperations.addPrimaryKey(Arrays.asList(COLUMN_NAME), TABLE, PK)).thenReturn(addPrimaryKey);

        newPropertyCommands.newProperty(CLASS, PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, true, false, false, false, false, null, null, DONT_SKIP, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, CLASS, true, false, false, false, false, null);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn, addPrimaryKey), AUTHOR, ID);
    }

    @Test
    public void addOneToOneProperty() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);
        when(classCommons.tableName(REF_CLASS)).thenReturn(REF_TABLE);

        Element addColumn = mock(Element.class);
        Element addForeignKey = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumn);
        when(liquibaseOperations.addForeignKey(TABLE, COLUMN_NAME, REF_TABLE, REF_COLUMN, FK)).thenReturn(addForeignKey);

        newPropertyCommands.newProperty(CLASS, PROPERTY, REF_CLASS, COLUMN_NAME, COLUMN_TYPE, false, true, false, false, false, null, REF_COLUMN, DONT_SKIP, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, REF_CLASS, COLUMN_NAME, COLUMN_TYPE, CLASS, false, true, false, false, false, null);
        verify(liquibaseOperations, times(1)).addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(addColumn, addForeignKey), AUTHOR, ID);
    }

    @Test
    public void addOneToOnePropertyMappedBy() {
        when(classCommons.exist(CLASS)).thenReturn(true);
        when(classCommons.hasField(CLASS, PROPERTY)).thenReturn(false);
        when(classCommons.tableName(CLASS)).thenReturn(TABLE);
        when(classCommons.tableName(REF_CLASS)).thenReturn(REF_TABLE);
        FieldMetadata fieldMetadata = mock(FieldMetadata.class);
        when(classCommons.field(REF_CLASS, new JavaSymbolName(MAPPED_BY))).thenReturn(fieldMetadata);

        Element addColumn = mock(Element.class);
        Element addForeignKey = mock(Element.class);
        when(liquibaseOperations.addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumn);
        when(liquibaseOperations.addForeignKey(TABLE, COLUMN_NAME, REF_TABLE, REF_COLUMN, FK)).thenReturn(addForeignKey);

        newPropertyCommands.newProperty(CLASS, PROPERTY, REF_CLASS, COLUMN_NAME, COLUMN_TYPE, false, true, false, false, false, MAPPED_BY, null, DONT_SKIP, AUTHOR, ID);

        verify(fieldOperations, times(1)).addField(PROPERTY, REF_CLASS, COLUMN_NAME, COLUMN_TYPE, CLASS, false, true, false, false, false, MAPPED_BY);
        verify(liquibaseOperations, never()).addColumn(TABLE, COLUMN_NAME, COLUMN_TYPE);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.<Element>asList(), AUTHOR, ID);
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

        verify(fieldOperations, times(1)).addField(ID_PROPERTY, ID_PROPERTY_TYPE, ID_COLUMN_NAME, ID_COLUMN_TYPE, CLASS, true, false, false, false, false, null);
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
