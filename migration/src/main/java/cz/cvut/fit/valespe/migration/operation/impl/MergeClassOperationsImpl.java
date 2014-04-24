package cz.cvut.fit.valespe.migration.operation.impl;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.MergeClassOperations;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.roo.model.RooJavaType.ROO_JAVA_BEAN;

@Component
@Service
public class MergeClassOperationsImpl implements MergeClassOperations {

    public static final JavaType MIGRATION_ENTITY = new JavaType(MigrationEntity.class.getName());
    private static final AnnotationMetadataBuilder ROO_JAVA_BEAN_BUILDER = new AnnotationMetadataBuilder(ROO_JAVA_BEAN);

    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private TypeManagementService typeManagementService;
    @Reference private FileManager fileManager;
    @Reference private PathResolver pathResolver;

    @Override
    public void mergeClasses(JavaType target, JavaType classA, JavaType classB, String table) {
        final ClassOrInterfaceTypeDetails classATypeDetails = typeLocationService.getTypeDetails(classA);
        final ClassOrInterfaceTypeDetails classBTypeDetails = typeLocationService.getTypeDetails(classB);


        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(target, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
        final ClassOrInterfaceTypeDetailsBuilder builder =
                new ClassOrInterfaceTypeDetailsBuilder(
                        declaredByMetadataId,
                        Modifier.PUBLIC,
                        target,
                        PhysicalTypeCategory.CLASS
                );
        builder.setDeclaredFields(getDeclaredFields(classATypeDetails, classBTypeDetails));
        builder.setAnnotations(createAnnotations(target.getSimpleTypeName(), table));

        typeManagementService.createOrUpdateTypeOnDisk(builder.build());
    }

    private Collection<? extends FieldMetadataBuilder> getDeclaredFields(ClassOrInterfaceTypeDetails classATypeDetails, ClassOrInterfaceTypeDetails classBTypeDetails) {
        List<? extends FieldMetadata> fieldsA = classATypeDetails.getDeclaredFields();
        List<? extends FieldMetadata> fieldsB = classBTypeDetails.getDeclaredFields();
        List<FieldMetadataBuilder> fieldsBuilders = new ArrayList<FieldMetadataBuilder>(fieldsA.size() + fieldsB.size());

        for (FieldMetadata fieldMetadata : fieldsA) {
            fieldsBuilders.add(new FieldMetadataBuilder(fieldMetadata));
        }
        for (FieldMetadata fieldMetadata : fieldsB) {
            fieldsBuilders.add(new FieldMetadataBuilder(fieldMetadata));
        }

        return fieldsBuilders;
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
    public void createTable(ClassOrInterfaceTypeDetails targetTypeDetails) {
    }

}
