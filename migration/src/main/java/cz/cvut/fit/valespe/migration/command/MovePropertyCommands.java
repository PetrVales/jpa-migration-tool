package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.MigrationSetupOperations;
import cz.cvut.fit.valespe.migration.operation.MovePropertyOperations;
import cz.cvut.fit.valespe.migration.operation.NewPropertyOperations;
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

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

@Component
@Service
public class MovePropertyCommands implements CommandMarker {

    private static final JavaType MIGRATION_ENTITY_ANNOTATION = new JavaType(MigrationEntity.class.getName());
    private static final JavaType COLUMN_ANNOTATION = new JavaType("javax.persistence.Column");
    
    @Reference private NewPropertyOperations newpropertyOperations;
    @Reference private RemovePropertyOperations removepropertyOperations;
    @Reference private MovePropertyOperations movePropertyOperations;
    @Reference private MigrationSetupOperations migrationSetupOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private ProjectOperations projectOperations;

    public MovePropertyCommands() { }

    public MovePropertyCommands(NewPropertyOperations newpropertyOperations, RemovePropertyOperations removepropertyOperations, MovePropertyOperations movePropertyOperations, MigrationSetupOperations migrationSetupOperations, TypeLocationService typeLocationService, ProjectOperations projectOperations) {
        this.newpropertyOperations = newpropertyOperations;
        this.removepropertyOperations = removepropertyOperations;
        this.movePropertyOperations = movePropertyOperations;
        this.migrationSetupOperations = migrationSetupOperations;
        this.typeLocationService = typeLocationService;
        this.projectOperations = projectOperations;
    }

    @CliAvailabilityIndicator({ "migrate move property" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && migrationSetupOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate move property", help = "Some helpful description")
    public void moveProperty(
            @CliOption(key = "from", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType fromType,
            @CliOption(key = "to", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType toType,
            @CliOption(key = "property", mandatory = true, help = "The name of the field to moveProperty") final JavaSymbolName propertyName) {
        final ClassOrInterfaceTypeDetails fromTypeDetails = typeLocationService.getTypeDetails(fromType);
        Validate.notNull(fromTypeDetails, "The type specified, '%s', doesn't exist", fromType);
        final ClassOrInterfaceTypeDetails toTypeDetails = typeLocationService.getTypeDetails(toType);
        Validate.notNull(toTypeDetails, "The type specified, '%s', doesn't exist", toType);
        FieldMetadata property = fromTypeDetails.getField(propertyName);
        AnnotationMetadata column = property.getAnnotation(COLUMN_ANNOTATION);
        AnnotationAttributeValue<String> columnName = column.getAttribute("name");
        AnnotationAttributeValue<String> columnType = column.getAttribute("columnDefinition");

        newpropertyOperations.addFieldToClass(propertyName, property.getFieldType(), columnName.getValue(), columnType.getValue(), toTypeDetails);
        moveColumn(columnName.getValue(), columnType.getValue(), fromTypeDetails, toTypeDetails);
        removepropertyOperations.removeFieldFromClass(propertyName, fromTypeDetails);
    }

    private void moveColumn(String columnName, String columnType, ClassOrInterfaceTypeDetails fromTypeDetails, ClassOrInterfaceTypeDetails toTypeDetails) {
        AnnotationMetadata fromEntity = fromTypeDetails.getAnnotation(MIGRATION_ENTITY_ANNOTATION);
        AnnotationAttributeValue<String> fromTable = fromEntity.getAttribute("table");
        AnnotationAttributeValue<String> fromSchema = fromEntity.getAttribute("schema");
        AnnotationAttributeValue<String> fromCatalog = fromEntity.getAttribute("catalog");
        AnnotationMetadata toEntity = toTypeDetails.getAnnotation(MIGRATION_ENTITY_ANNOTATION);
        AnnotationAttributeValue<String> toTable = toEntity.getAttribute("table");
        AnnotationAttributeValue<String> toSchema = toEntity.getAttribute("schema");
        AnnotationAttributeValue<String> toCatalog = toEntity.getAttribute("catalog");
        movePropertyOperations.moveColumn(
                columnName, columnType,
                fromTable == null ? "" : fromTable.getValue(),
                fromSchema == null ? "" : fromSchema.getValue(),
                fromCatalog == null ? "" : fromCatalog.getValue(),
                toTable == null ? "" : toTable.getValue(),
                toSchema == null ? "" : toSchema.getValue(),
                toCatalog == null ? "" : toCatalog.getValue());
    }
    
}