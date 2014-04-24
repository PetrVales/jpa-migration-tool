package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public interface PropertyOperations {

    /**
     * Add filed with specified name and type to given class.
     * Field is annotated with @Column annotation. Annotation specifies column name and type.
     * @param propertyName name of new field
     * @param propertyType type of new field
     * @param columnName name of column
     * @param columnType type of column
     * @param className name of class where field is added
     */
    void addField(
            JavaSymbolName propertyName,
            JavaType propertyType,
            String columnName,
            String columnType,
            JavaType className
    );

    /**
     * Add filed with specified name and type to given class.
     * Field is annotated with @Column annotation. Annotation specifies column name and type.
     * If id is true field is marked as @Id
     * @param propertyName name of new field
     * @param propertyType type of new field
     * @param columnName name of column
     * @param columnType type of column
     * @param className name of class where field is added
     * @param id field is id of given class
     */
    void addField(
            JavaSymbolName propertyName,
            JavaType propertyType,
            String columnName,
            String columnType,
            JavaType className,
            boolean id
    );

    /**
     * Remove specified field from given class
     * @param propertyName name of removed field
     * @param className name of class where field should be removed
     */
    void removeField(
            JavaSymbolName propertyName,
            JavaType className
    );

}