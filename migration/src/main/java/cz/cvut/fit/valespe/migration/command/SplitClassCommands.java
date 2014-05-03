package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.*;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JpaJavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
@Service
public class SplitClassCommands implements CommandMarker {

    @Reference private ProjectOperations projectOperations;
    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private ClassOperations classOperations;
    @Reference private FieldOperations fieldOperations;
    @Reference private ClassCommons classCommons;
    @Reference private FieldCommons fieldCommons;

    public SplitClassCommands() { }

    public SplitClassCommands(ClassOperations classOperations, FieldOperations fieldOperations, ProjectOperations projectOperations, LiquibaseOperations liquibaseOperations, ClassCommons classCommons, FieldCommons fieldCommons) {
        this.projectOperations = projectOperations;
        this.liquibaseOperations = liquibaseOperations;
        this.classOperations = classOperations;
        this.fieldOperations = fieldOperations;
        this.classCommons = classCommons;
        this.fieldCommons = fieldCommons;
    }

    @CliAvailabilityIndicator({ "migrate split class" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

    @CliCommand(value = "migrate split class", help = "")
    public void splitClass(
            @CliOption(key = {"", "class"}, mandatory = true, help = "The java type to apply this annotation to") final JavaType target,
            @CliOption(key = "classA", mandatory = true, help = "The java type to apply this annotation to") final JavaType targetA,
            @CliOption(key = "classB", mandatory = true, help = "The java type to apply this annotation to") final JavaType targetB,
            @CliOption(key = "tableA", mandatory = true, help = "The java type to apply this annotation to") final String tableA,
            @CliOption(key = "tableB", mandatory = true, help = "The java type to apply this annotation to") final String tableB,
            @CliOption(key = "entityA", mandatory = false, help = "The java type to apply this annotation to") final String entityA,
            @CliOption(key = "entityB", mandatory = false, help = "The java type to apply this annotation to") final String entityB,
            @CliOption(key = "propertiesA", mandatory = true, help = "The name of the field to add") final String propertiesAText,
            @CliOption(key = "propertiesB", mandatory = true, help = "The name of the field to add") final String propertiesBText,
            @CliOption(key = "queryA", mandatory = true, help = "The name of the field to add") final String queryA,
            @CliOption(key = "queryB", mandatory = true, help = "The name of the field to add") final String queryB,
            @CliOption(key = "skipDrop", mandatory = false, help = "skip dropping any data", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean skipDrop,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        Validate.isTrue(classCommons.exist(target), "The specified class, '%s', doesn't exist", target);
        Validate.isTrue(!classCommons.exist(targetA), "The specified class, '%s', already exists", targetA);
        Validate.isTrue(!classCommons.exist(targetB), "The specified class, '%s', already exists", targetB);
        final String table = classCommons.tableName(target);

        String[] propertyNamesA = propertiesAText.split(",");
        String[] propertyNamesB = propertiesBText.split(",");

        List<FieldMetadata> propertiesA = new ArrayList<FieldMetadata>();
        List<String> columnsA = new ArrayList<String>();
        List<FieldMetadata> propertiesB = new ArrayList<FieldMetadata>();
        List<String> columnsB = new ArrayList<String>();

        for (FieldMetadata field : classCommons.fields(target)) {
            for (String fieldName : propertyNamesA) {
                if (fieldCommons.fieldName(field).getSymbolName().equals(fieldName)) {
                    propertiesA.add(field);
                    columnsA.add(fieldCommons.columnName(field));
                }
            }
            for (String fieldName : propertyNamesB) {
                if (fieldCommons.fieldName(field).getSymbolName().equals(fieldName)) {
                    propertiesB.add(field);
                    columnsB.add(fieldCommons.columnName(field));
                }
            }
        }

        List<Element> elements = new LinkedList<Element>();

        classOperations.createClass(targetA, entityA == null ? tableA : entityA, tableA);
        elements.add(liquibaseOperations.createTable(tableA));
        classOperations.createClass(targetB, entityB == null ? tableB : entityB, tableB);
        elements.add(liquibaseOperations.createTable(tableB));

        elements.addAll(addPropertiesToClass(targetA, tableA, propertiesA));
        elements.addAll(addPropertiesToClass(targetB, tableB, propertiesB));

        elements.add(liquibaseOperations.copyData(table, tableA, columnsA, queryA));
        elements.add(liquibaseOperations.copyData(table, tableB, columnsB, queryB));

        classOperations.removeClass(target);
        if (!skipDrop)
            elements.add(liquibaseOperations.dropTable(table, true));

        liquibaseOperations.createChangeSet(elements, author, id);
    }

    private List<Element> addPropertiesToClass(JavaType target, String table, List<FieldMetadata> properties) {
        List<Element> elements = new LinkedList<Element>();
        for (FieldMetadata field : properties) {
            final String name = fieldCommons.columnName(field);
            final String type = fieldCommons.columnType(field);
            fieldOperations.addField(field.getFieldName(), field.getFieldType(), name, type, target);
            elements.add(liquibaseOperations.addColumn(table, name, type));
        }
        return elements;
    }
}