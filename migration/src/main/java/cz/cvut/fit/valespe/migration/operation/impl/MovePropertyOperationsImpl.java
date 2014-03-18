package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.operation.MovePropertyOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;

@Component
@Service
public class MovePropertyOperationsImpl implements MovePropertyOperations {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String CHANGE_SET = "changeSet";
    private static final String ADD_COLUMN = "addColumn";
    private static final String SQL = "sql";
    private static final String DROP_COLUMN = "dropColumn";

    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;

    public MovePropertyOperationsImpl() { }

    public MovePropertyOperationsImpl(PathResolver pathResolver, FileManager fileManager) {
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
    }

    @Override
    public void moveColumn(String columnName, String columnType, String fromTable, String fromSchema, String fromCatalog, String toTable, String toSchema, String toCatalog) {
        Validate.notNull(toTable, "Table name required");

        final String migrationPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML);
        final InputStream inputStream = fileManager.getInputStream(migrationPath);

        final Document migration = XmlUtils.readXml(inputStream);
        final Element root = migration.getDocumentElement();
        final Element databaseChangeLogElement = XmlUtils.findFirstElement("/databaseChangeLog", root);
        Validate.notNull(databaseChangeLogElement, "No databaseChangeLog element found");

        Element changeSetElement = migration.createElement(CHANGE_SET);
        databaseChangeLogElement.appendChild(changeSetElement);
        addColumn(columnName, columnType, toTable, toSchema, toCatalog, migration, changeSetElement);
//        moveData(fromTable, toTable, columnName, where, migration, changeSetElement);
        dropColumn(columnName, fromTable, fromSchema, fromCatalog, migration, changeSetElement);

        fileManager.createOrUpdateTextFileIfRequired(migrationPath,
                XmlUtils.nodeToString(migration), false);
    }

    private void addColumn(String columnName, String columnType, String table, String schema, String catalog, Document migration, Element changeSetElement) {
        Element addColumnElement = migration.createElement(ADD_COLUMN);
        Element columnElement = migration.createElement("column");
        setAttribute(columnElement, "name", columnName);
        setAttribute(columnElement, "type", columnType);
        addColumnElement.appendChild(columnElement);
        setAttribute(addColumnElement, "tableName", table);
        setAttribute(addColumnElement, "schemaName", schema);
        setAttribute(addColumnElement, "catalogName", catalog);
        changeSetElement.appendChild(addColumnElement);
    }


    private void moveData(String tableFrom, String tableTo, String columnName, String where, Document migration, Element changeSetElement) {
        Element sqlElement = migration.createElement(SQL);
        sqlElement.setTextContent("UPDATE " + tableTo + " SET " + columnName + "(SELECT " + columnName + " FROM " + tableFrom + " WHERE " + where + ")");
        changeSetElement.appendChild(sqlElement);

    }

    private void dropColumn(String columnName, String table, String schema, String catalog, Document migration, Element changeSetElement) {
        Element createTableElement = migration.createElement(DROP_COLUMN);
        setAttribute(createTableElement, "tableName", table);
        setAttribute(createTableElement, "schemaName", schema);
        setAttribute(createTableElement, "catalogName", catalog);
        setAttribute(createTableElement, "columnName", columnName);
        changeSetElement.appendChild(createTableElement);
    }

    private void setAttribute(Element element, String attribute, String value) {
        if (value != null && !value.isEmpty()) {
            element.setAttribute(attribute, value);
        }
    }

}
