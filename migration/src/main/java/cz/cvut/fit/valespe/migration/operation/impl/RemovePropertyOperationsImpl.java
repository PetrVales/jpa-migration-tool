package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.operation.RemovePropertyOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class RemovePropertyOperationsImpl implements RemovePropertyOperations {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String CHANGE_SET = "changeSet";
    private static final String DROP_COLUMN = "dropColumn";

    @Reference private TypeManagementService typeManagementService;
    @Reference private FileManager fileManager;
    @Reference private PathResolver pathResolver;

    @Override
    public void deleteFieldFromClass(JavaSymbolName propertyName, ClassOrInterfaceTypeDetails javaTypeDetails) {
        List<? extends FieldMetadata> fields = javaTypeDetails.getDeclaredFields();
        List<FieldMetadataBuilder> fieldsBuilders = new ArrayList<FieldMetadataBuilder>(fields.size());

        for (FieldMetadata fieldMetadata : fields) {
            if (fieldMetadata.getFieldName().compareTo(propertyName) != 0) {
                fieldsBuilders.add(new FieldMetadataBuilder(fieldMetadata));
            }
        }

        ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(javaTypeDetails);
        builder.setDeclaredFields(fieldsBuilders);

        fileManager.delete(pathResolver.getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, javaTypeDetails.getName()));
        typeManagementService.createOrUpdateTypeOnDisk(builder.build());
    }

    @Override
    public void dropColumn(String table, String schema, String catalog, String columnName) {
        Validate.notNull(table, "Table name required");

        final String migrationPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML);
        final InputStream inputStream = fileManager.getInputStream(migrationPath);

        final Document migration = XmlUtils.readXml(inputStream);
        final Element root = migration.getDocumentElement();
        final Element databaseChangeLogElement = XmlUtils.findFirstElement("/databaseChangeLog", root);
        Validate.notNull(databaseChangeLogElement, "No databaseChangeLog element found");

        Element changeSetElement = migration.createElement(CHANGE_SET);
        databaseChangeLogElement.appendChild(changeSetElement);
        Element createTableElement = migration.createElement(DROP_COLUMN);
        setAttribute(createTableElement, "tableName", table);
        setAttribute(createTableElement, "schemaName", schema);
        setAttribute(createTableElement, "catalogName", catalog);
        setAttribute(createTableElement, "columnName", columnName);
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
