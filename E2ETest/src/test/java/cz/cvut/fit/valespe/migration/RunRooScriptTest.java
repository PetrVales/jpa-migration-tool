package cz.cvut.fit.valespe.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class RunRooScriptTest extends E2ERunner {

    @Before
    public void init() throws Exception {
        runTestScript("runRoo");
    }

    @Test
    public void createsLogFile() {
        assertTrue(new File(testDirectory, "log.roo").exists());
    }

    @After
    public void clean() throws Exception {
        removeFiles();
    }

}
