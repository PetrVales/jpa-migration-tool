package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class NewPropertyOneToOneTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("newPropertyOneToOne");
    }

    @Test
    public void createsAddressPropertyInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains(
                    "@OneToOne\n" +
                "    @JoinColumn(columnDefinition = \"int\", name = \"address_id\")\n" +
                "    private Address address;"));
        assertTrue(orderClassContent.contains(
                    "@OneToOne(mappedBy = \"order\")\n" +
                "    private Address address2;"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("getAddress"));
        assertTrue(orderClassContent.contains("setAddress"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                        "        <addColumn tableName=\"order\">\n" +
                        "            <column name=\"address_id\" type=\"int\"/>\n" +
                        "        </addColumn>"));
    }

}
