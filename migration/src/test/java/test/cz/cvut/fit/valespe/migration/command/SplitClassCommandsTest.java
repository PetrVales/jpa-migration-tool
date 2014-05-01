package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.SplitClassCommands;
import cz.cvut.fit.valespe.migration.operation.*;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
import org.junit.Test;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JpaJavaType;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Element;
import test.cz.cvut.fit.valespe.migration.MigrationTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SplitClassCommandsTest extends MigrationTest {

    private static final JavaType ORIGINAL_CLASS = new JavaType("test.Class");
    private static final String ORIGINAL_TABLE = "originalTable";
    private static final String COMMON_PROPERTY_NAME = "commonProperty";
    private static final String A_PROPERTY_NAME = "aProperty";
    private static final String B_PROPERTY_NAME = "bProperty";
    private static final JavaSymbolName COMMON_PROPERTY = new JavaSymbolName(COMMON_PROPERTY_NAME);
    private static final JavaSymbolName A_PROPERTY = new JavaSymbolName(A_PROPERTY_NAME);
    private static final JavaSymbolName B_PROPERTY = new JavaSymbolName(B_PROPERTY_NAME);
    private static final String COMMON_COLUMN_NAME = "common";
    private static final String A_COLUMN_NAME = "a";
    private static final String B_COLUMN_NAME = "b";
    private static final JavaType PROPERTY_TYPE = new JavaType("test.Type");
    private static final String COLUMN_TYPE = "column-type";

    private static final JavaType A_CLASS = new JavaType("test.AClass");
    private static final JavaType B_CLASS = new JavaType("test.BClass");
    private static final String A_TABLE = "aTable";
    private static final String B_TABLE = "bTable";
    private static final String A_PROPERTIES = COMMON_PROPERTY_NAME + "," + A_PROPERTY_NAME;
    private static final String B_PROPERTIES = COMMON_PROPERTY_NAME + "," + B_PROPERTY_NAME;
    private static final String A_QUERY = "aQuery";
    private static final String B_QUERY = "bQuery";
    public static final String AUTHOR = "author";
    public static final String ID = "id";

    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private LiquibaseOperations liquibaseOperations = mock(LiquibaseOperations.class);
    private ClassOperations classOperations = mock(ClassOperations.class);
    private FieldOperations fieldOperations = mock(FieldOperations.class);
    private ClassCommons classCommons = mock(ClassCommons.class);
    private FieldCommons fieldCommons = mock(FieldCommons.class);
    private SplitClassCommands splitClassCommands = new SplitClassCommands(classOperations, fieldOperations, projectOperations, liquibaseOperations, classCommons, fieldCommons);

    @Test
    public void commandRemovePropertyIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(true);

        assertTrue(splitClassCommands.isCommandAvailable());
    }

    @Test
    public void commandRemovePropertyIsNotAvailableWhenProjectDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(false);

        assertFalse(splitClassCommands.isCommandAvailable());
    }

    @Test
    public void commandRemovePropertyIsNotAvailableWhenMigrationFileDoesNotExist() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(liquibaseOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(splitClassCommands.isCommandAvailable());
    }

    @Test
    public void commandSplitsOriginalClassIntoTwoNewClasses() {
        when(classCommons.exist(ORIGINAL_CLASS)).thenReturn(true);
        when(classCommons.exist(A_CLASS)).thenReturn(false);
        when(classCommons.exist(B_CLASS)).thenReturn(false);

        List fields = new ArrayList<FieldMetadata>(3);
        final FieldMetadata common = mockProperty(COMMON_PROPERTY, PROPERTY_TYPE, COMMON_COLUMN_NAME, COLUMN_TYPE);
        final FieldMetadata a = mockProperty(A_PROPERTY, PROPERTY_TYPE, A_COLUMN_NAME, COLUMN_TYPE);
        final FieldMetadata b = mockProperty(B_PROPERTY, PROPERTY_TYPE, B_COLUMN_NAME, COLUMN_TYPE);
        fields.add(common);
        fields.add(a);
        fields.add(b);

        when(classCommons.fields(ORIGINAL_CLASS)).thenReturn(fields);

        when(fieldCommons.fieldName(common)).thenReturn(COMMON_PROPERTY);
        when(fieldCommons.fieldName(a)).thenReturn(A_PROPERTY);
        when(fieldCommons.fieldName(b)).thenReturn(B_PROPERTY);

        when(fieldCommons.columnName(common)).thenReturn(COMMON_COLUMN_NAME);
        when(fieldCommons.columnName(a)).thenReturn(A_COLUMN_NAME);
        when(fieldCommons.columnName(b)).thenReturn(B_COLUMN_NAME);

        when(fieldCommons.columnType(common)).thenReturn(COLUMN_TYPE);
        when(fieldCommons.columnType(a)).thenReturn(COLUMN_TYPE);
        when(fieldCommons.columnType(b)).thenReturn(COLUMN_TYPE);

        when(classCommons.tableName(ORIGINAL_CLASS)).thenReturn(ORIGINAL_TABLE);

        Element createTableA = mock(Element.class);
        when(liquibaseOperations.createTable(A_TABLE)).thenReturn(createTableA);
        Element createTableB = mock(Element.class);
        when(liquibaseOperations.createTable(B_TABLE)).thenReturn(createTableB);
        Element addColumnACommon = mock(Element.class);
        when(liquibaseOperations.addColumn(A_TABLE, COMMON_COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumnACommon);
        Element addColumnBCommon = mock(Element.class);
        when(liquibaseOperations.addColumn(B_TABLE, COMMON_COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumnBCommon);
        Element addColumnA = mock(Element.class);
        when(liquibaseOperations.addColumn(A_TABLE, A_COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumnA);
        Element addColumnB = mock(Element.class);
        when(liquibaseOperations.addColumn(B_TABLE, B_COLUMN_NAME, COLUMN_TYPE)).thenReturn(addColumnB);
        Element dropTable = mock(Element.class);
        when(liquibaseOperations.dropTable(ORIGINAL_TABLE, true)).thenReturn(dropTable);
        Element copyA = mock(Element.class);
        when(liquibaseOperations.copyData(ORIGINAL_TABLE, A_TABLE, Arrays.asList(COMMON_COLUMN_NAME, A_COLUMN_NAME), A_QUERY)).thenReturn(copyA);
        Element copyB = mock(Element.class);
        when(liquibaseOperations.copyData(ORIGINAL_TABLE, B_TABLE, Arrays.asList(COMMON_COLUMN_NAME, B_COLUMN_NAME), B_QUERY)).thenReturn(copyB);

        splitClassCommands.splitClass(ORIGINAL_CLASS, A_CLASS, B_CLASS, A_TABLE, B_TABLE, null, null, A_PROPERTIES, B_PROPERTIES, A_QUERY, B_QUERY, AUTHOR, ID);

        verify(classOperations, times(1)).createClass(A_CLASS, A_TABLE, A_TABLE);
        verify(classOperations, times(1)).createClass(B_CLASS, B_TABLE, B_TABLE);

        verify(fieldOperations, times(1)).addField(A_PROPERTY, PROPERTY_TYPE, A_COLUMN_NAME, COLUMN_TYPE, A_CLASS);
        verify(fieldOperations, times(1)).addField(COMMON_PROPERTY, PROPERTY_TYPE, COMMON_COLUMN_NAME, COLUMN_TYPE, A_CLASS);
        verify(fieldOperations, times(1)).addField(B_PROPERTY, PROPERTY_TYPE, B_COLUMN_NAME, COLUMN_TYPE, B_CLASS);
        verify(fieldOperations, times(1)).addField(COMMON_PROPERTY, PROPERTY_TYPE, COMMON_COLUMN_NAME, COLUMN_TYPE, B_CLASS);


        verify(classOperations, times(1)).removeClass(ORIGINAL_CLASS);
        verify(liquibaseOperations, times(1)).dropTable(ORIGINAL_TABLE, true);
        verify(liquibaseOperations, times(1)).createChangeSet(Arrays.asList(createTableA, createTableB, addColumnACommon, addColumnA, addColumnBCommon, addColumnB, copyA, copyB, dropTable), AUTHOR, ID);
    }

}
