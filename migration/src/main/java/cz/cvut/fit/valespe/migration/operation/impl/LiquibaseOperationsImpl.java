package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Iterator;
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
    private static final String ADD_FOREIGN_KEY = "addForeignKeyConstraint";
    private static final String SQL = "sql";

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
    public void createChangeSet(List<Element> elements, String author, String id) {
        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element changeSetElement = createChangeSetElement(migration, getChangeLogElement(migration), author, id);
        for (Element element : elements)
            if (element != null) {
                migration.adoptNode(element);
                changeSetElement.appendChild(element);
            }

        fileManager.createOrUpdateTextFileIfRequired(migrationPath, XmlUtils.nodeToString(migration), false);
    }

    @Override
    public Element addColumn(String table, String columnName, String columnType) {
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

        return addColumnElement;
    }

    @Override
    public Element addPrimaryKey(List<String> columnNames, String tableName, String constraintName) {
        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        StringBuilder builder = new StringBuilder();
        final Iterator<String> iterator = columnNames.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext())
                builder.append(", ");
        }

        Element addPrimaryKey = migration.createElement(ADD_PRIMARY_KEY);
        setAttribute(addPrimaryKey, "columnNames", builder.toString());
        setAttribute(addPrimaryKey, "constraintName", constraintName);
        setAttribute(addPrimaryKey, "tableName", tableName);

        return addPrimaryKey;
    }

    @Override
    public Element addForeignKey(String table, String columnName, String referencedTable, String referencedColumn, String name) {
        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element addPrimaryKey = migration.createElement(ADD_FOREIGN_KEY);
        setAttribute(addPrimaryKey, "baseTableName", table);
        setAttribute(addPrimaryKey, "baseColumnNames", columnName);
        setAttribute(addPrimaryKey, "referencedTableName", referencedTable);
        setAttribute(addPrimaryKey, "referencedColumnNames", referencedColumn);
        setAttribute(addPrimaryKey, "constraintName", name);

        return addPrimaryKey;
    }

    @Override
    public Element createTable(String table) {
        Validate.notNull(table, "Table name required");

        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element createTableElement = migration.createElement(CREATE_TABLE);
        setAttribute(createTableElement, "tableName", table);

        return createTableElement;
    }

    @Override
    public Element dropTable(String table, boolean cascade) {
        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element dropTableElement = migration.createElement(DROP_TABLE);
        setAttribute(dropTableElement, "tableName", table);
        setAttribute(dropTableElement, "cascadeConstraints", Boolean.toString(cascade));

        return dropTableElement;
    }

    @Override
    public Element dropColumn(String table, String columnName) {
        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element dropColumnElement = migration.createElement(DROP_COLUMN);
        setAttribute(dropColumnElement, "tableName", table);
        setAttribute(dropColumnElement, "columnName", columnName);

        return dropColumnElement;
    }

    @Override
    public Element copyColumnData(String tableFrom, String tableTo, String columnName, String query) {
        return sql("UPDATE " + tableTo + " SET " + columnName +
                    " (SELECT " + columnName + " FROM " + tableFrom + " WHERE " + query + ")");
    }

    @Override
    public Element mergeTables(String target, String tableA, String tableB, List<String> columns, String query) {
        String columnList = StringUtils.join(columns, ", ");
        return sql("INSERT INTO " + target + "(" + columnList + ") " +
                    "(SELECT " + columnList + " FROM " + tableA + " JOIN " + tableB + " ON " + query + ")");
    }

    @Override
    public Element copyData(String origin, String target, List<String> columns, String query) {
        String columnList = StringUtils.join(columns, ", ");
        return sql("INSERT INTO " + target + "(" + columnList + ") " +
                    "(SELECT " + columnList + " FROM " + origin + " WHERE " + query + ")");
    }

    @Override
    public Element sql(String query) {
        final String migrationPath = getMigrationXmlPath();
        final Document migration = getMigrationDocument(migrationPath);

        Element copyColumnDataElement = migration.createElement(SQL);

        copyColumnDataElement.setTextContent(query);

        return copyColumnDataElement;
    }

    @Override
    public Element introduceParent(String targetTable, String parentTable) {
        return addColumn(parentTable, parentTable + "_type", "varchar2");
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

    private Element createChangeSetElement(Document migration, Element databaseChangeLogElement, String author, String id) {
        Element changeSetElement = migration.createElement(CHANGE_SET);
        setAttribute(changeSetElement, "author", author == null ? "" : author);
        setAttribute(changeSetElement, "id", id == null ? "" : id);
        databaseChangeLogElement.appendChild(changeSetElement);
        return changeSetElement;
    }

    private void setAttribute(Element element, String attribute, String value) {
        if (value != null && !value.isEmpty()) {
            element.setAttribute(attribute, value);
        }
    }

}
