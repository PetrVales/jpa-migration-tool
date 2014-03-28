package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;

import static org.springframework.roo.model.RooJavaType.ROO_JAVA_BEAN;

@Component
@Service
public class ClassOperationsImpl implements ClassOperations {

    private Logger log = Logger.getLogger(getClass().getName());

    public static final JavaType MIGRATION_ENTITY = new JavaType(MigrationEntity.class.getName());
    private static final AnnotationMetadataBuilder ROO_JAVA_BEAN_BUILDER = new AnnotationMetadataBuilder(ROO_JAVA_BEAN);

    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;
    @Reference private TypeManagementService typeManagementService;
    @Reference private TypeLocationService typeLocationService;

    public ClassOperationsImpl() { }

    public ClassOperationsImpl(PathResolver pathResolver, FileManager fileManager, TypeManagementService typeManagementService, TypeLocationService typeLocationService) {
        this.pathResolver = pathResolver;
        this.fileManager = fileManager;
        this.typeManagementService = typeManagementService;
        this.typeLocationService = typeLocationService;
    }

    @Override
    public void createClass(JavaType className, String entityName, String table) {
        Validate.notNull(className, "Class name required");
        Validate.isTrue(
                !JdkJavaType.isPartOfJavaLang(className.getSimpleTypeName()),
                "Entity name '%s' must not be part of java.lang",
                className.getSimpleTypeName());

        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = createClassBuilder(className);

        cidBuilder.setAnnotations(createAnnotations(entityName, table));

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }

    @Override
    public void createClass(JavaType className) {
        typeManagementService.createOrUpdateTypeOnDisk(createClassBuilder(className).build());
    }

    private ClassOrInterfaceTypeDetailsBuilder createClassBuilder(JavaType className) {
        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(className, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
        return new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId,
                Modifier.PUBLIC,
                className,
                PhysicalTypeCategory.CLASS
        );
    }

    @Override
    public void removeClass(JavaType target) {
        fileManager.delete(pathResolver.getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, target));
    }

    @Override
    public void introduceParent(JavaType target, JavaType parent) {
        final ClassOrInterfaceTypeDetails targetTypeDetails = typeLocationService.getTypeDetails(target);
        final ClassOrInterfaceTypeDetails parentTypeDetails = typeLocationService.getTypeDetails(parent);
        final ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(targetTypeDetails);
        builder.setSuperclass(new ClassOrInterfaceTypeDetailsBuilder(parentTypeDetails));
        builder.setExtendsTypes(Arrays.asList(parent));

        removeClass(target);
        typeManagementService.createOrUpdateTypeOnDisk(builder.build());
    }

    @Override
    public void removeParent(JavaType target) {
        final ClassOrInterfaceTypeDetails targetTypeDetails = typeLocationService.getTypeDetails(target);
        final ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(targetTypeDetails);
        builder.setExtendsTypes(Arrays.asList(JavaType.OBJECT));

        removeClass(target);
        typeManagementService.createOrUpdateTypeOnDisk(builder.build());
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

}