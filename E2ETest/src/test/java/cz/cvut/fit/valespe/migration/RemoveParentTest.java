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
        assertTrue(orderClassContent.contains("@Entity(name = \"target\")"));
        assertTrue(orderClassContent.contains("@Table(name = \"target\")"));
        assertTrue(orderClassContent.contains("public class Target"));
        assertTrue(orderClassContent.contains("private Integer orderTotal"));
        assertTrue(orderClassContent.contains("@Column(name = \"order_total\", columnDefinition = \"integer\")"));
    }

    @Test
    public void maintainParentJavaClass() throws IOException {
        File parentClass = new File(testDirectory, "src/main/java/cz/cvut/Parent.java");
        String orderClassContent = getFileContent(parentClass);

        assertTrue(orderClassContent.contains("@Entity(name = \"parent\")"));
        assertTrue(orderClassContent.contains("@Table(name = \"parent\")"));
        assertTrue(orderClassContent.contains("@Inheritance(strategy = InheritanceType.JOINED)"));
        assertTrue(orderClassContent.contains("@DiscriminatorColumn(name = \"parent_type\")"));
        assertTrue(orderClassContent.contains("public class Parent"));
    }

}