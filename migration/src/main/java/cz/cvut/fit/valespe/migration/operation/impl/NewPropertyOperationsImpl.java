package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.JpaFieldDetails;
import cz.cvut.fit.valespe.migration.operation.NewPropertyOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class NewPropertyOperationsImpl implements NewPropertyOperations {

    @Reference private TypeLocationService typeLocationService;
    @Reference private TypeManagementService typeManagementService;

    public NewPropertyOperationsImpl() { }

    public NewPropertyOperationsImpl(TypeManagementService typeManagementService, TypeLocationService typeLocationService) {
        this.typeManagementService = typeManagementService;
        this.typeLocationService = typeLocationService;
    }

    @Override
    public void addFieldToClass(JavaSymbolName propertyName, JavaType propertyType, String columnName, String columnType, ClassOrInterfaceTypeDetails classType) {
        addFieldToClass(propertyName, propertyType, columnName, columnType, classType, false);
    }

    @Override
    public void addFieldToClass(JavaSymbolName propertyName, JavaType propertyType, String columnName, String columnType, ClassOrInterfaceTypeDetails classType, boolean id) {
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

    private void insertField(final FieldDetails fieldDetails) {
        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);
        fieldDetails.setAnnotations(annotations);

        fieldDetails.setModifiers(Modifier.PRIVATE);

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(fieldDetails);
        typeManagementService.addField(fieldBuilder.build());
    }

}