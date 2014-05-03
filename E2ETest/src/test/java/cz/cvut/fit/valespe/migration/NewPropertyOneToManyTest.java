package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class NewPropertyOneToManyTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("newPropertyOneToMany");
    }

    @Test
    public void createsAddressPropertyInClass() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order.java");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains(
                    "@OneToMany(mappedBy = \"order\")\n" +
                "    private Address address2;"));
    }

    @Test
    public void createsJavaBeanAspect() throws IOException {
        File orderClass = new File(testDirectory, "src/main/java/cz/cvut/Order_Roo_JavaBean.aj");
        String orderClassContent = getFileContent(orderClass);

        assertTrue(orderClassContent.contains("public Address Order.getAddress2()"));
        assertTrue(orderClassContent.contains("public void Order.setAddress2(Address address2)"));
    }

}
