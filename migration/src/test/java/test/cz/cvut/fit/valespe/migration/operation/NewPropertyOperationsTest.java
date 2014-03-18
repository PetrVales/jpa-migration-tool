package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.NewPropertyOperations;
import cz.cvut.fit.valespe.migration.operation.impl.NewPropertyOperationsImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import java.io.StringBufferInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class NewPropertyOperationsTest {

    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final JavaType PROPERTY_TYPE = new JavaType("test.Type");
    private static final String PHYSICAL_TYPE_IDENTIFIER = "MID:" + PhysicalTypeIdentifier.class.getName() + "#?";
    private static final LogicalPath SRC_MAIN = mock(LogicalPath.class);
    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";


    private final TypeManagementService typeManagementService = mock(TypeManagementService.class);
    private final TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private final NewPropertyOperations newPropertyOperations = new NewPropertyOperationsImpl(typeManagementService, typeLocationService);

    @Test
    public void createClass() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classOrInterfaceTypeDetails.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);

        newPropertyOperations.addFieldToClass(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, classOrInterfaceTypeDetails);

        ArgumentCaptor<FieldMetadata> argument = ArgumentCaptor.forClass(FieldMetadata.class);
        verify(typeManagementService, times(1)).addField(argument.capture());

        assertEquals(PROPERTY.getSymbolName(), argument.getValue().getFieldName().getSymbolName());
        assertEquals(PROPERTY_TYPE.getSimpleTypeName(), argument.getValue().getFieldType().getSimpleTypeName());
        assertFalse(argument.getValue().getAnnotations().isEmpty());
    }

}
