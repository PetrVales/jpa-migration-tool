package cz.cvut.fit.valespe.migration;

import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.roo.model.JpaJavaType.COLUMN;
import static org.springframework.roo.model.JpaJavaType.ID;

public class JpaFieldDetails extends org.springframework.roo.classpath.operations.jsr303.FieldDetails {

    private String columnDefinition;
    private boolean id;

    public JpaFieldDetails(String physicalTypeIdentifier, JavaType fieldType, JavaSymbolName fieldName) {
        super(physicalTypeIdentifier, fieldType, fieldName);
    }

    @Override
    public void decorateAnnotationsList(List<AnnotationMetadataBuilder> annotations) {
        super.decorateAnnotationsList(annotations);
        if (getColumnDefinition() == null)
            return;
        else
            annotatedColumnDefinition(annotations);
        if (isId()) {
            final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
            AnnotationMetadataBuilder columnBuilder = new AnnotationMetadataBuilder(ID, attrs);
            annotations.add(columnBuilder);
        }
    }

    private boolean annotatedColumnDefinition(List<AnnotationMetadataBuilder> annotations) {
        boolean added = false;
        for (AnnotationMetadataBuilder annotation: annotations) {
            if (annotation.getAnnotationType().equals(COLUMN)) {
                annotation.addAttribute(new StringAttributeValue(new JavaSymbolName("columnDefinition"), getColumnDefinition()));
                added = true;
            }
        }
        if (!added) {
            final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
            attrs.add(new StringAttributeValue(new JavaSymbolName("columnDefinition"), getColumnDefinition()));
            AnnotationMetadataBuilder columnBuilder = new AnnotationMetadataBuilder(COLUMN, attrs);
            annotations.add(columnBuilder);
        }
        return added;
    }

    public String getColumnDefinition() {
        return columnDefinition;
    }

    public void setColumnDefinition(String columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

}
