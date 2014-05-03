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
import org.springframework.roo.model.JpaJavaType;
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

    public static final JavaType DISCRIMINATOR_VALUE = new JavaType("javax.persistence.DiscriminatorValue");
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

        cidBuilder.addAnnotation(ROO_JAVA_BEAN_BUILDER);
//        cidBuilder.addAnnotation(getEntityAnnotationBuilder(entityName, table));
        cidBuilder.addAnnotation(getEntityAnnotationBuilder(entityName));
        cidBuilder.addAnnotation(getTableAnnotationBuilder(table));

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
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
        final ClassOrInterfaceTypeDetailsBuilder targetBuilder = new ClassOrInterfaceTypeDetailsBuilder(targetTypeDetails);
        final ClassOrInterfaceTypeDetailsBuilder parentBuilder = new ClassOrInterfaceTypeDetailsBuilder(parentTypeDetails);
        targetBuilder.setSuperclass(new ClassOrInterfaceTypeDetailsBuilder(parentTypeDetails));
        targetBuilder.setExtendsTypes(Arrays.asList(parent));
        targetBuilder.addAnnotation(getDiscriminatorValueBuilder(target.getSimpleTypeName().toUpperCase()));

        if (parentBuilder.getDeclaredTypeAnnotation(JpaJavaType.INHERITANCE) == null)
            parentBuilder.addAnnotation(getInheritanceTypeBuilder("JOINED"));
        if (parentBuilder.getDeclaredTypeAnnotation(JpaJavaType.DISCRIMINATOR_COLUMN) == null)
            parentBuilder.addAnnotation(getDiscriminatorColumnBuilder(parent.getSimpleTypeName().toLowerCase() + "_type"));

        removeClass(target);
        removeClass(parent);
        typeManagementService.createOrUpdateTypeOnDisk(targetBuilder.build());
        typeManagementService.createOrUpdateTypeOnDisk(parentBuilder.build());
    }

    @Override
    public void removeParent(JavaType target) {
        final ClassOrInterfaceTypeDetails targetTypeDetails = typeLocationService.getTypeDetails(target);
        final ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(targetTypeDetails);

        builder.setExtendsTypes(Arrays.asList(JavaType.OBJECT));
        builder.removeAnnotation(DISCRIMINATOR_VALUE);

        removeClass(target);
        typeManagementService.createOrUpdateTypeOnDisk(builder.build());
    }

    private AnnotationMetadataBuilder getEntityAnnotationBuilder(String entityName)  {
        final AnnotationMetadataBuilder entityAnnotationBuilder = new AnnotationMetadataBuilder(JpaJavaType.ENTITY);
        entityAnnotationBuilder.addStringAttribute("name", entityName);
        return entityAnnotationBuilder;
    }

    private AnnotationMetadataBuilder getTableAnnotationBuilder(String table)  {
        final AnnotationMetadataBuilder entityAnnotationBuilder = new AnnotationMetadataBuilder(JpaJavaType.TABLE);
        entityAnnotationBuilder.addStringAttribute("name", table);
        return entityAnnotationBuilder;
    }

    private AnnotationMetadataBuilder getInheritanceTypeBuilder(String strategy)  {
        final AnnotationMetadataBuilder entityAnnotationBuilder = new AnnotationMetadataBuilder(JpaJavaType.INHERITANCE);
        entityAnnotationBuilder.addEnumAttribute("strategy", JpaJavaType.INHERITANCE_TYPE, strategy);
        return entityAnnotationBuilder;
    }

    private AnnotationMetadataBuilder getDiscriminatorColumnBuilder(String name)  {
        final AnnotationMetadataBuilder entityAnnotationBuilder = new AnnotationMetadataBuilder(JpaJavaType.DISCRIMINATOR_COLUMN);
        entityAnnotationBuilder.addStringAttribute("name", name);
        return entityAnnotationBuilder;
    }

    private AnnotationMetadataBuilder getDiscriminatorValueBuilder(String value)  {
        final AnnotationMetadataBuilder entityAnnotationBuilder = new AnnotationMetadataBuilder(DISCRIMINATOR_VALUE);
        entityAnnotationBuilder.addStringAttribute("value", value );
        return entityAnnotationBuilder;
    }

}