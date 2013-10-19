package cz.cvut.fit.valespe.migration.operation;

public interface MovePropertyOperations {

    /**
     *
     * @param columnName
     * @param columnType
     * @param fromTable
     * @param fromSchema
     * @param fromCatalog
     * @param toTable
     * @param toSchema
     * @param toCatalog
     */
    void moveColumn(String columnName, String columnType, String fromTable, String fromSchema, String fromCatalog, String toTable, String toSchema, String toCatalog);
}
