package cz.cvut.valespe.migration.setup;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;

@Component
@Service
public class MigrationSetupOperationsImpl implements MigrationSetupOperations {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String MIGRATION_TEMPLATE_XML = "migration-template.xml";
    private static final String DATABASE_CHANGE_LOG_ELEMENT = "/databaseChangeLog";

    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;

    public MigrationSetupOperationsImpl() {
    }

    public MigrationSetupOperationsImpl(PathResolver pathResolver, FileManager fileManager) {
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
    }

    @Override
    public boolean doesMigrationFileExist() {
        return fileManager.exists(getMigrationXmlPath());
    }

    private String getMigrationXmlPath() {
        return pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML);
    }

    @Override
    public void createMigrationFile() {
        final Document migration = getMigrationTemplateDocument();
        Validate.notNull(getDatabaseChangeLogElement(migration), "No databaseChangeLog element has been found");
        createMigrationFile(migration);
    }

    private Document getMigrationTemplateDocument() {
        final InputStream inputStream = FileUtils.getInputStream(getClass(), MIGRATION_TEMPLATE_XML);
        return XmlUtils.readXml(inputStream);
    }

    private Element getDatabaseChangeLogElement(Document migration) {
        final Element root = migration.getDocumentElement();
        return XmlUtils.findFirstElement(DATABASE_CHANGE_LOG_ELEMENT, root);
    }

    private void createMigrationFile(Document migration) {
        fileManager.createOrUpdateTextFileIfRequired(
                getMigrationXmlPath(),
                XmlUtils.nodeToString(migration),
                false);
    }

}
