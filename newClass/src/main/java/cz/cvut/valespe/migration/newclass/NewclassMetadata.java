package cz.cvut.valespe.migration.newclass;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import static org.springframework.roo.model.JpaJavaType.ENTITY;
import static org.springframework.roo.model.JpaJavaType.TABLE;

public class NewclassMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = NewclassMetadata.class.getName();
    public static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    private final MigrationEntityValues migrationEntityValues;
    
    public NewclassMetadata(String identifier, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata, MigrationEntityValues migrationEntityValues) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '" + identifier + "' does not appear to be a valid");
        this.migrationEntityValues = migrationEntityValues;

        addEntityAnnotation();
        addTableAnnotation();

        itdTypeDetails = builder.build();
    }

    /**
     * Add @Entity annotation
     */
    private void addEntityAnnotation() {
        builder.addAnnotation(getEntityAnnotation());
    }

    private AnnotationMetadata getEntityAnnotation() {
        AnnotationMetadata entityAnnotation = getTypeAnnotation(ENTITY);
        if (entityAnnotation == null) {
            return null;
        }

        if (StringUtils.isNotBlank(migrationEntityValues.getEntityName())) {
            final AnnotationMetadataBuilder entityBuilder = new AnnotationMetadataBuilder(
                    entityAnnotation);
            entityBuilder.addStringAttribute("name",
                    migrationEntityValues.getEntityName());
            entityAnnotation = entityBuilder.build();
        }

        return entityAnnotation;
    }

    /**
     * Add @Table annotation if required
     */
    private void addTableAnnotation() {
        builder.addAnnotation(getTableAnnotation());
    }

    /**
     * Generates the JPA @Table annotation to be applied to the entity
     *
     * @return
     */
    private AnnotationMetadata getTableAnnotation() {
        final AnnotationMetadata tableAnnotation = getTypeAnnotation(TABLE);
        if (tableAnnotation == null) {
            return null;
        }
        final String catalog = migrationEntityValues.getCatalog();
        final String schema = migrationEntityValues.getSchema();
        final String table = migrationEntityValues.getTable();
        if (StringUtils.isNotBlank(table) || StringUtils.isNotBlank(schema)
                || StringUtils.isNotBlank(catalog)) {
            final AnnotationMetadataBuilder tableBuilder = new AnnotationMetadataBuilder(
                    tableAnnotation);
            if (StringUtils.isNotBlank(catalog)) {
                tableBuilder.addStringAttribute("catalog", catalog);
            }
            if (StringUtils.isNotBlank(schema)) {
                tableBuilder.addStringAttribute("schema", schema);
            }
            if (StringUtils.isNotBlank(table)) {
                tableBuilder.addStringAttribute("name", table);
            }
            return tableBuilder.build();
        }
        return null;
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType, LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

}
