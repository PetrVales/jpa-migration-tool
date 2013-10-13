package cz.cvut.fit.valespe.migration.operation;

/**
 * Operations of migration setup command
 */
public interface MigrationSetupOperations {

    /**
     * Indicate commands should be available
     * 
     * @return true when migration.xml exists
     */
    boolean doesMigrationFileExist();

    /**
     * Creates migration.xml in MAIN_RESOURCES folder
     */
    void createMigrationFile();
}
