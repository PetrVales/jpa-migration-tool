package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class NewPropertyTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("newProperty");
    }

    @Test
    public void createsOrderTotalPropertyInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
        assertTrue(orderClassContent.contains("private Integer orderTotal"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("getOrderTotal"));
        assertTrue(orderClassContent.contains("setOrderTotal"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                "        <addColumn tableName=\"order\">\n" +
                "            <column name=\"order_total\" type=\"integer\"/>\n" +
                "        </addColumn>"));
    }

}
