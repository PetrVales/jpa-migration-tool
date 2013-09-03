package cz.cvut.valespe.migration.removeproperty;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public interface RemovepropertyOperations {

    /**
     * Remove given property from given java type
     * @param propertyName
     * @param javaTypeDetails
     */
    void deleteFieldFromClass(JavaSymbolName propertyName, ClassOrInterfaceTypeDetails javaTypeDetails);

    /**
     * Create dropColumn record in migration.xml
     * @param table
     * @param schema
     * @param catalog
     * @param columnName
     */
    void dropColumn(String table, String schema, String catalog, String columnName);

}
