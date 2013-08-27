package cz.cvut.valespe.migration.removeclass;

import cz.cvut.valespe.migration.newclass.MigrationEntity;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class RemoveclassCommands implements CommandMarker {

    private static final JavaType MIGRATION_ENTITY_ANNOTATION = new JavaType(MigrationEntity.class.getName());
    
    @Reference private RemoveclassOperations operations;
    @Reference private TypeLocationService typeLocationService;
    
    @CliAvailabilityIndicator({ "migrate remove class" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }
    
    @CliCommand(value = "migrate remove class", help = "Remove class and its aspects and make record in migration.xml")
    public void add(@CliOption(key = "class", mandatory = true, help = "The java type to apply this annotation to") JavaType target) {
        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService.getTypeDetails(target);
        Validate.notNull(javaTypeDetails, "The type specified, '%s', doesn't exist", target.getSimpleTypeName());

        operations.removeClass(target);
        removeTable(javaTypeDetails);
    }

    private void removeTable(ClassOrInterfaceTypeDetails javaTypeDetails) {
        AnnotationMetadata migrationEntity = javaTypeDetails.getAnnotation(MIGRATION_ENTITY_ANNOTATION);
        AnnotationAttributeValue<String> table = migrationEntity.getAttribute("table");
        AnnotationAttributeValue<String> schema = migrationEntity.getAttribute("schema");
        AnnotationAttributeValue<String> catalog = migrationEntity.getAttribute("catalog");
        operations.dropTable(
                table == null ? "" : table.getValue(),
                schema == null ? "" : schema.getValue(),
                catalog == null ? "" : catalog.getValue(),
                false
        );
    }

}
