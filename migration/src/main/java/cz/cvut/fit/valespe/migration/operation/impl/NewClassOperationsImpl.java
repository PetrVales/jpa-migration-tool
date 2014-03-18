package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.NewClassOperations;
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
public class NewClassOperationsImpl implements NewClassOperations {

    public static final JavaType MIGRATION_ENTITY = new JavaType(MigrationEntity.class.getName());
    private static final AnnotationMetadataBuilder ROO_JAVA_BEAN_BUILDER = new AnnotationMetadataBuilder(ROO_JAVA_BEAN);

    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;
    @Reference private TypeManagementService typeManagementService;

    public NewClassOperationsImpl() { }

    public NewClassOperationsImpl(PathResolver pathResolver, FileManager fileManager, TypeManagementService typeManagementService) {
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
        this.typeManagementService = typeManagementService;
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

}