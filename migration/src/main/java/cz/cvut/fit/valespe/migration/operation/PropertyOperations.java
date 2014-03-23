package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public interface PropertyOperations {

    void addField(
            JavaSymbolName propertyName,
            JavaType propertyType,
            String columnName,
            String columnType,
            ClassOrInterfaceTypeDetails classType
    );

    void addField(
            JavaSymbolName propertyName,
            JavaType propertyType,
            String columnName,
            String columnType,
            ClassOrInterfaceTypeDetails classType,
            boolean id
    );

    void removeField(JavaSymbolName propertyName, ClassOrInterfaceTypeDetails javaTypeDetails);

}