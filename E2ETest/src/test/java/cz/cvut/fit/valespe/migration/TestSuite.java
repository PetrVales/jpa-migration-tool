package cz.cvut.fit.valespe.migration;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PushUpTest.class,
        RemoveParentTest.class,
        IntroduceParentTest.class,
        RunRooScriptTest.class,
        InitProjectTest.class,
        MigrationSetupTest.class,
        NewClassTest.class,
        NewPropertyTest.class,
        AddIdTest.class,
        AddStringField.class,
        AddIntegerField.class,
        AddBooleanField.class,
        NewPropertyOneToManyTest.class,
        RemoveClassTest.class,
        RemovePropertyTest.class,
        MergeClassTest.class,
        MovePropertyTest.class,
        SplitClassTest.class
})
public class TestSuite extends TestCase {

    private static Map<String, File> ADD_ONS = new HashMap<String, File>();

    private static String UNINSTALL_SCRIPT = "uninstallAddon";
    private static String INSTALL_SCRIPT = "startAddon";
    private static String ADD_ON_NAME = "{addonName}";
    private static String ADD_ON_URL = "{addonUrl}";
    private static File INSTALL_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

    // FIXME this is ugly to hardwire project locations, but i have no better idea...
    static {
        ADD_ONS.put("cz.cvut.fit.valespe.migration", new File("/home/petr/workspace/diplomka/jpa-refactoring-tool/migration"));
    }

    @BeforeClass
    public static void beforeTests() throws IOException, InterruptedException {
        for (Map.Entry<String, File> entry : ADD_ONS.entrySet()) {
            mvnClean(entry.getValue());
            mvnPackage(entry.getValue());
            rooUninstall(entry.getKey());
            rooInstall(new File(entry.getValue(), "target/" + entry.getKey() + "-0.1.0.BUILD-SNAPSHOT.jar"));
        }
    }

    private static void mvnClean(File location) throws IOException, InterruptedException {
        executeMvnCommand(location, "clean");
    }

    private static void mvnPackage(File location) throws IOException, InterruptedException {
        executeMvnCommand(location, "package");
    }

    private static void executeMvnCommand(File location, String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",  "mvn " + command, "-c");
        processBuilder.directory(location);
        Process process = processBuilder.start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("Failed to run mvn " + command + " on project: " + location.toString());
        }
    }

    private static void rooUninstall(String addonName) throws IOException, InterruptedException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(ADD_ON_NAME, addonName);
        prepareScript(UNINSTALL_SCRIPT, params);
        runRooScript(UNINSTALL_SCRIPT);
        removeScript(UNINSTALL_SCRIPT);
    }

    private static void rooInstall(File addonLocation) throws IOException, InterruptedException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(ADD_ON_URL, addonLocation.getAbsolutePath());
        prepareScript(INSTALL_SCRIPT, params);
        runRooScript(INSTALL_SCRIPT);
        removeScript(INSTALL_SCRIPT);
    }

    private static void prepareScript(String scriptName, Map<String, String> params) throws IOException {
        copyScript(scriptName, getScriptText(scriptName, params));
    }

    private static String getScriptText(String scriptName, Map<String, String> params) throws IOException {
        String scriptText = IOUtils.toString(TestSuite.class.getResourceAsStream(scriptName));
        for (Map.Entry<String, String> entry : params.entrySet()) {
            scriptText = scriptText.replace(entry.getKey(), entry.getValue());
        }
        return scriptText;
    }

    private static void copyScript(String scriptName, String scriptText) throws IOException {
        InputStream script = IOUtils.toInputStream(scriptText);
        File scriptFile = new File(INSTALL_DIRECTORY, scriptName);
        if (scriptFile.createNewFile()) {
            FileOutputStream stream = new FileOutputStream(scriptFile);
            IOUtils.copy(script, stream);
            stream.close();
        }
        script.close();
    }

    private static void runRooScript(String scriptName) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",  "roo < " + scriptName, "-c");
        processBuilder.directory(INSTALL_DIRECTORY);
        Process process = processBuilder.start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("Script to install addon: " + scriptName);
        }
    }

    private static void removeScript(String scriptName) throws IOException {
        File scriptFile = new File(INSTALL_DIRECTORY, scriptName);
        if (scriptFile.exists()) {
            scriptFile.delete();
        }
    }

    @AfterClass
    public static void afterTests() throws IOException, InterruptedException {
        for (Map.Entry<String, File> entry : ADD_ONS.entrySet()) {
//            rooUninstall(entry.getKey());
        }
    }

}
