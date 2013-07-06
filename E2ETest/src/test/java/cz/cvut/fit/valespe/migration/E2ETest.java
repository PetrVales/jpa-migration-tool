package cz.cvut.fit.valespe.migration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class E2ETest {

    protected File testDirectory;

    protected void runTestScript(String scriptName) throws IOException, InterruptedException {
        createTestDirectory();
        copyScript(scriptName);
        runScript(scriptName);
    }

    private void createTestDirectory() throws IOException {
        testDirectory = File.createTempFile("temp", Long.toString(System.nanoTime()));
        if(!(testDirectory.delete())) {
            throw new IOException("Could not delete temp file: " + testDirectory.getAbsolutePath());
        }
        if(!(testDirectory.mkdir())) {
            throw new IOException("Could not create temp directory: " + testDirectory.getAbsolutePath());
        }
    }

    private void copyScript(String scriptName) throws IOException {
        InputStream script = E2ETest.class.getResourceAsStream(scriptName);
        File scriptFile = new File(testDirectory, scriptName);
        if (scriptFile.createNewFile()) {
            FileOutputStream stream = new FileOutputStream(scriptFile);
            IOUtils.copy(script, stream);
            stream.close();
        }
        script.close();
    }

    private void runScript(String scriptName) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",  "roo < " + scriptName, "-c");
        processBuilder.directory(testDirectory);
        Process process = processBuilder.start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("Script failed.");
        }
    }

    protected void removeFiles() throws IOException {
        FileUtils.deleteDirectory(testDirectory);
    }

    protected String getFileContent(File file) throws IOException {
        return IOUtils.toString(new FileInputStream(file));
    }

}
