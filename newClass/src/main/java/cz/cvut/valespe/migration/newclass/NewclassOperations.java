package cz.cvut.valespe.migration.newclass;

import org.springframework.roo.model.JavaType;

public interface NewclassOperations {

    boolean isCommandAvailable();

    void createEntity(JavaType className, String entityName, String table, String schema, String catalog);

    void createTable(String table, String schema, String catalog, String tablespace);
}