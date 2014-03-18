package cz.cvut.fit.valespe.migration;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class AddStringField extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("addStringField");
    }

    @Test
    public void createsIdInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("private String name"));
        assertTrue(orderClassContent.contains("@Column(name = \"name-column\", columnDefinition = \"varchar2(255)\")"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("public String Order.getName()"));
        assertTrue(orderClassContent.contains("public void Order.setName(String name)"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains("addColumn"));
        assertTrue(migrationContent.contains("name=\"name-column\""));
        assertTrue(migrationContent.contains("type=\"varchar2(255)\""));
    }

}
