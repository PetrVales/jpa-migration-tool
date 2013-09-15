package cz.cvut.valespe.migration.newproperty;

import cz.cvut.valespe.migration.newclass.MigrationEntity;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.jsr303.CollectionField;
import org.springframework.roo.classpath.operations.jsr303.DateField;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.ReservedWords;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

@Component
@Service
public class NewpropertyCommands implements CommandMarker {

    private static final JavaType MIGRATION_ENTITY_ANNOTATION = new JavaType(MigrationEntity.class.getName());
    
    @Reference private NewpropertyOperations operations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private ProjectOperations projectOperations;

    @CliAvailabilityIndicator({ "migrate new property" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }
    
    @CliCommand(value = "migrate new property", help = "Some helpful description")
    public void add(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "property", mandatory = true, help = "The name of the field to add") final JavaSymbolName propertyName,
            @CliOption(key = "propertyType", mandatory = true, help = "Type of new property") final JavaType propertyType,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "columnType", mandatory = true, help = "The JPA @Column name") final String columnType) {
        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService.getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '%s', doesn't exist", typeName);

        operations.addFieldToClass(propertyName, propertyType, columnName, columnType, javaTypeDetails);
        createColumn(columnName, columnType, javaTypeDetails);
    }

    private void createColumn(String columnName, String columnType, ClassOrInterfaceTypeDetails javaTypeDetails) {
        AnnotationMetadata migrationEntity = javaTypeDetails.getAnnotation(MIGRATION_ENTITY_ANNOTATION);
        AnnotationAttributeValue<String> table = migrationEntity.getAttribute("table");
        AnnotationAttributeValue<String> schema = migrationEntity.getAttribute("schema");
        AnnotationAttributeValue<String> catalog = migrationEntity.getAttribute("catalog");
        operations.createColumn(
                table == null ? "" : table.getValue(),
                schema == null ? "" : schema.getValue(),
                catalog == null ? "" : catalog.getValue(),
                columnName, columnType
        );
    }

}
