package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.PropertyOperations;
import cz.cvut.fit.valespe.migration.operation.impl.PropertyOperationsImpl;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.impl.ClassCommonsImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class PropertyOperationsTest {

    private static final JavaType CLASS = new JavaType("test.Class");
    private static final JavaSymbolName PROPERTY = new JavaSymbolName("property");
    private static final JavaType PROPERTY_TYPE = new JavaType("test.Type");
    private static final String PHYSICAL_TYPE_IDENTIFIER = "MID:" + PhysicalTypeIdentifier.class.getName() + "#?";
    private static final String COLUMN_NAME = "column-name";
    private static final String COLUMN_TYPE = "column-type";
    private static final JavaSymbolName PROPERTY_NOT_TO_REMOVE = new JavaSymbolName("propertyNotToRemove");
    private static final PhysicalTypeCategory PHYSICAL_TYPE_CATEGORY = PhysicalTypeCategory.CLASS;

    private final TypeManagementService typeManagementService = mock(TypeManagementService.class);
    private final ClassCommons classCommons = mock(ClassCommons.class);
    private final PropertyOperations propertyOperations = new PropertyOperationsImpl(typeManagementService, classCommons);

    @Test
    public void addField() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classCommons.classDetails(CLASS)).thenReturn(classOrInterfaceTypeDetails);
        when(classOrInterfaceTypeDetails.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);

        propertyOperations.addField(PROPERTY, PROPERTY_TYPE, COLUMN_NAME, COLUMN_TYPE, CLASS);

        ArgumentCaptor<FieldMetadata> argument = ArgumentCaptor.forClass(FieldMetadata.class);
        verify(typeManagementService, times(1)).addField(argument.capture());

        assertEquals(PROPERTY.getSymbolName(), argument.getValue().getFieldName().getSymbolName());
        assertEquals(PROPERTY_TYPE.getSimpleTypeName(), argument.getValue().getFieldType().getSimpleTypeName());
        assertFalse(argument.getValue().getAnnotations().isEmpty());
    }

    @Test
    public void removeFieldFromClass() {
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = mock(ClassOrInterfaceTypeDetails.class);
        when(classCommons.classDetails(CLASS)).thenReturn(classOrInterfaceTypeDetails);
        List fields = new ArrayList<FieldMetadata>();
        final FieldMetadata fieldMetadata1 = mock(FieldMetadata.class);
        when(fieldMetadata1.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);
        when(fieldMetadata1.getFieldName()).thenReturn(PROPERTY);
        when(fieldMetadata1.getFieldType()).thenReturn(PROPERTY_TYPE);
        final FieldMetadata fieldMetadata2 = mock(FieldMetadata.class);
        when(fieldMetadata2.getFieldName()).thenReturn(PROPERTY_NOT_TO_REMOVE);
        when(fieldMetadata2.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);
        when(fieldMetadata2.getFieldType()).thenReturn(PROPERTY_TYPE);
        fields.add(fieldMetadata1);
        fields.add(fieldMetadata2);
        when(classOrInterfaceTypeDetails.getDeclaredByMetadataId()).thenReturn(PHYSICAL_TYPE_IDENTIFIER);
        when(classOrInterfaceTypeDetails.getDeclaredFields()).thenReturn(fields);
        when(classOrInterfaceTypeDetails.getName()).thenReturn(CLASS);
        when(classOrInterfaceTypeDetails.getPhysicalTypeCategory()).thenReturn(PHYSICAL_TYPE_CATEGORY);

        propertyOperations.removeField(PROPERTY, CLASS);

        ArgumentCaptor<ClassOrInterfaceTypeDetails> argument = ArgumentCaptor.forClass(ClassOrInterfaceTypeDetails.class);
        verify(typeManagementService, times(1)).createOrUpdateTypeOnDisk(argument.capture());

        assertEquals(1, argument.getValue().getDeclaredFields().size());
        assertEquals(PROPERTY_NOT_TO_REMOVE, argument.getValue().getDeclaredFields().get(0).getFieldName());
    }

}
