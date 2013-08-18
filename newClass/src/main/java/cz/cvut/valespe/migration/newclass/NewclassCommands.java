package cz.cvut.valespe.migration.newclass;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class NewclassCommands implements CommandMarker {
    
    @Reference private NewclassOperations operations;
    @Reference private ProjectOperations projectOperations;

    @CliAvailabilityIndicator({ "migrate new class" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }

    @CliCommand(value = "migrate new class", help = "Creates a new JPA persistent entity in SRC_MAIN_JAVA and DB migration script in SRC_MAIN_RESOURCES")
    public void newClass(
            @CliOption(key = "class", optionContext = "update,project", mandatory = true, help = "Name of the entity to create") final JavaType className,
            @CliOption(key = "table", mandatory = false, help = "The JPA table name to use for this entity") final String table,
            @CliOption(key = "abstract", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help = "Whether the generated class should be marked as abstract") final boolean createAbstract,
            @CliOption(key = "schema", mandatory = false, help = "The JPA table schema name to use for this entity") final String schema,
            @CliOption(key = "catalog", mandatory = false, help = "The JPA table catalog name to use for this entity") final String catalog,
            @CliOption(key = "tablespace", mandatory = false, help = "The JPA table catalog name to use for this entity") final String tablespace,
            @CliOption(key = "entityName", mandatory = false, help = "The name used to refer to the entity in queries") final String entityName) {
        operations.createEntity(className, entityName, table, schema, catalog);
        operations.createTable(getTableName(className, table), schema, catalog, tablespace);
    }

    private String getTableName(JavaType className, String table) {
        if (table == null)
            return className.getSimpleTypeName();
        else
            return table;
    }

}
