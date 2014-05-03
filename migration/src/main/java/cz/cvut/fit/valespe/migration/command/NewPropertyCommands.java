package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.FieldOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

@Component
@Service
public class NewPropertyCommands implements CommandMarker {
    @Reference private FieldOperations fieldOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private ClassCommons classCommons;
    @Reference private FieldCommons fieldCommons;

    private final Logger log =
            Logger.getLogger(getClass().getName());

    public NewPropertyCommands() {}

    public NewPropertyCommands(
            FieldOperations fieldOperations,
            ProjectOperations projectOperations,
            LiquibaseOperations liquibaseOperations,
            ClassCommons classCommons,
            FieldCommons fieldCommons
    ) {
        this.fieldOperations = fieldOperations;
        this.projectOperations = projectOperations;
        this.liquibaseOperations = liquibaseOperations;
        this.classCommons = classCommons;
        this.fieldCommons = fieldCommons;
    }

    @CliAvailabilityIndicator({ "migrate new property" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate new property", help = "Some helpful description")
    public void newProperty(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = {"", "property"}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "propertyType", mandatory = true, help = "Type of new property") final JavaType propertyType,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "columnType", mandatory = true, help = "The JPA @Column name") final String columnType,
            @CliOption(key = "pk", mandatory = false, help = "@Id", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean pk,
            @CliOption(key = "oneToOne", mandatory = false, help = "@OneToOne", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean oneToOne,
            @CliOption(key = "oneToMany", mandatory = false, help = "@OneToMany", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean oneToMany,
            @CliOption(key = "manyToOne", mandatory = false, help = "@ManyToOne", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean manyToOne,
            @CliOption(key = "manyToMany", mandatory = false, help = "@ManyToMany", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean manyToMany,
            @CliOption(key = "mappedBy", mandatory = false, help = "Reference mappedBy values") final String mappedBy,
            @CliOption(key = "refColumn", mandatory = false, help = "Referenced column") final String refColumn,
            @CliOption(key = "skipDrop", mandatory = false, help = "skip dropping any data", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean skipDrop,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        validate(typeName, propertyName);

        fieldOperations.addField(propertyName, propertyType, columnName, columnType, typeName, pk, oneToOne, oneToMany, manyToOne, manyToMany, mappedBy);
        final String table = classCommons.tableName(typeName);

        List<Element> elements = new ArrayList<Element>();
        if (!oneToMany && !(oneToOne && mappedBy != null))
            elements.add(liquibaseOperations.addColumn(table, columnName, columnType));
        if (pk)
            elements.add(liquibaseOperations.addPrimaryKey(Arrays.asList(columnName), table, table + "_pk"));
        if ((oneToOne && mappedBy == null) || manyToOne) {
            final String referencedTable = classCommons.tableName(propertyType);
            Validate.notBlank(refColumn, "Referenced column is not specified");
            elements.add(liquibaseOperations.addForeignKey(table, columnName, referencedTable, refColumn, table + "_" + referencedTable + "_fk"));
        }

        liquibaseOperations.createChangeSet(elements, author, id);
    }

    @CliCommand(value = "migrate add id", help = "Some helpful description")
    public void addId(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        final JavaSymbolName propertyName = new JavaSymbolName("id");
        validate(typeName, propertyName);

        final String table = classCommons.tableName(typeName);

        fieldOperations.addField(propertyName, new JavaType("java.lang.Long"), "id", "bigint", typeName, true, false, false, false, false, null);
        List<Element> elements = new ArrayList<Element>();
        elements.add(liquibaseOperations.addColumn(table, "id", "bigint"));
        elements.add(liquibaseOperations.addPrimaryKey(Arrays.asList("id"), table, table + "_pk"));
        liquibaseOperations.createChangeSet(elements, author, id);
    }

    @CliCommand(value = "migrate add string", help = "Some helpful description")
    public void addString(
            @CliOption(key = {"field", ""}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        validate(typeName, propertyName);

        fieldOperations.addField(propertyName, new JavaType("java.lang.String"), columnName, "varchar2(255)", typeName);
        addColumn(classCommons.tableName(typeName), columnName, "varchar2(255)", author, id);
    }

    @CliCommand(value = "migrate add integer", help = "Some helpful description")
    public void addInteger(
            @CliOption(key = {"field", ""}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        validate(typeName, propertyName);

        fieldOperations.addField(propertyName, new JavaType("java.lang.Integer"), columnName, "integer", typeName);
        addColumn(classCommons.tableName(typeName), columnName, "integer", author, id);
    }

    @CliCommand(value = "migrate add boolean", help = "Some helpful description")
    public void addBoolean(
            @CliOption(key = {"field", ""}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        validate(typeName, propertyName);

        fieldOperations.addField(propertyName, new JavaType("java.lang.Boolean"), columnName, "boolean", typeName);
        addColumn(classCommons.tableName(typeName), columnName, "boolean", author, id);
    }

    private void addColumn(String tableName, String columnName, String columnType, String author, String id) {
        liquibaseOperations.createChangeSet(
                Arrays.asList(liquibaseOperations.addColumn(tableName, columnName, columnType)),
                author, id);
    }

    private void validate(JavaType typeName, JavaSymbolName propertyName) {
        Validate.isTrue(classCommons.exist(typeName), "Specified class, '%s', doesn't exist", typeName);
        if (classCommons.hasField(typeName, propertyName))
            Validate.isTrue(!classCommons.hasField(typeName, propertyName), "Specified class, '%s' has property %s already", typeName, propertyName);
    }

}
