package cz.cvut.fit.valespe.migration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.*;
import java.util.Collection;

public class E2ETest {

    protected static File testDirectory;

    @ClassRule
    public static TestWatcher classWatchman = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            try {
                removeFiles();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            try {
                logDirectoryStructure();
                logFileContents();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    protected static void runTestScript(String scriptName) throws IOException, InterruptedException {
        createTestDirectory(scriptName);
        copyScript(scriptName);
        runScript(scriptName);
    }

    private static void createTestDirectory(String scriptName) throws IOException {
        testDirectory = File.createTempFile("tmp_" + scriptName + "_", Long.toString(System.nanoTime()));
        if(!(testDirectory.delete())) {
            throw new IOException("Could not delete temp file: " + testDirectory.getAbsolutePath());
        }
        if(!(testDirectory.mkdir())) {
            throw new IOException("Could not create temp directory: " + testDirectory.getAbsolutePath());
        }
    }

    private static void copyScript(String scriptName) throws IOException {
        InputStream script = E2ETest.class.getResourceAsStream(scriptName);
        File scriptFile = new File(testDirectory, scriptName);
        if (scriptFile.createNewFile()) {
            FileOutputStream stream = new FileOutputStream(scriptFile);
            IOUtils.copy(script, stream);
            stream.close();
        }
        script.close();
    }

    private static void runScript(String scriptName) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",  "roo < " + scriptName, "-c");
        processBuilder.directory(testDirectory);
        Process process = processBuilder.start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("Script " + scriptName + " failed.");
        }
    }

    protected static void removeFiles() throws IOException {
        FileUtils.deleteDirectory(testDirectory);
    }

    protected static String getFileContent(File file) throws IOException {
        return IOUtils.toString(new FileInputStream(file));
    }

    protected static void logDirectoryStructure() {
        logDirecotoryContent(testDirectory, 0);
    }

    protected static void logFileContents() throws IOException {
        logFileContents(testDirectory);
    }

    private static void logDirecotoryContent(File dir, int offset) {
        Collection<File> files = FileUtils.listFiles(dir, null, true);
        for (File file : files) {
            System.out.println("-" + file.getName());
        }
    }

    private static void logFileContents(File dir) throws IOException {
        String[] extensions = {"aj", "xml", "java", "roo"};
        Collection<File> files = FileUtils.listFiles(dir, extensions, true);
        for (File file : files) {
            System.out.println("*****\t" + file.getName() + "\t*****");
            System.out.println(getFileContent(file));
            System.out.println();
        }
    }

}
