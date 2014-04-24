package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemovePropertyTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("removeProperty");
    }

    @Test
    public void removesOrderTotalField() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertFalse(orderClassContent.contains("orderTotal"));
        assertTrue(orderClassContent.contains("otherProperty"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                    "<changeSet>\n" +
                "        <dropColumn columnName=\"order_total\" tableName=\"order\"/>\n" +
                "    </changeSet>"));
    }

    @Test
    public void removesOrderTotalInJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertFalse(orderClassContent.contains("getOrderTotal"));
        assertFalse(orderClassContent.contains("setOrderTotal"));
        assertTrue(orderClassContent.contains("getOtherProperty"));
        assertTrue(orderClassContent.contains("setOtherProperty"));
    }

}
