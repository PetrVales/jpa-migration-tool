package cz.cvut.fit.valespe.migration;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class AddIntegerField extends E2ETest {

    @Before
    public void init() throws Exception {
        runTestScript("addIntegerField");
    }

    @Test
    public void createsIdInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("private Integer count"));
        assertTrue(orderClassContent.contains("@Column(name = \"count-column\", columnDefinition = \"integer\")"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("public Integer Order.getCount()"));
        assertTrue(orderClassContent.contains("public void Order.setCount(Integer count)"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains("addColumn"));
        assertTrue(migrationContent.contains("name=\"count-column\""));
        assertTrue(migrationContent.contains("type=\"integer\""));
    }

}
