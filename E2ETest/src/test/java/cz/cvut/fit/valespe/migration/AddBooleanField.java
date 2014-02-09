package cz.cvut.fit.valespe.migration;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class AddBooleanField extends E2ETest {

    @Before
    public void init() throws Exception {
        runTestScript("addBooleanField");
    }

    @Test
    public void createsIdInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("private Boolean shipped"));
        assertTrue(orderClassContent.contains("@Column(name = \"shipped-column\", columnDefinition = \"boolean\")"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("public Boolean Order.getShipped()"));
        assertTrue(orderClassContent.contains("public void Order.setShipped(Boolean shipped)"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains("addColumn"));
        assertTrue(migrationContent.contains("name=\"shipped-column\""));
        assertTrue(migrationContent.contains("type=\"boolean\""));
    }

}
