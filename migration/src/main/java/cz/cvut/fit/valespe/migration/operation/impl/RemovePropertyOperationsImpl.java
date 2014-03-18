package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.operation.RemovePropertyOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class RemovePropertyOperationsImpl implements RemovePropertyOperations {

    @Reference private TypeManagementService typeManagementService;

    public RemovePropertyOperationsImpl() { }

    public RemovePropertyOperationsImpl(TypeManagementService typeManagementService) {
        this.typeManagementService = typeManagementService;
    }

    @Override
    public void removeFieldFromClass(JavaSymbolName propertyName, ClassOrInterfaceTypeDetails javaTypeDetails) {
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

}
