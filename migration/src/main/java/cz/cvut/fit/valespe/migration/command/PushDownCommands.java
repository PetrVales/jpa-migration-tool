package cz.cvut.fit.valespe.migration.command;

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
import org.springframework.roo.model.JpaJavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;
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
            @CliOption(key = "query", mandatory = false, help = "The name used to refer to the entity in queries") final String query,
            @CliOption(key = "author", mandatory = false, help = "The name used to refer to the entity in queries") final String author,
            @CliOption(key = "id", mandatory = false, help = "The name used to refer to the entity in queries") final String id) {
        final ClassOrInterfaceTypeDetails fromTypeDetails = typeLocationService.getTypeDetails(from);
        final ClassOrInterfaceTypeDetails toTypeDetails = typeLocationService.getTypeDetails(to);
        final FieldMetadata fieldMetadata = fromTypeDetails.getField(propertyName);
        final JavaType propertyType = fieldMetadata.getFieldType();
        AnnotationMetadata column = fieldMetadata.getAnnotation(COLUMN_ANNOTATION);
        String columnName = column.<String>getAttribute("name").getValue();
        String columnType = column.<String>getAttribute("columnDefinition").getValue();

        propertyOperations.addField(propertyName, propertyType, columnName, columnType, to);
        propertyOperations.removeField(propertyName, from);

        pushDownColumn(columnName, columnType, fromTypeDetails, toTypeDetails, query, author, id);
    }

    private void pushDownColumn(String columnName, String columnType, ClassOrInterfaceTypeDetails fromTypeDetails, ClassOrInterfaceTypeDetails toTypeDetails, String query, String author, String id) {
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
