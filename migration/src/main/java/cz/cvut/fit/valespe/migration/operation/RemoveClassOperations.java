package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.model.JavaType;

public interface RemoveClassOperations {

    /**
     * Remove given type
     * @param target type to remove
     */
    void removeClass(JavaType target);

}