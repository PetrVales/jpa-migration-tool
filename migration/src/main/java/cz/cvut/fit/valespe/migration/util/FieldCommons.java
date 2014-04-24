package cz.cvut.fit.valespe.migration.util;

import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Service with common methods for work with fields
 */
public interface FieldCommons {

    public JavaSymbolName fieldName(FieldMetadata field);

    public JavaType fieldType(FieldMetadata field);

    public String columnName(FieldMetadata field);

    public String columnType(FieldMetadata field);

}
