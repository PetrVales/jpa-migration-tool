package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JpaJavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.w3c.dom.Element;

import java.util.Arrays;

@Component
@Service
public class RemoveClassCommands implements CommandMarker {

    @Reference private ClassOperations classOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private ClassCommons classCommons;
    @Reference private LiquibaseOperations liquibaseOperations;

    public RemoveClassCommands() { }

    public RemoveClassCommands(
            ClassOperations classOperations,
            ProjectOperations projectOperations,
            ClassCommons classCommons,
            LiquibaseOperations liquibaseOperations) {
        this.classOperations = classOperations;
        this.projectOperations = projectOperations;
        this.classCommons = classCommons;
        this.liquibaseOperations = liquibaseOperations;
    }

    @CliAvailabilityIndicator({ "migrate remove class" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate remove class", help = "Remove class and its aspects and make record in migration.xml")
    public void removeClass(
            @CliOption(key = {"", "class"}, mandatory = true, help = "The java type to apply this annotation to") JavaType target,
            @CliOption(key = "skipDrop", mandatory = false, help = "skip dropping any data", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean skipDrop,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        Validate.notNull(classCommons.exist(target), "The type specified, '%s', doesn't exist", target.getSimpleTypeName());

        classOperations.removeClass(target);
        if (!skipDrop) {
            final Element element = liquibaseOperations.dropTable(classCommons.tableName(target), false);
            liquibaseOperations.createChangeSet(Arrays.asList(element), author, id);
        }
    }

}
