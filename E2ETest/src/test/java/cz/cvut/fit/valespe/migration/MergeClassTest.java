package cz.cvut.fit.valespe.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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

        assertTrue(orderClassContent.contains("public"));
        assertTrue(orderClassContent.contains("class"));
        assertTrue(orderClassContent.contains("OrderedItem"));
        assertTrue(orderClassContent.contains("@RooJavaBean"));
        assertTrue(orderClassContent.contains("@MigrationEntity"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains("ordered_item"));
    }

    @Test
    public void mergedClassContainsProperties() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/OrderedItem.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("private Integer orderTotal"));
        assertTrue(orderClassContent.contains("private String name"));
    }

    @Test
    public void createsMigrationEntityAspect() throws IOException {
        File aspect = new File(testDirectory, "src/main/java/cz/cvut/OrderedItem_Roo_Migration_Entity.aj");
        String aspectContent = getFileContent(aspect);

        assertTrue(aspectContent.contains("@Entity"));
        assertTrue(aspectContent.contains("@Table(name = \"ordered_item\""));
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
