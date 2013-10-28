package test.cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.command.SplitClassCommands;
import cz.cvut.fit.valespe.migration.operation.MigrationSetupOperations;
import cz.cvut.fit.valespe.migration.operation.SplitClassOperations;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import test.cz.cvut.fit.valespe.migration.MigrationTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SplitClassCommandsTest extends MigrationTest {

    private static final JavaType ORIGINAL_CLASS = new JavaType("test.Class");
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
    private static final String SCHEMA = "schema";
    private static final String CATALOG = "catalog";
    private static final String TABLESPACE = "tablespace";

    private SplitClassOperations splitClassOperations = mock(SplitClassOperations.class);
    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private MigrationSetupOperations migrationSetupOperations = mock(MigrationSetupOperations.class);
    private TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private SplitClassCommands splitClassCommands = new SplitClassCommands(splitClassOperations, projectOperations, migrationSetupOperations, typeLocationService);

    @Test
    public void commandRemovePropertyIsAvailableWhenProjectAndMigrationFileAreCreated() {
        when(projectOperations.isFocusedProjectAvailable()).thenReturn(true);
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(true);

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
        when(migrationSetupOperations.doesMigrationFileExist()).thenReturn(false);

        assertFalse(splitClassCommands.isCommandAvailable());
    }

    @Test
    public void commandSplitsOriginalClassIntoTwoNewClasses() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);

        List fields = new ArrayList<FieldMetadata>(3);
        fields.add(mockProperty(COMMON_PROPERTY, PROPERTY_TYPE, COMMON_COLUMN_NAME, COLUMN_TYPE));
        fields.add(mockProperty(A_PROPERTY, PROPERTY_TYPE, A_COLUMN_NAME, COLUMN_TYPE));
        fields.add(mockProperty(B_PROPERTY, PROPERTY_TYPE, B_COLUMN_NAME, COLUMN_TYPE));

        when(classOrInterfaceTypeDetails.getDeclaredFields()).thenReturn(fields);
        when(typeLocationService.getTypeDetails(ORIGINAL_CLASS)).thenReturn(classOrInterfaceTypeDetails);

        splitClassCommands.splitClass(ORIGINAL_CLASS, A_CLASS, B_CLASS, A_TABLE, B_TABLE, A_PROPERTIES, B_PROPERTIES, SCHEMA, CATALOG, TABLESPACE);

        ArgumentCaptor<List> aProperties = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> bProperties = ArgumentCaptor.forClass(List.class);
        verify(splitClassOperations, times(1)).createClass(eq(ORIGINAL_CLASS), eq(A_CLASS), aProperties.capture(), eq(A_TABLE), eq(SCHEMA), eq(CATALOG), eq(TABLESPACE));
        verify(splitClassOperations, times(1)).createClass(eq(ORIGINAL_CLASS), eq(B_CLASS), bProperties.capture(), eq(B_TABLE), eq(SCHEMA), eq(CATALOG), eq(TABLESPACE));
        verify(splitClassOperations, times(1)).createTable(anyList(), eq(A_TABLE), eq(SCHEMA), eq(CATALOG), eq(TABLESPACE));
        verify(splitClassOperations, times(1)).createTable(anyList(), eq(B_TABLE), eq(SCHEMA), eq(CATALOG), eq(TABLESPACE));
        verify(splitClassOperations, times(1)).removeClass(ORIGINAL_CLASS);

        assertEquals(2, aProperties.getValue().size());
        assertEquals(2, bProperties.getValue().size());
    }

}
