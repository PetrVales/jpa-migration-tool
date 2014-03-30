package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.model.JavaType;

public interface ClassOperations {

    void createClass(JavaType className, String entityName, String table);
    void removeClass(JavaType target);

    void introduceParent(JavaType target, JavaType parent);
    void removeParent(JavaType target);
}