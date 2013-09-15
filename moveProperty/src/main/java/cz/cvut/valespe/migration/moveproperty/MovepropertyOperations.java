package cz.cvut.valespe.migration.moveproperty;

public interface MovepropertyOperations {

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
