package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.JpaFieldDetails;
import cz.cvut.fit.valespe.migration.operation.PropertyOperations;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class PropertyOperationsImpl implements PropertyOperations {

    @Reference private TypeLocationService typeLocationService;
    @Reference private TypeManagementService typeManagementService;

    public PropertyOperationsImpl() { }

    public PropertyOperationsImpl(TypeManagementService typeManagementService, TypeLocationService typeLocationService) {
        this.typeManagementService = typeManagementService;
        this.typeLocationService = typeLocationService;
    }

    @Override
    public void addField(JavaSymbolName propertyName, JavaType propertyType, String columnName, String columnType, ClassOrInterfaceTypeDetails classType) {
        addField(propertyName, propertyType, columnName, columnType, classType, false);
    }

    @Override
    public void addField(JavaSymbolName propertyName, JavaType propertyType, String columnName, String columnType, ClassOrInterfaceTypeDetails classType, boolean id) {
        final String physicalTypeIdentifier = classType.getDeclaredByMetadataId();
        final JpaFieldDetails fieldDetails = new JpaFieldDetails(physicalTypeIdentifier, propertyType, propertyName);
        if (columnName != null) {
            fieldDetails.setColumn(columnName);
        }
        if (columnType != null) {
            fieldDetails.setColumnDefinition(columnType);
        }
        fieldDetails.setId(id);

        insertField(fieldDetails);
    }

    @Override
    public void removeField(JavaSymbolName propertyName, ClassOrInterfaceTypeDetails javaTypeDetails) {
        List<? extends FieldMetadata> fields = javaTypeDetails.getDeclaredFields();
        List<FieldMetadataBuilder> fieldsBuilders = new ArrayList<FieldMetadataBuilder>(fields.size());

        for (FieldMetadata fieldMetadata : fields) {
            if (fieldMetadata.getFieldName().compareTo(propertyName) != 0) {
                fieldsBuilders.add(new FieldMetadataBuilder(fieldMetadata));
            }
        }

        ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(javaTypeDetails);
        builder.setDeclaredFields(fieldsBuilders);

        typeManagementService.createOrUpdateTypeOnDisk(builder.build());
    }

    private void insertField(final FieldDetails fieldDetails) {
        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);
        fieldDetails.setAnnotations(annotations);

        fieldDetails.setModifiers(Modifier.PRIVATE);

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(fieldDetails);
        typeManagementService.addField(fieldBuilder.build());
    }

}