package cz.cvut.fit.valespe.migration.util.impl;

import cz.cvut.fit.valespe.migration.util.FieldCommons;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JpaJavaType;

@Component
@Service
public class FieldCommonsImpl implements FieldCommons {

    public JavaSymbolName fieldName(FieldMetadata field) {
        return field.getFieldName();
    }

    public JavaType fieldType(FieldMetadata field) {
        return field.getFieldType();
    }

    public String columnName(FieldMetadata field) {
        AnnotationMetadata column = getAnootation(field);
        AnnotationAttributeValue<String> columnName = column.getAttribute("name");
        return columnName.getValue();
    }

    public String columnType(FieldMetadata field) {
        AnnotationMetadata column = getAnootation(field);
        AnnotationAttributeValue<String> columnName = column.getAttribute("columnDefinition");
        return columnName.getValue();
    }

    private AnnotationMetadata getAnootation(FieldMetadata field) {
        AnnotationMetadata column = field.getAnnotation(JpaJavaType.COLUMN);
        if (column != null)
            return column;
        else
            return field.getAnnotation(JpaJavaType.JOIN_COLUMN);
    }

}
