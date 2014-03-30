package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class IntroduceParentTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("introduceParent");
    }

    @Test
    public void targetExtendsParent() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Target.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("@MigrationEntity(entityName = \"target\", table = \"target\")"));
        assertTrue(orderClassContent.contains("@DiscriminatorValue(\"TARGET\")"));
        assertTrue(orderClassContent.contains("public class Target extends Parent"));
        assertTrue(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
        assertTrue(orderClassContent.contains("private Integer orderTotal"));
    }

    @Test
    public void createsParentJavaClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Parent.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("@MigrationEntity(entityName = \"parent\", table = \"parent\")"));
        assertTrue(orderClassContent.contains("@Inheritance(strategy = InheritanceType.JOINED)"));
        assertTrue(orderClassContent.contains("@DiscriminatorColumn(name = \"parent_type\")"));
        assertTrue(orderClassContent.contains("public class Parent"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                     "<changeSet>\n" +
                "        <createTable tableName=\"target\"/>\n" +
                "    </changeSet>"));
        assertTrue(migrationContent.contains(
                     "<changeSet>\n" +
                "        <addColumn tableName=\"target\">\n" +
                "            <column name=\"order_total\" type=\"integer\"/>\n" +
                "        </addColumn>\n" +
                "    </changeSet>"));
        assertTrue(migrationContent.contains(
                     "<changeSet>\n" +
                "        <createTable tableName=\"parent\"/>\n" +
                "        <addColumn tableName=\"parent\">\n" +
                "            <column name=\"parent_type\" type=\"varchar2\"/>\n" +
                "        </addColumn>\n" +
                "    </changeSet>"));
    }

}
