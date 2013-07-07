package cz.cvut.valespe.migration.setup;

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
