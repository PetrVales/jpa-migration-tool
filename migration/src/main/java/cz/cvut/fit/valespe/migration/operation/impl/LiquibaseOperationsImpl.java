package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
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
import java.util.Arrays;
import java.util.List;

@Component
@Service
public class LiquibaseOperationsImpl implements LiquibaseOperations {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String MIGRATION_TEMPLATE_XML = "migration-template.xml";
    private static final String DATABASE_CHANGE_LOG_ELEMENT = "/databaseChangeLog";
    private static final String CHANGE_SET = "changeSet";
    private static final String ADD_COLUMN = "addColumn";
    private static final String CREATE_TABLE = "createTable";
    private static final String DROP_TABLE = "dropTable";
    private static final String DROP_COLUMN = "dropColumn";
    private static final String ADD_PRIMARY_KEY = "addPrimaryKey";

    @Reference
    private PathResolver pathResolver;
    @Reference
    private FileManager fileManager;

    public LiquibaseOperationsImpl() { }

    public LiquibaseOperationsImpl(PathResolver pathResolver, FileManager fileManager) {
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
    }

    @Override
    public boolean doesMigrationFileExist() {
        return fileManager.exists(getMigrationXmlPath());
    }

    @Override
    public void createMigrationFile() {
        final Document migration = getMigrationTemplateDocument();
        Validate.notNull(getDatabaseChangeLogElement(migration), "No databaseChangeLog element has been found");
        createMigrationFile(migration);
    }

    @Override
    public String createNewChangeSet(String user) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createColumn(String table, String schema, String catalog, String columnName, String columnType, Boolean id) {
        Validate.notNull(table, "Table name required");
        Validate.notNull(columnName, "Column name required");
        Validate.notNull(columnType, "Column type required");

        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element addColumnElement = migration.createElement(ADD_COLUMN);
        Element columnElement = migration.createElement("column");
        setAttribute(columnElement, "name", columnName);
        setAttribute(columnElement, "type", columnType);
        addColumnElement.appendChild(columnElement);
        setAttribute(addColumnElement, "tableName", table);
        setAttribute(addColumnElement, "schemaName", schema);
        setAttribute(addColumnElement, "catalogName", catalog);
        Element addPrimaryKey = null;
        if (id) {
            addPrimaryKey = migration.createElement(ADD_PRIMARY_KEY);
            setAttribute(addPrimaryKey, "columnNames", columnName);
            setAttribute(addPrimaryKey, "constraintName", "pk_" + columnName);
            setAttribute(addPrimaryKey, "tableName", table);

        }

        addElementToNewChangeSet(migration, Arrays.asList(addColumnElement, addPrimaryKey));

        fileManager.createOrUpdateTextFileIfRequired(migrationPath,
                XmlUtils.nodeToString(migration), false);
    }

    @Override
    public void createTable(String table) {
        Validate.notNull(table, "Table name required");

        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element createTableElement = migration.createElement(CREATE_TABLE);
        setAttribute(createTableElement, "tableName", table);

        addElementToNewChangeSet(migration, Arrays.asList(createTableElement));

        fileManager.createOrUpdateTextFileIfRequired(migrationPath,
                XmlUtils.nodeToString(migration), false);
    }

    @Override
    public void dropTable(String table, String schema, String catalog, boolean cascade) {
        Validate.notNull(table, "Table name required");

        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element createTableElement = migration.createElement(DROP_TABLE);
        setAttribute(createTableElement, "tableName", table);
        setAttribute(createTableElement, "schemaName", schema);
        setAttribute(createTableElement, "catalogName", catalog);
        setAttribute(createTableElement, "cascadeConstraints", Boolean.toString(cascade));
        addElementToNewChangeSet(migration, Arrays.asList(createTableElement));

        fileManager.createOrUpdateTextFileIfRequired(migrationPath,
                XmlUtils.nodeToString(migration), false);
    }

    @Override
    public void dropColumn(String table, String schema, String catalog, String columnName) {
        Validate.notNull(table, "Table name required");
        Validate.notNull(columnName, "Column name required");

        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element createTableElement = migration.createElement(DROP_COLUMN);
        setAttribute(createTableElement, "tableName", table);
        setAttribute(createTableElement, "schemaName", schema);
        setAttribute(createTableElement, "catalogName", catalog);
        setAttribute(createTableElement, "columnName", columnName);
        addElementToNewChangeSet(migration, Arrays.asList(createTableElement));

        fileManager.createOrUpdateTextFileIfRequired(migrationPath,
                XmlUtils.nodeToString(migration), false);
    }

    private String getMigrationXmlPath() {
        return pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML);
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

    private void addElementToNewChangeSet(Document migration, List<Element> elements) {
        final Element databaseChangeLogElement = getChangeLogElement(migration);

        Element changeSetElement = createChangeSetElement(migration, databaseChangeLogElement);
        for (Element element : elements)
            if (element != null)
                changeSetElement.appendChild(element);
    }

    private Document getMigrationDocument(String migrationPath) {
        final InputStream inputStream = fileManager.getInputStream(migrationPath);
        return XmlUtils.readXml(inputStream);
    }

    private Element getChangeLogElement(Document migration) {
        final Element root = migration.getDocumentElement();
        final Element databaseChangeLogElement = XmlUtils.findFirstElement(DATABASE_CHANGE_LOG_ELEMENT, root);
        Validate.notNull(databaseChangeLogElement, "No databaseChangeLog element found");
        return databaseChangeLogElement;
    }

    private Element createChangeSetElement(Document migration, Element databaseChangeLogElement) {
        Element changeSetElement = migration.createElement(CHANGE_SET);
        databaseChangeLogElement.appendChild(changeSetElement);
        return changeSetElement;
    }

    private void setAttribute(Element element, String attribute, String value) {
        if (value != null && !value.isEmpty()) {
            element.setAttribute(attribute, value);
        }
    }

}
