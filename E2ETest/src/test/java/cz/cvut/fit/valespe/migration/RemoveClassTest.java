package cz.cvut.fit.valespe.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemoveClassTest extends E2ETest {

    @Before
    public void init() throws Exception {
        runTestScript("removeClass");
    }

    @Test
    public void removesOrderJavaClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");

        assertFalse(orderClass.exists());
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains("dropTable"));
        assertTrue(migrationContent.contains("order"));
    }

    @Test
    public void removesMigrationEntityAspect() throws IOException {
        File aspect = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_Migration_Entity.aj");

        assertFalse(aspect.exists());
    }

    @After
    public void clean() throws Exception {
        removeFiles();
    }

}
