package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.FieldOperations;
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
import org.springframework.roo.model.JpaJavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

@Component
@Service
public class MovePropertyCommands implements CommandMarker {

    @Reference private FieldOperations fieldOperations;
    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private ProjectOperations projectOperations;

    public MovePropertyCommands() { }

    public MovePropertyCommands(FieldOperations fieldOperations, LiquibaseOperations liquibaseOperations, TypeLocationService typeLocationService, ProjectOperations projectOperations) {
        this.fieldOperations = fieldOperations;
        this.liquibaseOperations = liquibaseOperations;
        this.typeLocationService = typeLocationService;
        this.projectOperations = projectOperations;
    }

    @CliAvailabilityIndicator({ "migrate move property" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate move property", help = "Some helpful description")
    public void moveProperty(
            @CliOption(key = "from", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType fromType,
            @CliOption(key = "to", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType toType,
            @CliOption(key = {"", "property"}, mandatory = true, help = "The name of the field to moveProperty") final JavaSymbolName propertyName,
            @CliOption(key = "query", mandatory = true, help = "Query") final String query,
            @CliOption(key = "author", mandatory = false, help = "The name used to refer to the entity in queries") final String author,
            @CliOption(key = "id", mandatory = false, help = "The name used to refer to the entity in queries") final String id
        ) {
        final ClassOrInterfaceTypeDetails fromTypeDetails = typeLocationService.getTypeDetails(fromType);
        Validate.notNull(fromTypeDetails, "The type specified, '%s', doesn't exist", fromType);
        final ClassOrInterfaceTypeDetails toTypeDetails = typeLocationService.getTypeDetails(toType);
        Validate.notNull(toTypeDetails, "The type specified, '%s', doesn't exist", toType);
        FieldMetadata property = fromTypeDetails.getField(propertyName);
        AnnotationMetadata column = property.getAnnotation(JpaJavaType.COLUMN);
        AnnotationAttributeValue<String> columnName = column.getAttribute("name");
        AnnotationAttributeValue<String> columnType = column.getAttribute("columnDefinition");

        fieldOperations.addField(propertyName, property.getFieldType(), columnName.getValue(), columnType.getValue(), toType);
        moveColumn(columnName.getValue(), columnType.getValue(), fromTypeDetails, toTypeDetails, query, author, id);
        fieldOperations.removeField(propertyName, fromType);
    }

    private void moveColumn(String columnName, String columnType, ClassOrInterfaceTypeDetails fromTypeDetails, ClassOrInterfaceTypeDetails toTypeDetails, String query, String author, String id) {
        AnnotationMetadata fromEntity = fromTypeDetails.getAnnotation(JpaJavaType.TABLE);
        AnnotationAttributeValue<String> fromTable = fromEntity.getAttribute("name");
        AnnotationMetadata toEntity = toTypeDetails.getAnnotation(JpaJavaType.TABLE);
        AnnotationAttributeValue<String> toTable = toEntity.getAttribute("name");

        List<Element> elements = new LinkedList<Element>();
        elements.add(liquibaseOperations.addColumn(toTable.getValue(), columnName, columnType));
        elements.add(liquibaseOperations.copyColumnData(fromTable.getValue(), toTable.getValue(), columnName, query));
        elements.add(liquibaseOperations.dropColumn(fromTable.getValue(), columnName));

        liquibaseOperations.createChangeSet(elements, author, id);
    }
    
}