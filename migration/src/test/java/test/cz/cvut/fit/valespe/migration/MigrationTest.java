package test.cz.cvut.fit.valespe.migration;

import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test common methods
 */
public class MigrationTest {

    /**
     * Mock field (and column annotation) with given values
     * @param propertyName
     * @param propertyType
     * @param columnName
     * @param columnType
     * @return
     */
    protected FieldMetadata mockProperty(JavaSymbolName propertyName, JavaType propertyType, Object columnName, Object columnType) {
        AnnotationAttributeValue columnNameMock = mock(AnnotationAttributeValue.class);
        when(columnNameMock.getValue()).thenReturn(columnName);
        AnnotationAttributeValue columnTypeMock = mock(AnnotationAttributeValue.class);
        when(columnTypeMock.getValue()).thenReturn(columnType);

        AnnotationMetadata fieldAnnotationMetadata = mock(AnnotationMetadata.class);
        when(fieldAnnotationMetadata.getAttribute("name")).thenReturn(columnNameMock);
        when(fieldAnnotationMetadata.getAttribute("columnDefinition")).thenReturn(columnTypeMock);

        FieldMetadata fieldMetadata = mock(FieldMetadata.class);
        when(fieldMetadata.getAnnotation(new JavaType("javax.persistence.Column"))).thenReturn(fieldAnnotationMetadata);
        when(fieldMetadata.getFieldName()).thenReturn(propertyName);
        when(fieldMetadata.getFieldType()).thenReturn(propertyType);

        return fieldMetadata;
    }
}
