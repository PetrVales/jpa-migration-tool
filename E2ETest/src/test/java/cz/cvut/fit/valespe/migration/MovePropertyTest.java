package cz.cvut.fit.valespe.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MovePropertyTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("moveProperty");
    }

    @Test
    public void createsOrderTotalPropertyInCopyClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Copy.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("private Integer orderTotal"));
        assertTrue(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
    }

    @Test
    public void removesOrderTotalPropertyFromOriginalClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertFalse(orderClassContent.contains("private Integer orderTotal"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Copy_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("getOrderTotal"));
        assertTrue(orderClassContent.contains("setOrderTotal"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                    "<changeSet>\n" +
                "        <addColumn tableName=\"copy\">\n" +
                "            <column name=\"order_total\" type=\"integer\"/>\n" +
                "        </addColumn>\n" +
                "        <sql>UPDATE copy SET order_total (SELECT order_total FROM order WHERE xxx)</sql>\n" +
                "        <dropColumn columnName=\"order_total\" tableName=\"order\"/>\n" +
                "    </changeSet>"));
    }

}
