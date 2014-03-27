package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.model.JavaType;

public interface ClassOperations {

    void createClass(JavaType className, String entityName, String table);
    void createClass(JavaType className);
    void removeClass(JavaType target);

    void addParentClass(JavaType target, JavaType parent);
}