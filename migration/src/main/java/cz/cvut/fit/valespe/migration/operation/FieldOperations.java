package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public interface FieldOperations {

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
     * @param oneToOne field is one to one reference
     * @param oneToMany
     * @param manyToOne
     * @param manyToMany
     * @param mappedBy
     */
    void addField(
            JavaSymbolName propertyName,
            JavaType propertyType,
            String columnName,
            String columnType,
            JavaType className,
            boolean id,
            boolean oneToOne, boolean oneToMany, boolean manyToOne, boolean manyToMany, String mappedBy);

    /**
     * Remove specified field from given class
     * @param propertyName name of removed field
     * @param className name of class where field should be removed
     */
    void removeField(
            JavaSymbolName propertyName,
            JavaType className
    );

    /**
     * Make field id, annotated it with @Id
     * @param typeName
     * @param propertyName
     */
    void makeFieldId(
            JavaType typeName,
            JavaSymbolName propertyName
    );
}