package cz.cvut.fit.valespe.migration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class InitProjectTest extends E2ETest {

    @BeforeClass
    public static void init() throws Exception {
        runTestScript("initProject");
    }

    @Test
    public void createsLogFile() {
        assertTrue(new File(testDirectory, "log.roo").exists());
    }

    @Test
    public void createsPomFile() {
        assertTrue(new File(testDirectory, "pom.xml").exists());
    }

    @Test
    public void createsSrcDir() {
        assertTrue(new File(testDirectory, "src").exists());
        assertTrue(new File(testDirectory, "src").isDirectory());
    }

    @Test
    public void createsResourcesDir() {
        assertTrue(new File(testDirectory, "src/main/resources").exists());
        assertTrue(new File(testDirectory, "src/main/resources").isDirectory());
    }

}
