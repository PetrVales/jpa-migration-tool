package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.model.JavaType;

public interface NewClassOperations {

    void createEntity(JavaType className, String entityName, String table, String schema, String catalog);

}