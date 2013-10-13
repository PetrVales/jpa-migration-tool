package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.model.JavaType;

public interface RemoveClassOperations {

    /**
     * Remove given type
     * @param target type to remove
     */
    void removeClass(JavaType target);

    /**
     * Create drop table record in migration.xml
     * @param table
     * @param schema
     * @param catalog
     * @param cascade
     */
    void dropTable(String table, String schema, String catalog, boolean cascade);
}