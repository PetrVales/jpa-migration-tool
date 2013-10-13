package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public interface NewPropertyOperations {

    /**
     * creates migration record in migration file
     */
    void createColumn(String table, String schema, String catalog, String columnName, String columnType);

    /**
     * Add field with given name and type to class of given class
     * @param propertyName
     * @param propertyType
     * @param columnName
     * @param classType
     */
    void addFieldToClass(JavaSymbolName propertyName, JavaType propertyType, String columnName, String columnType, ClassOrInterfaceTypeDetails classType);
}