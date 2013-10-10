package cz.cvut.fit.valespe.migration;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SplitClassTest extends E2ETest {

    private final String expectedMigrationContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd                             http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
            "\n" +
            "<changeSet>\n" +
            "        <createTable tableName=\"original\"/>\n" +
            "    </changeSet>\n" +
            "<changeSet>\n" +
            "        <addColumn tableName=\"original\">\n" +
            "            <column name=\"a\" type=\"integer\"/>\n" +
            "        </addColumn>\n" +
            "    </changeSet>\n" +
            "<changeSet>\n" +
            "        <addColumn tableName=\"original\">\n" +
            "            <column name=\"b\" type=\"integer\"/>\n" +
            "        </addColumn>\n" +
            "    </changeSet>\n" +
            "<changeSet>\n" +
            "        <addColumn tableName=\"original\">\n" +
            "            <column name=\"common\" type=\"integer\"/>\n" +
            "        </addColumn>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            try {
                logDirectoryStructure();
                logFileContents();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected void finished(Description description) {
            try {
                removeFiles();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Before
    public void init() throws Exception {
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
        assertTrue(classContent.contains("@MigrationEntity"));

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
        assertTrue(classContent.contains("@MigrationEntity"));

        assertTrue(classContent.contains("private Integer b"));
        assertTrue(classContent.contains("@Column(name = \"b\", columnDefinition = \"integer\")"));

        assertTrue(classContent.contains("private Integer common"));
        assertTrue(classContent.contains("@Column(name = \"common\", columnDefinition = \"integer\")"));
    }

    @Test
    public void createsAspectsForAClass() throws IOException {
        File entityAspect = new File(testDirectory, "src/main/java/cz/cvut/A_Roo_Migration_Entity.aj");
        File beanAspect = new File(testDirectory, "src/main/java/cz/cvut/A_Roo_JavaBean.aj");
        String entityAspectContent = getFileContent(entityAspect);
        String beanAspectContent = getFileContent(beanAspect);

        assertTrue(entityAspectContent.contains("@Entity"));
        assertTrue(entityAspectContent.contains("@Table(name = \"a_table\""));

        assertTrue(beanAspectContent.contains("getA"));
        assertTrue(beanAspectContent.contains("setA"));
        assertTrue(beanAspectContent.contains("getCommon"));
        assertTrue(beanAspectContent.contains("setCommon"));
    }

    @Test
    public void createsAspectsForBClass() throws IOException {
        File entityAspect = new File(testDirectory, "src/main/java/cz/cvut/B_Roo_Migration_Entity.aj");
        File beanAspect = new File(testDirectory, "src/main/java/cz/cvut/B_Roo_JavaBean.aj");
        String entityAspectContent = getFileContent(entityAspect);
        String beanAspectContent = getFileContent(beanAspect);

        assertTrue(entityAspectContent.contains("@Entity"));
        assertTrue(entityAspectContent.contains("@Table(name = \"b_table\""));

        assertTrue(beanAspectContent.contains("getB"));
        assertTrue(beanAspectContent.contains("setB"));
        assertTrue(beanAspectContent.contains("getCommon"));
        assertTrue(beanAspectContent.contains("setCommon"));
    }

    @Test
    public void createsRecordsInMigrationFile() throws IOException, SAXException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String actualMigrationContent = getFileContent(migration);

        Diff diff = XMLUnit.compareXML(expectedMigrationContent, actualMigrationContent);
        if (!diff.similar()) {
            System.err.println(diff.toString());
            fail();
        } else {
            assertTrue(true);
        }
    }

}
