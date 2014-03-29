package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PullUpTest extends E2ETest {


    @BeforeClass
    public static void init() throws Exception {
        runTestScript("pullUp");
    }

    @Test
    public void createsTargetJavaClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Target.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("public class Target extends Parent"));
        assertFalse(orderClassContent.contains("private Integer orderTotal"));
        assertFalse(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
    }

    @Test
    public void createsParentJavaClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Parent.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("public class Parent"));
        assertTrue(orderClassContent.contains("private Integer orderTotal"));
        assertTrue(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
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
