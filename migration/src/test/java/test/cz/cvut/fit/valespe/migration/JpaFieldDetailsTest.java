package test.cz.cvut.fit.valespe.migration;

import cz.cvut.fit.valespe.migration.JpaFieldDetails;
import org.junit.Test;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.roo.model.JpaJavaType.*;

public class JpaFieldDetailsTest {

    private static final String PHYSICAL_TYPE_IDENTIFIER = "MID:" + PhysicalTypeIdentifier.class.getName() + "#?";
    private static final JavaType FIELD_TYPE = new JavaType("java.lang.Integer");
    private static final JavaSymbolName FIELD_NAME = new JavaSymbolName("fieldName");
    private static final String COLUMN_NAME_VALUE = "column-name-value";
    private static final String COLUMN_DEFINITION_VALUE = "column-definition-value";
    private static final String MAPPED_BY_VALES = "mapped-by-value";

    @Test
    public void annotatesWithColumnDefinitionAttr() {
        JpaFieldDetails jpaFieldDetails = new JpaFieldDetails(PHYSICAL_TYPE_IDENTIFIER, FIELD_TYPE, FIELD_NAME, COLUMN_NAME_VALUE, COLUMN_DEFINITION_VALUE);

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        jpaFieldDetails.decorateAnnotationsList(annotations);

        assertEquals(COLUMN_NAME_VALUE, annotations.get(0).getAttributes().get("name").getValue());
        assertEquals(COLUMN_DEFINITION_VALUE, annotations.get(0).getAttributes().get("columnDefinition").getValue());
    }

    @Test
    public void annotatesWithId() {
        JpaFieldDetails jpaFieldDetails = new JpaFieldDetails(PHYSICAL_TYPE_IDENTIFIER, FIELD_TYPE, FIELD_NAME, COLUMN_NAME_VALUE, COLUMN_DEFINITION_VALUE);
        jpaFieldDetails.setId(true);


        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        jpaFieldDetails.decorateAnnotationsList(annotations);

        assertEquals(ID, annotations.get(1).getAnnotationType());
    }

    @Test
    public void annotatesWithOneToOne() {
        JpaFieldDetails jpaFieldDetails = new JpaFieldDetails(PHYSICAL_TYPE_IDENTIFIER, FIELD_TYPE, FIELD_NAME, COLUMN_NAME_VALUE, COLUMN_DEFINITION_VALUE);
        jpaFieldDetails.setOneToOne(true);
        jpaFieldDetails.setMappedBy(MAPPED_BY_VALES);


        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        jpaFieldDetails.decorateAnnotationsList(annotations);

        assertEquals(ONE_TO_ONE, annotations.get(0).getAnnotationType());
        assertEquals(MAPPED_BY_VALES, annotations.get(0).getAttributes().get("mappedBy").getValue());
    }

    @Test
    public void annotatesWithOneToOneWithoutMappedBy() {
        JpaFieldDetails jpaFieldDetails = new JpaFieldDetails(PHYSICAL_TYPE_IDENTIFIER, FIELD_TYPE, FIELD_NAME, COLUMN_NAME_VALUE, COLUMN_DEFINITION_VALUE);
        jpaFieldDetails.setOneToOne(true);


        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        jpaFieldDetails.decorateAnnotationsList(annotations);

        assertEquals(ONE_TO_ONE, annotations.get(0).getAnnotationType());
        assertEquals(JOIN_COLUMN, annotations.get(1).getAnnotationType());
        assertEquals(COLUMN_NAME_VALUE, annotations.get(1).getAttributes().get("name").getValue());
        assertEquals(COLUMN_DEFINITION_VALUE, annotations.get(1).getAttributes().get("columnDefinition").getValue());
    }

    @Test
    public void annotatesWithManyToOne() {
        JpaFieldDetails jpaFieldDetails = new JpaFieldDetails(PHYSICAL_TYPE_IDENTIFIER, FIELD_TYPE, FIELD_NAME, COLUMN_NAME_VALUE, COLUMN_DEFINITION_VALUE);
        jpaFieldDetails.setManyToOne(true);


        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        jpaFieldDetails.decorateAnnotationsList(annotations);

        assertEquals(MANY_TO_ONE, annotations.get(0).getAnnotationType());
    }

    @Test
    public void annotatesWithOneToMany() {
        JpaFieldDetails jpaFieldDetails = new JpaFieldDetails(PHYSICAL_TYPE_IDENTIFIER, FIELD_TYPE, FIELD_NAME, COLUMN_NAME_VALUE, COLUMN_DEFINITION_VALUE);
        jpaFieldDetails.setOneToMany(true);
        jpaFieldDetails.setMappedBy(MAPPED_BY_VALES);


        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        jpaFieldDetails.decorateAnnotationsList(annotations);

        assertEquals(ONE_TO_MANY, annotations.get(0).getAnnotationType());
        assertEquals(MAPPED_BY_VALES, annotations.get(0).getAttributes().get("mappedBy").getValue());
    }

}
