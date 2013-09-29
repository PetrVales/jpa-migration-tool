package cz.cvut.valespe.migration.mergeclass;

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
public class MergeclassOperationsImpl implements MergeclassOperations {

    public static final JavaType MIGRATION_ENTITY = new JavaType("cz.cvut.valespe.migration.newclass.MigrationEntity");
    private static final AnnotationMetadataBuilder ROO_JAVA_BEAN_BUILDER = new AnnotationMetadataBuilder(
            ROO_JAVA_BEAN);

    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private TypeManagementService typeManagementService;
    @Reference private FileManager fileManager;
    @Reference private PathResolver pathResolver;

    @Override
    public void mergeClasses(JavaType target, JavaType classA, JavaType classB, String table, String schema, String catalog, String tablespace) {
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
        builder.setDeclaredMethods(getDeclaredMethods(classATypeDetails, classBTypeDetails));
        builder.setDeclaredInnerTypes(getDeclaredInnerTypes(classATypeDetails, classBTypeDetails));
        builder.setEnumConstants(getEnums(classATypeDetails, classBTypeDetails));
        builder.setAnnotations(createAnnotations(target.getSimpleTypeName(), table, schema, catalog));

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

    private Collection<? extends MethodMetadataBuilder> getDeclaredMethods(ClassOrInterfaceTypeDetails classATypeDetails, ClassOrInterfaceTypeDetails classBTypeDetails) {
        List<? extends MethodMetadata> fieldsA = classATypeDetails.getDeclaredMethods();
        List<? extends MethodMetadata> fieldsB = classBTypeDetails.getDeclaredMethods();
        List<MethodMetadataBuilder> methodBuilders = new ArrayList<MethodMetadataBuilder>(fieldsA.size() + fieldsB.size());

        for (MethodMetadata methodMetadata : fieldsA) {
            methodBuilders.add(new MethodMetadataBuilder(methodMetadata));
        }
        for (MethodMetadata methodMetadata : fieldsB) {
            methodBuilders.add(new MethodMetadataBuilder(methodMetadata));
        }

        return methodBuilders;
    }

    private Collection<? extends ClassOrInterfaceTypeDetailsBuilder> getDeclaredInnerTypes(ClassOrInterfaceTypeDetails classATypeDetails, ClassOrInterfaceTypeDetails classBTypeDetails) {
        List<? extends ClassOrInterfaceTypeDetails> innersA = classATypeDetails.getDeclaredInnerTypes();
        List<? extends ClassOrInterfaceTypeDetails> innersB = classBTypeDetails.getDeclaredInnerTypes();
        List<ClassOrInterfaceTypeDetailsBuilder> innerBuilders = new ArrayList<ClassOrInterfaceTypeDetailsBuilder>(innersA.size() + innersB.size());

        for (ClassOrInterfaceTypeDetails inner : innersA) {
            innerBuilders.add(new ClassOrInterfaceTypeDetailsBuilder(inner));
        }
        for (ClassOrInterfaceTypeDetails inner : innersB) {
            innerBuilders.add(new ClassOrInterfaceTypeDetailsBuilder(inner));
        }

        return innerBuilders;
    }

    private Collection<? extends JavaSymbolName> getEnums(ClassOrInterfaceTypeDetails classATypeDetails, ClassOrInterfaceTypeDetails classBTypeDetails) {
        List<? extends JavaSymbolName> enumsA = classATypeDetails.getEnumConstants();
        List<? extends JavaSymbolName> enumsB = classBTypeDetails.getEnumConstants();
        List<JavaSymbolName> enums = new ArrayList<JavaSymbolName>(enumsA.size() + enumsB.size());
        enums.addAll(enumsA);
        enums.addAll(enumsB);
        return enums;
    }

    private Collection<? extends JavaType> getInterfaces(ClassOrInterfaceTypeDetails classATypeDetails, ClassOrInterfaceTypeDetails classBTypeDetails) {
        List<? extends JavaType> interfacesA = classATypeDetails.getImplementsTypes();
        List<? extends JavaType> interfacesB = classBTypeDetails.getImplementsTypes();
        List<JavaType> interfaces = new ArrayList<JavaType>(interfacesA.size() + interfacesB.size());
        interfaces.addAll(interfacesA);
        interfaces.addAll(interfacesB);
        return interfaces;
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
    public void createTable(ClassOrInterfaceTypeDetails targetTypeDetails) {
    }

}
