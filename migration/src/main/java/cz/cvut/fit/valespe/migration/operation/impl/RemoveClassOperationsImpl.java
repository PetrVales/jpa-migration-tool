package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.operation.RemoveClassOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;

@Component
@Service
public class RemoveClassOperationsImpl implements RemoveClassOperations {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String CHANGE_SET = "changeSet";
    private static final String DROP_TABLE = "dropTable";
    
    @Reference private ProjectOperations projectOperations;
    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;
    @Reference private TypeManagementService typeManagementService;

    public RemoveClassOperationsImpl() { }

    public RemoveClassOperationsImpl(ProjectOperations projectOperations, PathResolver pathResolver, FileManager fileManager, TypeManagementService typeManagementService) {
        this.projectOperations = projectOperations;
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
        this.typeManagementService = typeManagementService;
    }

    @Override
    public void removeClass(JavaType target) {
        fileManager.delete(pathResolver.getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, target));
    }

    @Override
    public void dropTable(String table, String schema, String catalog, boolean cascade) {
        Validate.notNull(table, "Table name required");

        final String migrationPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML);
        final InputStream inputStream = fileManager.getInputStream(migrationPath);

        final Document migration = XmlUtils.readXml(inputStream);
        final Element root = migration.getDocumentElement();
        final Element databaseChangeLogElement = XmlUtils.findFirstElement("/databaseChangeLog", root);
        Validate.notNull(databaseChangeLogElement, "No databaseChangeLog element found");

        Element changeSetElement = migration.createElement(CHANGE_SET);
        databaseChangeLogElement.appendChild(changeSetElement);
        Element createTableElement = migration.createElement(DROP_TABLE);
        setAttribute(createTableElement, "tableName", table);
        setAttribute(createTableElement, "schemaName", schema);
        setAttribute(createTableElement, "catalogName", catalog);
        setAttribute(createTableElement, "cascadeConstraints", Boolean.toString(cascade));
        changeSetElement.appendChild(createTableElement);

        fileManager.createOrUpdateTextFileIfRequired(migrationPath,
                XmlUtils.nodeToString(migration), false);
    }

    private void setAttribute(Element element, String attribute, String value) {
        if (value != null && !value.isEmpty()) {
            element.setAttribute(attribute, value);
        }
    }

}