package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.PropertyOperations;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

import java.util.logging.Logger;

@Service
@Component
public class PushDownCommands implements CommandMarker {

    private Logger log = Logger.getLogger(getClass().getName());

    private static final JavaType COLUMN_ANNOTATION = new JavaType("javax.persistence.Column");

    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private ProjectOperations projectOperations;
    @Reference private PropertyOperations propertyOperations;

    @CliAvailabilityIndicator({ "migrate push down" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

    @CliCommand(value = "migrate push down", help = "Merge two classes into one and generate migration")
    public void pushDown(
            @CliOption(key = "from", mandatory = true, help = "The java type to apply this annotation to") JavaType from,
            @CliOption(key = "to", mandatory = true, help = "The java type to apply this annotation to") JavaType to,
            @CliOption(key = "property", mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "author", mandatory = false, help = "The name used to refer to the entity in queries") final String author,
            @CliOption(key = "id", mandatory = false, help = "The name used to refer to the entity in queries") final String id) {
        final ClassOrInterfaceTypeDetails fromTypeDetails = typeLocationService.getTypeDetails(from);
        log.info(from.toString());
        final ClassOrInterfaceTypeDetails toTypeDetails = typeLocationService.getTypeDetails(to);
        final FieldMetadata fieldMetadata = fromTypeDetails.getField(propertyName);
        log.info(fieldMetadata.toString());
        final JavaType propertyType = fieldMetadata.getFieldType();
        log.info(propertyType.toString());
        AnnotationMetadata column = fieldMetadata.getAnnotation(COLUMN_ANNOTATION);
        log.info(column.toString());
        String columnName = column.<String>getAttribute("name").getValue();
        String columnType = column.<String>getAttribute("columnDefinition").getValue();
        log.info(columnName);
        log.info(columnType);

        propertyOperations.addField(propertyName, propertyType, columnName, columnType, toTypeDetails);
        propertyOperations.removeField(propertyName, fromTypeDetails);
//
//        final List<? extends FieldMetadata> targetDeclaredFields = targetTypeDetails.getDeclaredFields();
//        targetDeclaredFields.remove(fieldMetadata);
//        final List<? extends FieldMetadata> parentDeclaredFields = parentTypeDetails.getDeclaredFields();
//        parentDeclaredFields.add(fieldMetadata);

    }
}
