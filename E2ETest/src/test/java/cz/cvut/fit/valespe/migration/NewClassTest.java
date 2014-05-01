package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class NewClassTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("newClass");
    }

    @Test
    public void createsOrderJavaClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("@RooJavaBean")); // Is this annotation necessary?
        assertTrue(orderClassContent.contains("@Entity(name = \"order\")"));
        assertTrue(orderClassContent.contains("@Table(name = \"order\")"));
        assertTrue(orderClassContent.contains("public class Order"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                    "<changeSet>\n" +
                "        <createTable tableName=\"order\"/>\n" +
                "    </changeSet>"));
    }

}
