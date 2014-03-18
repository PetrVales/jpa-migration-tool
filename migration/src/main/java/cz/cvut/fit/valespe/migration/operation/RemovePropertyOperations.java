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

}
