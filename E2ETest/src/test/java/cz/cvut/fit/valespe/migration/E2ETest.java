package cz.cvut.fit.valespe.migration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collection;

public class E2ETest {

    protected File testDirectory;

    protected void runTestScript(String scriptName) throws IOException, InterruptedException {
        createTestDirectory();
        copyScript(scriptName);
        runScript(scriptName);
    }

    private void createTestDirectory() throws IOException {
        testDirectory = File.createTempFile("tmp", Long.toString(System.nanoTime()));
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
            throw new IllegalStateException("Script " + scriptName + " failed.");
        }
    }

    protected void removeFiles() throws IOException {
        FileUtils.deleteDirectory(testDirectory);
    }

    protected String getFileContent(File file) throws IOException {
        return IOUtils.toString(new FileInputStream(file));
    }

    protected void logDirectoryStructure() {
        logDirecotoryContent(testDirectory, 0);
    }

    protected void logFileContents() throws IOException {
        logFileContents(testDirectory);
    }

    private void logDirecotoryContent(File dir, int offset) {
        Collection<File> files = FileUtils.listFiles(dir, null, true);
        for (File file : files) {
//            for (int i = 0; i < offset; i++)
//                System.out.println("|  ");
//            System.out.println("+--" + file.getName() + (file.isDirectory() ? "/" : ""));
//            if (file.isDirectory())
//                logDirecotoryContent(file, offset + 1);
            System.out.println("-" + file.getName());
        }
    }

    private void logFileContents(File dir) throws IOException {
        String[] extensions = {"aj", "xml", "java", "roo"};
        Collection<File> files = FileUtils.listFiles(dir, extensions, true);
        for (File file : files) {
            System.out.println("*****\t" + file.getName() + "\t*****");
            System.out.println(getFileContent(file));
            System.out.println();
        }
    }

}
