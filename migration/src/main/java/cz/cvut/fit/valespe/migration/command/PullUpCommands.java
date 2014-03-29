package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.PropertyOperations;
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

import java.util.List;

@Service
@Component
public class PullUpCommands implements CommandMarker {

    private static final JavaType COLUMN_ANNOTATION = new JavaType("javax.persistence.Column");

    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private ProjectOperations projectOperations;
    @Reference private PropertyOperations propertyOperations;

    @CliAvailabilityIndicator({ "migrate pull up" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

    @CliCommand(value = "migrate pull up", help = "Merge two classes into one and generate migration")
    public void pullUp(
            @CliOption(key = "class", mandatory = true, help = "The java type to apply this annotation to") JavaType target,
            @CliOption(key = "property", mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "author", mandatory = false, help = "The name used to refer to the entity in queries") final String author,
            @CliOption(key = "id", mandatory = false, help = "The name used to refer to the entity in queries") final String id) {
        final ClassOrInterfaceTypeDetails targetTypeDetails = typeLocationService.getTypeDetails(target);
        final ClassOrInterfaceTypeDetails parentTypeDetails = targetTypeDetails.getSuperclass();
        final FieldMetadata fieldMetadata = targetTypeDetails.getField(propertyName);
        final JavaType propertyType = fieldMetadata.getFieldType();
        AnnotationMetadata column = fieldMetadata.getAnnotation(COLUMN_ANNOTATION);
        String columnName = column.<String>getAttribute("name").getValue();
        String columnType = column.<String>getAttribute("columnDefinition").getValue();

        propertyOperations.addField(propertyName, propertyType, columnName, columnType, parentTypeDetails);
        propertyOperations.removeField(propertyName, targetTypeDetails);
//
//        final List<? extends FieldMetadata> targetDeclaredFields = targetTypeDetails.getDeclaredFields();
//        targetDeclaredFields.remove(fieldMetadata);
//        final List<? extends FieldMetadata> parentDeclaredFields = parentTypeDetails.getDeclaredFields();
//        parentDeclaredFields.add(fieldMetadata);

    }

}
