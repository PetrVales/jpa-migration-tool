package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.JpaFieldDetails;
import cz.cvut.fit.valespe.migration.operation.NewPropertyOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class NewPropertyOperationsImpl implements NewPropertyOperations {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String CHANGE_SET = "changeSet";
    private static final String ADD_COLUMN = "addColumn";

    @Reference private TypeLocationService typeLocationService;
    @Reference private TypeManagementService typeManagementService;
    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;

    public NewPropertyOperationsImpl() { }

    public NewPropertyOperationsImpl(TypeManagementService typeManagementService, TypeLocationService typeLocationService, PathResolver pathResolver, FileManager fileManager) {
        this.typeManagementService = typeManagementService;
        this.typeLocationService = typeLocationService;
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
    }

    @Override
    public void createColumn(String table, String schema, String catalog, String columnName, String columnType) {
        Validate.notNull(table, "Table name required");

        final String migrationPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML);
        final InputStream inputStream = fileManager.getInputStream(migrationPath);

        final Document migration = XmlUtils.readXml(inputStream);
        final Element root = migration.getDocumentElement();
        final Element databaseChangeLogElement = XmlUtils.findFirstElement("/databaseChangeLog", root);
        Validate.notNull(databaseChangeLogElement, "No databaseChangeLog element found");

        Element changeSetElement = migration.createElement(CHANGE_SET);
        databaseChangeLogElement.appendChild(changeSetElement);
        Element addColumnElement = migration.createElement(ADD_COLUMN);
        Element columnElement = migration.createElement("column");
        setAttribute(columnElement, "name", columnName);
        setAttribute(columnElement, "type", columnType);
        addColumnElement.appendChild(columnElement);
        setAttribute(addColumnElement, "tableName", table);
        setAttribute(addColumnElement, "schemaName", schema);
        setAttribute(addColumnElement, "catalogName", catalog);
        changeSetElement.appendChild(addColumnElement);


        fileManager.createOrUpdateTextFileIfRequired(migrationPath,
                XmlUtils.nodeToString(migration), false);
    }

    private void setAttribute(Element element, String attribute, String value) {
        if (value != null && !value.isEmpty()) {
            element.setAttribute(attribute, value);
        }
    }

    @Override
    public void addFieldToClass(JavaSymbolName propertyName, JavaType propertyType, String columnName, String columnType, ClassOrInterfaceTypeDetails classType) {
        final String physicalTypeIdentifier = classType.getDeclaredByMetadataId();
        final JpaFieldDetails fieldDetails = new JpaFieldDetails(physicalTypeIdentifier, propertyType, propertyName);
        if (columnName != null) {
            fieldDetails.setColumn(columnName);
        }
        if (columnType != null) {
            fieldDetails.setColumnDefinition(columnType);
        }

        insertField(fieldDetails);
    }

    private void insertField(final FieldDetails fieldDetails) {
        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);
        fieldDetails.setAnnotations(annotations);

        fieldDetails.setModifiers(Modifier.PRIVATE);

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(fieldDetails);
        typeManagementService.addField(fieldBuilder.build());
    }

}