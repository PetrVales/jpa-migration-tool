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

public class JpaFieldDetailsTest {

    private static final String PHYSICAL_TYPE_IDENTIFIER = "MID:" + PhysicalTypeIdentifier.class.getName() + "#?";
    private static final JavaType FIELD_TYPE = new JavaType("java.lang.Integer");
    private static final JavaSymbolName FIELD_NAME = new JavaSymbolName("fieldName");
    private static final String COLUMN_DEFINITION = "column-definition";

    @Test
    public void annotatesWithColumnDefinitionAttr() {
        JpaFieldDetails jpaFieldDetails = new JpaFieldDetails(PHYSICAL_TYPE_IDENTIFIER, FIELD_TYPE, FIELD_NAME);
        jpaFieldDetails.setColumnDefinition(COLUMN_DEFINITION);

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        jpaFieldDetails.decorateAnnotationsList(annotations);

        assertEquals(COLUMN_DEFINITION, annotations.get(0).getAttributes().get("columnDefinition").getValue());
    }

}
