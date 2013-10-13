package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaSymbolName;

public interface RemovePropertyOperations {

    /**
     * Remove given property from given java type
     * @param propertyName
     * @param javaTypeDetails
     */
    void removeFieldFromClass(JavaSymbolName propertyName, ClassOrInterfaceTypeDetails javaTypeDetails);

    /**
     * Create dropColumn record in migration.xml
     * @param table
     * @param schema
     * @param catalog
     * @param columnName
     */
    void dropColumn(String table, String schema, String catalog, String columnName);

}
