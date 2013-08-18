package cz.cvut.valespe.migration.newclass;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
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

import static org.springframework.roo.model.RooJavaType.ROO_JAVA_BEAN;

@Component
@Service
public class NewclassOperationsImpl implements NewclassOperations {

    private static final String MIGRATION_XML = "migration.xml";
    private static final String CHANGE_SET = "changeSet";
    private static final String CREATE_TABLE = "createTable";
    public static final JavaType MIGRATION_ENTITY = new JavaType(MigrationEntity.class.getName());
    private static final AnnotationMetadataBuilder MIGRATION_ENTITY_BUILDER =
            new AnnotationMetadataBuilder(MIGRATION_ENTITY);
    private static final AnnotationMetadataBuilder ROO_JAVA_BEAN_BUILDER = new AnnotationMetadataBuilder(
            ROO_JAVA_BEAN);

    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;
    @Reference private TypeManagementService typeManagementService;

    public NewclassOperationsImpl() { }

    public NewclassOperationsImpl(PathResolver pathResolver, FileManager fileManager, TypeManagementService typeManagementService) {
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
        this.typeManagementService = typeManagementService;
    }

    public boolean isCommandAvailable() {
        return true;
    }

    @Override
    public void createEntity(JavaType className, String entityName, String table, String schema, String catalog) {
        Validate.notNull(className, "Class name required");
        Validate.isTrue(
                !JdkJavaType.isPartOfJavaLang(className.getSimpleTypeName()),
                "Entity name '%s' must not be part of java.lang",
                className.getSimpleTypeName());

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(className, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
                new ClassOrInterfaceTypeDetailsBuilder(
                        declaredByMetadataId,
                        Modifier.PUBLIC,
                        className,
                        PhysicalTypeCategory.CLASS
                );

        cidBuilder.setAnnotations(createAnnotations(entityName, table, schema, catalog));

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }

    private List<AnnotationMetadataBuilder> createAnnotations(String entityName, String table, String schema, String catalog) {
        final List<AnnotationMetadataBuilder> annotationBuilder = new ArrayList<AnnotationMetadataBuilder>();
        annotationBuilder.add(ROO_JAVA_BEAN_BUILDER);
        annotationBuilder.add(getEntityAnnotationBuilder(entityName, table, schema, catalog));
        return annotationBuilder;
    }

    private AnnotationMetadataBuilder getEntityAnnotationBuilder(String entityName, String table, String schema, String catalog)  {
        final AnnotationMetadataBuilder entityAnnotationBuilder = new AnnotationMetadataBuilder(MIGRATION_ENTITY);

        if (entityName != null) {
            entityAnnotationBuilder.addStringAttribute("entityName", entityName);
        }
        if (table != null) {
            entityAnnotationBuilder.addStringAttribute("table", table);
        }
        if (schema != null) {
            entityAnnotationBuilder.addStringAttribute("schema", schema);
        }
        if (catalog != null) {
            entityAnnotationBuilder.addStringAttribute("catalog", catalog);
        }

        return entityAnnotationBuilder;
    }

    @Override
    public void createTable(String table, String schema, String catalog, String tablespace) {
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
        setAttribute(createTableElement, "schemaName", schema);
        setAttribute(createTableElement, "catalogName", catalog);
        setAttribute(createTableElement, "tablespace", tablespace);
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