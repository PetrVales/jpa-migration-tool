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

@Service
@Component
public class PullUpCommands implements CommandMarker {

    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private FieldOperations fieldOperations;
    @Reference private ClassCommons classCommons;
    @Reference private FieldCommons fieldCommons;

    @CliAvailabilityIndicator({ "migrate pull up" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

    @CliCommand(value = "migrate pull up", help = "Merge two classes into one and generate migration")
    public void pullUp(
            @CliOption(key = "class", mandatory = true, help = "The java type to apply this annotation to") JavaType target,
            @CliOption(key = "property", mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "query", mandatory = true, help = "The name used to refer to the entity in queries") final String query,
            @CliOption(key = "skipDrop", mandatory = false, help = "skip dropping any data", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean skipDrop,
            @CliOption(key = "author", mandatory = false, help = "The name used to refer to the entity in queries") final String author,
            @CliOption(key = "id", mandatory = false, help = "The name used to refer to the entity in queries") final String id) {
        Validate.isTrue(classCommons.exist(target), "Specified class, '%s', doesn't exist", target);
        Validate.isTrue(classCommons.hasField(target, propertyName), "Specified class, '%s', doesn't have field %s", target, propertyName);

        final JavaType parent = classCommons.getParentType(target);

        Validate.isTrue(parent != null && !parent.isCoreType(), "Specified class, '%s', doesn't have parent class", target);

        final FieldMetadata field = classCommons.field(target, propertyName);
        final JavaType propertyType = fieldCommons.fieldType(field);
        String columnName = fieldCommons.columnName(field);
        String columnType = fieldCommons.columnType(field);

        if (!classCommons.hasField(parent, propertyName))
            fieldOperations.addField(propertyName, propertyType, columnName, columnType, parent);
        fieldOperations.removeField(propertyName, target);
        pullUpColumn(columnName, columnType, target, parent, query, skipDrop, author, id);

    }

    private void pullUpColumn(String columnName, String columnType, JavaType target, JavaType parent, String query, Boolean skipDrop, String author, String id) {
        final String fromTable = classCommons.tableName(target);
        final String toTable = classCommons.tableName(parent);

        List<Element> elements = new LinkedList<Element>();
        elements.add(liquibaseOperations.addColumn(toTable, columnName, columnType));
        elements.add(liquibaseOperations.copyColumnData(fromTable, toTable, columnName, query));
        if (!skipDrop)
            elements.add(liquibaseOperations.dropColumn(fromTable, columnName));

        liquibaseOperations.createChangeSet(elements, author, id);
    }

}
