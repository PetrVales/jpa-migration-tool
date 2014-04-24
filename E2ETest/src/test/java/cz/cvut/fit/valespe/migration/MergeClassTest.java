package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MergeClassTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("mergeClass");
    }

    @Test
    public void createsMergedJavaClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/OrderedItem.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("@RooJavaBean"));
        assertTrue(orderClassContent.contains("@Entity(name = \"ordered_item\")"));
        assertTrue(orderClassContent.contains("@Table(name = \"ordered_item\")"));
        assertTrue(orderClassContent.contains("public class OrderedItem"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                     "<changeSet>\n" +
                 "        <createTable tableName=\"ordered_item\"/>\n" +
                 "        <addColumn tableName=\"ordered_item\">\n" +
                 "            <column name=\"order_total\" type=\"integer\"/>\n" +
                 "        </addColumn>\n" +
                 "        <addColumn tableName=\"ordered_item\">\n" +
                 "            <column name=\"name\" type=\"varchar(256)\"/>\n" +
                 "        </addColumn>\n" +
                 "        <sql>INSERT INTO ordered_item(order_total, name) (SELECT order_total, name FROM order JOIN item ON query)</sql>\n" +
                 "        <dropTable cascadeConstraints=\"false\" tableName=\"order\"/>\n" +
                 "        <dropTable cascadeConstraints=\"false\" tableName=\"item\"/>\n" +
                 "    </changeSet>"));
    }

    @Test
    public void mergedClassContainsProperties() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/OrderedItem.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
        assertTrue(orderClassContent.contains("private Integer orderTotal"));
        assertTrue(orderClassContent.contains("@Column(name = \"name\", columnDefinition = \"varchar(256)\")"));
        assertTrue(orderClassContent.contains("private String name"));
    }

    @Test
    public void removesOldClasses() throws IOException {
        File item = new File(testDirectory, "src/main/java/cz/cvut/Item.java");
        File order = new File(testDirectory, "src/main/java/cz/cvut/Order.java");

        assertFalse(item.exists());
        assertFalse(order.exists());
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/OrderedItem_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("getOrderTotal"));
        assertTrue(orderClassContent.contains("setOrderTotal"));
        assertTrue(orderClassContent.contains("getName"));
        assertTrue(orderClassContent.contains("setName"));
    }

}
