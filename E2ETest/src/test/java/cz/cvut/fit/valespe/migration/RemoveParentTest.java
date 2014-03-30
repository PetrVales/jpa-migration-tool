package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemoveParentTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("removeParent");
    }

    @Test
    public void removeExtendsType() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Target.java");
        String orderClassContent = getFileContent(orderClass);

        assertFalse(orderClassContent.contains("public class Target extends Parent"));
        assertFalse(orderClassContent.contains("@DiscriminatorValue(\"TARGET\")"));
        assertTrue(orderClassContent.contains("public class Target"));
        assertTrue(orderClassContent.contains("private Integer orderTotal"));
        assertTrue(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
    }

    @Test
    public void maintainParentJavaClass() throws IOException {
        File parentClass = new File(testDirectory, "src/main/java/cz/cvut/Parent.java");
        String orderClassContent = getFileContent(parentClass);

        assertTrue(orderClassContent.contains("@MigrationEntity(entityName = \"parent\", table = \"parent\")"));
        assertTrue(orderClassContent.contains("@Inheritance(strategy = InheritanceType.JOINED)"));
        assertTrue(orderClassContent.contains("@DiscriminatorColumn(name = \"parent_type\")"));
        assertTrue(orderClassContent.contains("public class Parent"));
    }

//    @Test
//    public void createsRecordInMigrationFile() throws IOException {
//        File migration = new File(testDirectory, "src/main/resources/migration.xml");
//        String migrationContent = getFileContent(migration);
//
//        assertTrue(migrationContent.contains("createTable"));
//        assertTrue(migrationContent.contains("order"));
//    }
//
//    @Test
//    public void createsMigrationEntityAspect() throws IOException {
//        File aspect = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_Migration_Entity.aj");
//        String aspectContent = getFileContent(aspect);
//
//        assertTrue(aspectContent.contains("@Entity"));
//        assertTrue(aspectContent.contains("@Table(name = \"order\""));
//    }

}