package cz.cvut.fit.valespe.migration;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class NewPropertyOneToManyTest extends E2ETest {

    @Before
    public void init() throws Exception {
        runTestScript("newPropertyOneToMany");
    }

    @Test
    public void createsOrderTotalPropertyInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("private Integer orderTotal"));
        assertTrue(orderClassContent.contains("private Address address"));
        assertTrue(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("getOrderTotal"));
        assertTrue(orderClassContent.contains("setOrderTotal"));
        assertTrue(orderClassContent.contains("getAddress"));
        assertTrue(orderClassContent.contains("setAddress"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains("addColumn"));
        assertTrue(migrationContent.contains("name=\"order_total\""));
        assertTrue(migrationContent.contains("name=\"address_id\""));
        assertTrue(migrationContent.contains("type=\"integer\""));
    }

}
