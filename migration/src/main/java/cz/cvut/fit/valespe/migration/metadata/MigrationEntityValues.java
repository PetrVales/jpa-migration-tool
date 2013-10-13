package cz.cvut.fit.valespe.migration.metadata;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;
import org.springframework.roo.model.JavaType;

public class MigrationEntityValues extends AbstractAnnotationValues {

    public static final JavaType MIGRATION_ENTITY = new JavaType(MigrationEntity.class.getName());

    @AutoPopulate private String catalog = "";
    @AutoPopulate private String entityName = "";
    @AutoPopulate private String schema = "";
    @AutoPopulate private String table = "";

    /**
     * Constructor for reading the values of the given annotation
     *
     * @param annotatedType the type from which to read the values (required)
     */
    public MigrationEntityValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, MIGRATION_ENTITY);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getCatalog() {
        return catalog;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

}
