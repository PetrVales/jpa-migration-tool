package cz.cvut.fit.valespe.migration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Trigger annotation for this newProperty-on.
 
 * @since 1.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface MigrationEntity {

    /**
     * Specifies the database catalog name that should be used for the entity.
     *
     * @return the name of the catalog to use (defaults to "")
     */
    String catalog() default "";

    /**
     * Specifies the name used to refer to the entity in queries.
     * <p>
     * The name must not be a reserved literal in JPQL.
     *
     * @return the name given to the entity (defaults to "")
     */
    String entityName() default "";

    /**
     * Specifies the database schema name that should be used for the entity.
     *
     * @return the name of the schema to use (defaults to "")
     */
    String schema() default "";

    /**
     * Specifies the table name that should be used for the entity.
     *
     * @return the name of the table to use (defaults to "")
     */
    String table() default "";

}
