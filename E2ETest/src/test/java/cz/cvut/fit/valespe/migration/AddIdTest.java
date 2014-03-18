package cz.cvut.fit.valespe.migration;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class AddIdTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("addId");
    }

    @Test
    public void createsIdInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);
        orderClassContent = orderClassContent.replaceAll("\n", " ");
        orderClassContent = orderClassContent.replaceAll("  ", " ");

        assertTrue(orderClassContent.contains("@Column(name = \"id\", columnDefinition = \"bigint\") @Id private Long id"));
        assertTrue(orderClassContent.contains("@Column(name = \"id2\", columnDefinition = \"integer\") @Id private Integer id2"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("public Long Order.getId()"));
        assertTrue(orderClassContent.contains("public void Order.setId(Long id)"));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains("addColumn"));
        assertTrue(migrationContent.contains("name=\"id\""));
        assertTrue(migrationContent.contains("type=\"bigint\""));
        assertTrue(migrationContent.contains("primaryKey=\"true\""));
        assertTrue(migrationContent.contains("primaryKeyName=\"id_pk\""));
    }

}
