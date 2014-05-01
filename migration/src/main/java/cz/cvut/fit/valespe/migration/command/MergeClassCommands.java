package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.FieldOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.w3c.dom.Element;

import java.util.*;

@Component
@Service
public class MergeClassCommands implements CommandMarker {

    @Reference private ProjectOperations projectOperations;
    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private ClassOperations classOperations;
    @Reference private FieldOperations fieldOperations;
    @Reference private ClassCommons classCommons;
    @Reference private FieldCommons fieldCommons;

    public MergeClassCommands() { }

    public MergeClassCommands(
            ClassOperations classOperations,
            ProjectOperations projectOperations,
            LiquibaseOperations liquibaseOperations,
            FieldOperations fieldOperations,
            ClassCommons classCommons,
            FieldCommons fieldCommons
    ) {
        this.classOperations = classOperations;
        this.projectOperations = projectOperations;
        this.liquibaseOperations = liquibaseOperations;
        this.fieldOperations = fieldOperations;
        this.classCommons = classCommons;
        this.fieldCommons = fieldCommons;
    }

    @CliAvailabilityIndicator({ "migrate merge class" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate merge class", help = "Merge two classes into one and generate liquibase change set.")
    public void mergeClass(
            @CliOption(key = {"", "class"}, mandatory = true, help = "The java type to apply this annotation to") JavaType target,
            @CliOption(key = "classA", mandatory = true, help = "The java type to apply this annotation to") JavaType classA,
            @CliOption(key = "classB", mandatory = true, help = "The java type to apply this annotation to") JavaType classB,
            @CliOption(key = "table", mandatory = true, help = "The JPA table name to use for this entity") final String table,
            @CliOption(key = "entity", mandatory = false, help = "The JPA table name to use for this entity") final String entity,
            @CliOption(key = "query", mandatory = true, help = "The JPA table name to use for this entity") final String query,
            @CliOption(key = "author", mandatory = false, help = "Change set author") final String author,
            @CliOption(key = "id", mandatory = false, help = "Change set id") final String id) {
        Validate.isTrue(classCommons.exist(classA), "The specified class, '%s', doesn't exist", classA);
        Validate.isTrue(classCommons.exist(classB), "The specified class, '%s', doesn't exist", classB);

        classOperations.createClass(target, entity == null ? table : entity, table);
        final List<? extends FieldMetadata> fieldsA = classCommons.fields(classA);
        final List<? extends FieldMetadata> fieldsB = classCommons.fields(classB);

        Map<JavaSymbolName, JavaType> types = new HashMap<JavaSymbolName, JavaType>();
        Map<JavaSymbolName, String[]> columns = new HashMap<JavaSymbolName, String[]>();
        List<String> columnNames = new ArrayList<String>();

        for (FieldMetadata field : fieldsA) {
            types.put(fieldCommons.fieldName(field), fieldCommons.fieldType(field));
            columns.put(fieldCommons.fieldName(field), new String[] {
                    fieldCommons.columnName(field), fieldCommons.columnType(field)
            });
            columnNames.add(fieldCommons.columnName(field));
        }

        for (FieldMetadata field : fieldsB) {
            if (types.containsKey(fieldCommons.fieldName(field)))
                Validate.isTrue(false, "Fields conflict. Both merged classed contains field %s", fieldCommons.fieldName(field));
            types.put(fieldCommons.fieldName(field), fieldCommons.fieldType(field));
            columns.put(fieldCommons.fieldName(field), new String[] {
                    fieldCommons.columnName(field), fieldCommons.columnType(field)
            });
            columnNames.add(fieldCommons.columnName(field));
        }

        List<Element> elements = new ArrayList<Element>();
        elements.add(liquibaseOperations.createTable(table));
        for (Map.Entry<JavaSymbolName, JavaType> field :  types.entrySet()) {
            String[] column = columns.get(field.getKey());
            fieldOperations.addField(field.getKey(), field.getValue(), column[0], column[1], target);
            elements.add(
                    liquibaseOperations.addColumn(table, column[0], column[1])
            );
        }

        elements.add(liquibaseOperations.mergeTables(
                table,
                classCommons.tableName(classA),
                classCommons.tableName(classB),
                columnNames,
                query
        ));
        elements.add(liquibaseOperations.dropTable(classCommons.tableName(classA), false));
        elements.add(liquibaseOperations.dropTable(classCommons.tableName(classB), false));
        liquibaseOperations.createChangeSet(elements, author, id);

        classOperations.removeClass(classA);
        classOperations.removeClass(classB);
    }

}
