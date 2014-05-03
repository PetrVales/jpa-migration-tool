package cz.cvut.fit.valespe.migration;

import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.roo.model.JpaJavaType.*;

public class JpaFieldDetails extends org.springframework.roo.classpath.operations.jsr303.FieldDetails {

    private String columnDefinition;
    private boolean id;
    private boolean oneToOne;
    private String mappedBy;
    private boolean oneToMany;
    private boolean manyToOne;
    private boolean manyToMany;

    public JpaFieldDetails(String physicalTypeIdentifier, JavaType fieldType, JavaSymbolName fieldName, String columnName, String columnDefinition) {
        super(physicalTypeIdentifier, fieldType, fieldName);
        setColumn(columnName);
        this.columnDefinition = columnDefinition;
    }

    @Override
    public void decorateAnnotationsList(List<AnnotationMetadataBuilder> annotations) {
        if (!oneToOne && !manyToOne && !oneToMany)
            annotateColumn(annotations);
        if (id)
            annotateId(annotations);
        if (oneToOne) {
            annotateOneToOne(annotations);
            if (mappedBy == null)
                annotateJoinColumn(annotations);
        }
        if (oneToMany) {
            annotateOneToMany(annotations);
        }
        if (manyToOne) {
            annotateManyToOne(annotations);
            annotateJoinColumn(annotations);
        }
    }

    private void annotateColumn(List<AnnotationMetadataBuilder> annotations) {
        final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
        attrs.add(new StringAttributeValue(new JavaSymbolName("name"), getColumn()));
        attrs.add(new StringAttributeValue(new JavaSymbolName("columnDefinition"), getColumnDefinition()));
        AnnotationMetadataBuilder columnBuilder = new AnnotationMetadataBuilder(COLUMN, attrs);
        annotations.add(columnBuilder);
    }

    private void annotateId(List<AnnotationMetadataBuilder> annotations) {
        final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
        AnnotationMetadataBuilder columnBuilder = new AnnotationMetadataBuilder(ID, attrs);
        annotations.add(columnBuilder);
    }

    private void annotateJoinColumn(List<AnnotationMetadataBuilder> annotations) {
        final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
        attrs.add(new StringAttributeValue(new JavaSymbolName("columnDefinition"), getColumnDefinition()));
        attrs.add(new StringAttributeValue(new JavaSymbolName("name"), getColumn()));
        AnnotationMetadataBuilder columnBuilder = new AnnotationMetadataBuilder(JOIN_COLUMN, attrs);
        annotations.add(columnBuilder);
    }

    private void annotateOneToOne(List<AnnotationMetadataBuilder> annotations) {
        final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
        if (getMappedBy() != null)
            attrs.add(new StringAttributeValue(new JavaSymbolName("mappedBy"), getMappedBy()));
        AnnotationMetadataBuilder columnBuilder = new AnnotationMetadataBuilder(ONE_TO_ONE, attrs);
        annotations.add(columnBuilder);
    }

    private void annotateOneToMany(List<AnnotationMetadataBuilder> annotations) {
        final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
        if (getMappedBy() != null)
            attrs.add(new StringAttributeValue(new JavaSymbolName("mappedBy"), getMappedBy()));
        AnnotationMetadataBuilder columnBuilder = new AnnotationMetadataBuilder(ONE_TO_MANY, attrs);
        annotations.add(columnBuilder);
    }

    private void annotateManyToOne(List<AnnotationMetadataBuilder> annotations) {
        final List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
        AnnotationMetadataBuilder columnBuilder = new AnnotationMetadataBuilder(MANY_TO_ONE, attrs);
        annotations.add(columnBuilder);
    }

    private void annotateManyToMany(List<AnnotationMetadataBuilder> annotations) {
        throw new IllegalStateException("Not implemented, yet.");
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

    public boolean isOneToOne() {
        return oneToOne;
    }

    public void setOneToOne(boolean oneToOne) {
        this.oneToOne = oneToOne;
    }

    public String getMappedBy() {
        return mappedBy;
    }

    public void setMappedBy(String mappedBy) {
        this.mappedBy = mappedBy;
    }

    public void setOneToMany(boolean oneToMany) {
        this.oneToMany = oneToMany;
    }

    public boolean isOneToMany() {
        return oneToMany;
    }

    public void setManyToOne(boolean manyToOne) {
        this.manyToOne = manyToOne;
    }

    public boolean isManyToOne() {
        return manyToOne;
    }

    public void setManyToMany(boolean manyToMany) {
        this.manyToMany = manyToMany;
    }

    public boolean isManyToMany() {
        return manyToMany;
    }
}
