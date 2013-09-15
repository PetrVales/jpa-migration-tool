package cz.cvut.valespe.migration.moveproperty;

import cz.cvut.valespe.migration.newclass.MigrationEntity;
import cz.cvut.valespe.migration.newproperty.NewpropertyOperations;
import cz.cvut.valespe.migration.removeproperty.RemovepropertyOperations;
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
public class MovepropertyCommands implements CommandMarker {

    private static final JavaType MIGRATION_ENTITY_ANNOTATION = new JavaType(MigrationEntity.class.getName());
    private static final JavaType COLUMN_ANNOTATION = new JavaType("javax.persistence.Column");
    
    @Reference private NewpropertyOperations newpropertyOperations;
    @Reference private RemovepropertyOperations removepropertyOperations;
    @Reference private MovepropertyOperations movepropertyOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private ProjectOperations projectOperations;
    
    @CliAvailabilityIndicator({ "migrate move property" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }
    
    @CliCommand(value = "migrate move property", help = "Some helpful description")
    public void add(
            @CliOption(key = "from", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType fromType,
            @CliOption(key = "to", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType toType,
            @CliOption(key = "property", mandatory = true, help = "The name of the field to add") final JavaSymbolName propertyName) {
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
        removepropertyOperations.deleteFieldFromClass(propertyName, fromTypeDetails);
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
        movepropertyOperations.moveColumn(
                columnName, columnType,
                fromTable == null ? "" : fromTable.getValue(),
                fromSchema == null ? "" : fromSchema.getValue(),
                fromCatalog == null ? "" : fromCatalog.getValue(),
                toTable == null ? "" : toTable.getValue(),
                toSchema == null ? "" : toSchema.getValue(),
                toCatalog == null ? "" : toCatalog.getValue());
    }
    
}