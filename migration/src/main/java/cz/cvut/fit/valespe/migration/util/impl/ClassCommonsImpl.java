package cz.cvut.fit.valespe.migration.util.impl;

import cz.cvut.fit.valespe.migration.util.ClassCommons;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JpaJavaType;

import java.util.List;

@Component
@Service
public class ClassCommonsImpl implements ClassCommons {

    @Reference
    private TypeLocationService typeLocationService;

    public JavaType className(ClassOrInterfaceTypeDetails typeDetails) {
        return typeDetails.getName();
    }

    public ClassOrInterfaceTypeDetails classDetails(JavaType typeName) {
        return typeLocationService.getTypeDetails(typeName);
    }

    public String tableName(JavaType typeName) {
        return tableName(classDetails(typeName));
    }

    public String tableName(ClassOrInterfaceTypeDetails classDetail) {
        AnnotationMetadata migrationEntity = classDetail.getAnnotation(JpaJavaType.TABLE);
        AnnotationAttributeValue<String> table = migrationEntity.getAttribute("name");
        return table.getValue();
    }

    public String entityName(JavaType typeName) {
        return entityName(classDetails(typeName));
    }

    public String entityName(ClassOrInterfaceTypeDetails classDetail) {
        AnnotationMetadata migrationEntity = classDetail.getAnnotation(JpaJavaType.ENTITY);
        AnnotationAttributeValue<String> table = migrationEntity.getAttribute("name");
        return table.getValue();
    }

    public FieldMetadata field(JavaType typeName, JavaSymbolName fieldName) {
        return field(classDetails(typeName), fieldName);
    }

    public FieldMetadata field(ClassOrInterfaceTypeDetails classDetail, JavaSymbolName fieldName) {
        return classDetail.getDeclaredField(fieldName);
    }

    @Override
    public List<? extends FieldMetadata> fields(JavaType typeName) {
        return fields(classDetails(typeName));
    }

    @Override
    public List<? extends FieldMetadata> fields(ClassOrInterfaceTypeDetails classDetail) {
        return classDetail.getDeclaredFields();
    }

    public boolean exist(JavaType typeName) {
        return classDetails(typeName) != null;
    }

    public boolean hasField(JavaType typeName, JavaSymbolName fieldName) {
        return field(typeName, fieldName) != null;
    }

}
