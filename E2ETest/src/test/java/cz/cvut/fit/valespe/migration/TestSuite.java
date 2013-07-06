package cz.cvut.fit.valespe.migration;

import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RunWith(Suite.class)
@Suite.SuiteClasses({RunRooScriptTest.class, InitProjectTest.class})
public class TestSuite extends TestCase {



    private static Map<String, File> ADDONS = new HashMap<String, File>();

    static {

    }

    @BeforeClass
    public static void doYourOneTimeSetup() {

    }

    @AfterClass
    public static void doYourOneTimeTeardown() {

    }

}
