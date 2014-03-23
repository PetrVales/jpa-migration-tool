package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.SplitClassOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.roo.model.RooJavaType.ROO_JAVA_BEAN;

@Component
@Service
public class SplitClassOperationsImpl implements SplitClassOperations {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String CHANGE_SET = "changeSet";
    private static final String CREATE_TABLE = "createTable";
    private static final String ADD_COLUMN = "addColumn";
    
    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private TypeManagementService typeManagementService;
    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;

    public static final JavaType MIGRATION_ENTITY = new JavaType(MigrationEntity.class.getName());
    private static final AnnotationMetadataBuilder ROO_JAVA_BEAN_BUILDER = new AnnotationMetadataBuilder(ROO_JAVA_BEAN);

    @Override
    public void createClass(JavaType original, JavaType target, List<FieldMetadata> propertiesA, String table) {
        final ClassOrInterfaceTypeDetails originalTypeDetails = typeLocationService.getTypeDetails(original);

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(target, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
        final ClassOrInterfaceTypeDetailsBuilder builder =
                new ClassOrInterfaceTypeDetailsBuilder(
                        declaredByMetadataId,
                        Modifier.PUBLIC,
                        target,
                        PhysicalTypeCategory.CLASS
                );
        builder.setDeclaredFields(getDeclaredFields(propertiesA));
        builder.setDeclaredMethods(getDeclaredMethods(originalTypeDetails.getDeclaredMethods()));
        builder.setDeclaredInnerTypes(getDeclaredInnerTypes(originalTypeDetails.getDeclaredInnerTypes()));
        builder.setEnumConstants(originalTypeDetails.getEnumConstants());
        builder.setAnnotations(createAnnotations(target.getSimpleTypeName(), table));

        typeManagementService.createOrUpdateTypeOnDisk(builder.build());
    }

    private Collection<? extends FieldMetadataBuilder> getDeclaredFields(List<FieldMetadata> fields) {
        List<FieldMetadataBuilder> fieldsBuilders = new ArrayList<FieldMetadataBuilder>(fields.size());

        for (FieldMetadata fieldMetadata : fields) {
            fieldsBuilders.add(new FieldMetadataBuilder(fieldMetadata));
        }

        return fieldsBuilders;
    }

    private Iterable<? extends MethodMetadataBuilder> getDeclaredMethods(List<? extends MethodMetadata> declaredMethods) {
        List<MethodMetadataBuilder> methodBuilders = new ArrayList<MethodMetadataBuilder>(declaredMethods.size());

        for (MethodMetadata methodMetadata : declaredMethods) {
            methodBuilders.add(new MethodMetadataBuilder(methodMetadata));
        }

        return methodBuilders;
    }

    private Collection<? extends ClassOrInterfaceTypeDetailsBuilder> getDeclaredInnerTypes(List<ClassOrInterfaceTypeDetails> declaredInnerTypes) {
        List<ClassOrInterfaceTypeDetailsBuilder> innerBuilders = new ArrayList<ClassOrInterfaceTypeDetailsBuilder>(declaredInnerTypes.size());

        for (ClassOrInterfaceTypeDetails inner : declaredInnerTypes) {
            innerBuilders.add(new ClassOrInterfaceTypeDetailsBuilder(inner));
        }

        return innerBuilders;
    }


    private List<AnnotationMetadataBuilder> createAnnotations(String entityName, String table) {
        final List<AnnotationMetadataBuilder> annotationBuilder = new ArrayList<AnnotationMetadataBuilder>();
        annotationBuilder.add(ROO_JAVA_BEAN_BUILDER);
        annotationBuilder.add(getEntityAnnotationBuilder(entityName, table));
        return annotationBuilder;
    }

    private AnnotationMetadataBuilder getEntityAnnotationBuilder(String entityName, String table)  {
        final AnnotationMetadataBuilder entityAnnotationBuilder = new AnnotationMetadataBuilder(MIGRATION_ENTITY);

        if (entityName != null) {
            entityAnnotationBuilder.addStringAttribute("entityName", entityName);
        }
        if (table != null) {
            entityAnnotationBuilder.addStringAttribute("table", table);
        }

        return entityAnnotationBuilder;
    }

    @Override
    public void removeClass(JavaType target) {
        fileManager.delete(pathResolver.getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, target));
    }

    @Override
    public void createTable(List<FieldMetadata> properties, String table) {
        createTable(table);
        for (FieldMetadata fieldMetadata : properties) {
//            addColumn(table, schema, catalog, );
        }
    }

    public void createTable(String table) {
        Validate.notNull(table, "Table name required");

        final String migrationPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RESOURCES, MIGRATION_XML);
        final InputStream inputStream = fileManager.getInputStream(migrationPath);

        final Document migration = XmlUtils.readXml(inputStream);
        final Element root = migration.getDocumentElement();
        final Element databaseChangeLogElement = XmlUtils.findFirstElement("/databaseChangeLog", root);
        Validate.notNull(databaseChangeLogElement, "No databaseChangeLog element found");

        Element changeSetElement = migration.createElement(CHANGE_SET);
        databaseChangeLogElement.appendChild(changeSetElement);
        Element createTableElement = migration.createElement(CREATE_TABLE);
        setAttribute(createTableElement, "tableName", table);
        changeSetElement.appendChild(createTableElement);

        fileManager.createOrUpdateTextFileIfRequired(migrationPath,
                XmlUtils.nodeToString(migration), false);
    }

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

}
