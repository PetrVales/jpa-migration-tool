package cz.cvut.fit.valespe.migration.operation;

public interface MovePropertyOperations {

    /**
     *
     * @param columnName
     * @param columnType
     * @param fromTable
     * @param toTable
     */
    void moveColumn(String columnName, String columnType, String fromTable, String toTable);
}
