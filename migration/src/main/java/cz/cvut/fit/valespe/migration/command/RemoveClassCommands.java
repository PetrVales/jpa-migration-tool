package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;
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

    private static final JavaType MIGRATION_ENTITY_ANNOTATION = new JavaType(MigrationEntity.class.getName());
    
    @Reference private ClassOperations classOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private LiquibaseOperations liquibaseOperations;

    public RemoveClassCommands() { }

    public RemoveClassCommands(
            ClassOperations classOperations,
            ProjectOperations projectOperations,
            TypeLocationService typeLocationService,
            LiquibaseOperations liquibaseOperations) {
        this.classOperations = classOperations;
        this.projectOperations = projectOperations;
        this.typeLocationService = typeLocationService;
        this.liquibaseOperations = liquibaseOperations;
    }

    @CliAvailabilityIndicator({ "migrate remove class" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate remove class", help = "Remove class and its aspects and make record in migration.xml")
    public void removeClass(
            @CliOption(key = "class", mandatory = true, help = "The java type to apply this annotation to") JavaType target,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService.getTypeDetails(target);
        Validate.notNull(javaTypeDetails, "The type specified, '%s', doesn't exist", target.getSimpleTypeName());

        classOperations.removeClass(target);
        removeTable(javaTypeDetails, author, id);
    }

    private void removeTable(ClassOrInterfaceTypeDetails javaTypeDetails, String author, String id) {
        AnnotationMetadata migrationEntity = javaTypeDetails.getAnnotation(MIGRATION_ENTITY_ANNOTATION);
        AnnotationAttributeValue<String> table = migrationEntity.getAttribute("table");
        final Element element = liquibaseOperations.dropTable(table == null ? "" : table.getValue(), false);
        liquibaseOperations.createChangeSet(Arrays.asList(element), author, id);
    }

}
