package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class MakeIdTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("makeId");
    }

    @Test
    public void createsIdInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains(
                "    @Column(name = \"id2\", columnDefinition = \"integer\")\n" +
                "    @Id\n" +
                "    private Integer id2;"
        ));
    }

    @Test
    public void createsRecordInMigrationFile() throws IOException {
        File migration = new File(testDirectory, "src/main/resources/migration.xml");
        String migrationContent = getFileContent(migration);

        assertTrue(migrationContent.contains(
                    "<changeSet>\n" +
                "        <addPrimaryKey columnNames=\"id2\" constraintName=\"id2_pk\" tableName=\"order\"/>\n" +
                "    </changeSet>"
        ));
    }

}
