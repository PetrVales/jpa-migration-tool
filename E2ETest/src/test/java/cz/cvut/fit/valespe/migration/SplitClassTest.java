package cz.cvut.fit.valespe.migration;

import org.junit.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SplitClassTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("splitClass");
    }

    @Test
    public void removesOriginalClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Original.java");

        assertFalse(orderClass.exists());
    }

    @Test
    public void createsClassA() throws IOException {
        File aClass = new File(testDirectory, "src/main/java/cz/cvut/A.java");
        String classContent = getFileContent(aClass);

        assertTrue(classContent.contains("public"));
        assertTrue(classContent.contains("class"));
        assertTrue(classContent.contains("A"));
        assertTrue(classContent.contains("@RooJavaBean"));
        assertTrue(classContent.contains("@Entity(name = \"a_table\")"));
        assertTrue(classContent.contains("@Table(name = \"a_table\")"));

        assertTrue(classContent.contains("private Integer a"));
        assertTrue(classContent.contains("@Column(name = \"a\", columnDefinition = \"integer\")"));

        assertTrue(classContent.contains("private Integer common"));
        assertTrue(classContent.contains("@Column(name = \"common\", columnDefinition = \"integer\")"));
    }

    @Test
    public void createsClassB() throws IOException {
        File bClass = new File(testDirectory, "src/main/java/cz/cvut/B.java");
        String classContent = getFileContent(bClass);

        assertTrue(classContent.contains("public"));
        assertTrue(classContent.contains("class"));
        assertTrue(classContent.contains("B"));
        assertTrue(classContent.contains("@RooJavaBean"));
        assertTrue(classContent.contains("@Entity(name = \"b_table\")"));
        assertTrue(classContent.contains("@Table(name = \"b_table\")"));

        assertTrue(classContent.contains("private Integer b"));
        assertTrue(classContent.contains("@Column(name = \"b\", columnDefinition = \"integer\")"));

        assertTrue(classContent.contains("private Integer common"));
        assertTrue(classContent.contains("@Column(name = \"common\", columnDefinition = \"integer\")"));
    }

    @Test
    public void createsAspectsForAClass() throws IOException {
        File beanAspect = new File(testDirectory, "src/main/java/cz/cvut/A_Roo_JavaBean.aj");
        String beanAspectContent = getFileContent(beanAspect);

        assertTrue(beanAspectContent.contains("getA"));
        assertTrue(beanAspectContent.contains("setA"));
        assertTrue(beanAspectContent.contains("getCommon"));
        assertTrue(beanAspectContent.contains("setCommon"));
    }

    @Test
    public void createsAspectsForBClass() throws IOException {
        File beanAspect = new File(testDirectory, "src/main/java/cz/cvut/B_Roo_JavaBean.aj");
        String beanAspectContent = getFileContent(beanAspect);

        assertTrue(beanAspectContent.contains("getB"));
        assertTrue(beanAspectContent.contains("setB"));
        assertTrue(beanAspectContent.contains("getCommon"));
        assertTrue(beanAspectContent.contains("setCommon"));
    }

    @Test
    public void createsRecordsInMigrationFile() throws IOException, SAXException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                    "<changeSet>\n" +
                "        <createTable tableName=\"a_table\"/>\n" +
                "        <createTable tableName=\"b_table\"/>\n" +
                "        <addColumn tableName=\"a_table\">\n" +
                "            <column name=\"a\" type=\"integer\"/>\n" +
                "        </addColumn>\n" +
                "        <addColumn tableName=\"a_table\">\n" +
                "            <column name=\"common\" type=\"integer\"/>\n" +
                "        </addColumn>\n" +
                "        <addColumn tableName=\"b_table\">\n" +
                "            <column name=\"b\" type=\"integer\"/>\n" +
                "        </addColumn>\n" +
                "        <addColumn tableName=\"b_table\">\n" +
                "            <column name=\"common\" type=\"integer\"/>\n" +
                "        </addColumn>\n" +
                "        <sql>INSERT INTO a_table(a, common) (SELECT a, common FROM original WHERE queryA)</sql>\n" +
                "        <sql>INSERT INTO b_table(b, common) (SELECT b, common FROM original WHERE queryB)</sql>\n" +
                "        <dropTable cascadeConstraints=\"true\" tableName=\"original\"/>\n" +
                "    </changeSet>"));
    }

}
