package cz.cvut.fit.valespe.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class InitMigrationTest extends E2ETest {

    @Before
    public void init() throws Exception {
        runTestScript("initMigration");
    }

    @Test
    public void createsMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migration.exists());
        assertTrue(migrationContent.contains("databaseChangeLog"));
    }

    @After
    public void clean() throws Exception {
        removeFiles();
    }

}
