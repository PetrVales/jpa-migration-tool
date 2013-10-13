package cz.cvut.fit.valespe.migration;

import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.roo.model.JpaJavaType.COLUMN;

public class JpaFieldDetails extends org.springframework.roo.classpath.operations.jsr303.FieldDetails {

    private String columnDefinition;

    public JpaFieldDetails(String physicalTypeIdentifier, JavaType fieldType, JavaSymbolName fieldName) {
        super(physicalTypeIdentifier, fieldType, fieldName);
    }

    @Override
    public void decorateAnnotationsList(List<AnnotationMetadataBuilder> annotations) {
        super.decorateAnnotationsList(annotations);
        if (columnDefinition == null)
            return;
        boolean added = false;
        for (AnnotationMetadataBuilder annotation: annotations) {
            if (annotation.getAnnotationType().equals(COLUMN)) {
                annotation.addAttribute(new StringAttributeValue(new JavaSymbolName("columnDefinition"), columnDefinition));
                added = true;
            }
        }
        if (!added) {
            AnnotationMetadataBuilder columnBuilder = null;
            final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
            attrs.add(new StringAttributeValue(new JavaSymbolName("columnDefinition"), columnDefinition));
            columnBuilder = new AnnotationMetadataBuilder(COLUMN, attrs);
            annotations.add(columnBuilder);
        }
    }

    public String getColumnDefinition() {
        return columnDefinition;
    }

    public void setColumnDefinition(String columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

}
