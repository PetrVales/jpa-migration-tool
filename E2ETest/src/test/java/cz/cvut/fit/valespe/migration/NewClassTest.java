package cz.cvut.fit.valespe.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class NewClassTest extends E2ETest {

    @Before
    public void init() throws Exception {
        runTestScript("newClass");
    }

    @Test
    public void createsOrderJavaClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("public"));
        assertTrue(orderClassContent.contains("class"));
        assertTrue(orderClassContent.contains("Order"));
        assertTrue(orderClassContent.contains("@RooJavaBean")); // Is this annotation necessary?
        assertTrue(orderClassContent.contains("@MigrationEntity"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains("createTable"));
        assertTrue(migrationContent.contains("order"));
    }

    @Test
    public void createsMigrationEntityAspect() throws IOException {
        File aspect = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_Migration_Entity.aj");
        String aspectContent = getFileContent(aspect);

        assertTrue(aspectContent.contains("@Entity"));
        assertTrue(aspectContent.contains("@Table(name = \"order\""));
    }

    @After
    public void clean() throws Exception {
        removeFiles();
    }

}
