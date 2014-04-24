package cz.cvut.fit.valespe.migration.util;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import java.util.List;

/**
 * Service with common methods for work with classes
 */
public interface ClassCommons {

    public JavaType className(ClassOrInterfaceTypeDetails typeDetails);

    public ClassOrInterfaceTypeDetails classDetails(JavaType typeName);

    public String tableName(JavaType typeName);
    public String tableName(ClassOrInterfaceTypeDetails classDetail);

    public String entityName(JavaType typeName);
    public String entityName(ClassOrInterfaceTypeDetails classDetail);

    public FieldMetadata field(JavaType typeName, JavaSymbolName fieldName);
    public FieldMetadata field(ClassOrInterfaceTypeDetails classDetail, JavaSymbolName fieldName);

    public List<? extends FieldMetadata> fields(JavaType typeName);
    public List<? extends FieldMetadata> fields(ClassOrInterfaceTypeDetails classDetail);

    public boolean exist(JavaType typeName);

    public boolean hasField(JavaType typeName, JavaSymbolName fieldName);
}
