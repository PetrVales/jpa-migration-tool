package cz.cvut.valespe.migration.removeclass;

import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers. Typically used by a command type or an external add-on.
 *
 * @since 1.1
 */
public interface RemoveclassOperations {

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

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