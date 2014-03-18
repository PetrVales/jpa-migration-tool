package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.RemovePropertyOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class RemovePropertyCommands implements CommandMarker {

    private static final JavaType MIGRATION_ENTITY_ANNOTATION = new JavaType(MigrationEntity.class.getName());
    private static final JavaType COLUMN_ANNOTATION = new JavaType("javax.persistence.Column");
    
    @Reference private RemovePropertyOperations removePropertyOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private LiquibaseOperations liquibaseOperations;

    public RemovePropertyCommands() { }

    public RemovePropertyCommands(RemovePropertyOperations removePropertyOperations, ProjectOperations projectOperations, TypeLocationService typeLocationService, LiquibaseOperations liquibaseOperations) {
        this.removePropertyOperations = removePropertyOperations;
        this.projectOperations = projectOperations;
        this.typeLocationService = typeLocationService;
        this.liquibaseOperations = liquibaseOperations;
    }

    @CliAvailabilityIndicator({ "migrate remove property" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate remove property", help = "Some helpful description")
    public void removeProperty(
            @CliOption(key = "class", mandatory = true, help = "The java type to apply this annotation to") JavaType typeName,
            @CliOption(key = "property", mandatory = true, help = "The java type to apply this annotation to") JavaSymbolName propertyName) {
        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService.getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '%s', doesn't exist", typeName);
        Validate.notNull(javaTypeDetails.declaresField(propertyName), "The specified property, '%s', of type, %s, doesn't exist", propertyName, typeName);

        removePropertyOperations.removeFieldFromClass(propertyName, javaTypeDetails);
        dropColumn(propertyName, javaTypeDetails);
    }

    private void dropColumn(JavaSymbolName propertyName, ClassOrInterfaceTypeDetails javaTypeDetails) {
        AnnotationMetadata migrationEntity = javaTypeDetails.getAnnotation(MIGRATION_ENTITY_ANNOTATION);
        AnnotationAttributeValue<String> table = migrationEntity.getAttribute("table");
        AnnotationAttributeValue<String> schema = migrationEntity.getAttribute("schema");
        AnnotationAttributeValue<String> catalog = migrationEntity.getAttribute("catalog");
        FieldMetadata declaredField = javaTypeDetails.getDeclaredField(propertyName);
        AnnotationMetadata column = declaredField.getAnnotation(COLUMN_ANNOTATION);
        AnnotationAttributeValue<String> columnName = column.getAttribute("name");
        liquibaseOperations.dropColumn(
                table == null ? "" : table.getValue(),
                schema == null ? "" : schema.getValue(),
                catalog == null ? "" : catalog.getValue(),
                columnName.getValue()
        );
    }

}
