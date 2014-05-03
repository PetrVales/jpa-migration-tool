package cz.cvut.fit.valespe.migration.command;

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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@Service
@Component
public class PushDownCommands implements CommandMarker {

    private Logger log = Logger.getLogger(getClass().getName());

    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private FieldOperations fieldOperations;
    @Reference private ClassCommons classCommons;
    @Reference private FieldCommons fieldCommons;

    @CliAvailabilityIndicator({ "migrate push down" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

    @CliCommand(value = "migrate push down", help = "Merge two classes into one and generate migration")
    public void pushDown(
            @CliOption(key = "from", mandatory = true, help = "The java type to apply this annotation to") JavaType from,
            @CliOption(key = "to", mandatory = true, help = "The java type to apply this annotation to") JavaType to,
            @CliOption(key = "property", mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "query", mandatory = false, help = "The name used to refer to the entity in queries") final String query,
            @CliOption(key = "skipDrop", mandatory = false, help = "skip dropping any data", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean skipDrop,
            @CliOption(key = "author", mandatory = false, help = "The name used to refer to the entity in queries") final String author,
            @CliOption(key = "id", mandatory = false, help = "The name used to refer to the entity in queries") final String id) {
        Validate.isTrue(classCommons.exist(from), "Specified class, '%s', doesn't exist", from);
        Validate.isTrue(classCommons.exist(to), "Specified class, '%s', doesn't exist", to);
        Validate.isTrue(classCommons.hasField(from, propertyName), "Specified class, '%s', doesn't have field %s", from, propertyName);

        final FieldMetadata field = classCommons.field(from, propertyName);
        final JavaType fieldType = fieldCommons.fieldType(field);
        String columnName = fieldCommons.columnName(field);
        String columnType = fieldCommons.columnType(field);

        fieldOperations.addField(propertyName, fieldType, columnName, columnType, to);
        fieldOperations.removeField(propertyName, from);

        pushDownColumn(columnName, columnType, from, to, query, skipDrop, author, id);
    }

    private void pushDownColumn(String columnName, String columnType, JavaType from, JavaType to, String query, Boolean skipDrop, String author, String id) {
        String fromTable = classCommons.tableName(from);
        String toTable = classCommons.tableName(to);

        List<Element> elements = new LinkedList<Element>();
        elements.add(liquibaseOperations.addColumn(toTable, columnName, columnType));
        elements.add(liquibaseOperations.copyColumnData(fromTable, toTable, columnName, query));
        if (!skipDrop)
            elements.add(liquibaseOperations.dropColumn(fromTable, columnName));

        liquibaseOperations.createChangeSet(elements, author, id);
    }

}
