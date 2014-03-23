package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.*;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@Component
@Service
public class SplitClassCommands implements CommandMarker {

    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private NewClassOperations newClassOperations;
    @Reference private NewPropertyOperations newPropertyOperations;
    @Reference private RemoveClassOperations removeClassOperations;

    public SplitClassCommands() { }

    public SplitClassCommands(NewClassOperations newClassOperations, NewPropertyOperations newPropertyOperations, RemoveClassOperations removeClassOperations, ProjectOperations projectOperations, LiquibaseOperations liquibaseOperations, TypeLocationService typeLocationService) {
        this.projectOperations = projectOperations;
        this.liquibaseOperations = liquibaseOperations;
        this.typeLocationService = typeLocationService;
        this.newClassOperations = newClassOperations;
        this.newPropertyOperations = newPropertyOperations;
        this.removeClassOperations = removeClassOperations;
    }

    @CliAvailabilityIndicator({ "migrate split class" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

//    migrate split class --class ~.Original --classA ~.A --tableA a_table --propertiesA a common --classB ~.B --tableB b_table --propertiesB b common
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
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        final ClassOrInterfaceTypeDetails classTypeDetails = typeLocationService.getTypeDetails(target);

        String[] propertyNamesA = propertiesAText.split(",");
        String[] propertyNamesB = propertiesBText.split(",");

        List<FieldMetadata> propertiesA = new ArrayList<FieldMetadata>();
        List<FieldMetadata> propertiesB = new ArrayList<FieldMetadata>();

        for (FieldMetadata field : classTypeDetails.getDeclaredFields()) {
            for (String fieldName : propertyNamesA) {
                if (field.getFieldName().getSymbolName().equals(fieldName)) {
                    propertiesA.add(field);
                }
            }
            for (String fieldName : propertyNamesB) {
                if (field.getFieldName().getSymbolName().equals(fieldName)) {
                    propertiesB.add(field);
                }
            }
        }

        List<Element> elements = new LinkedList<Element>();

        newClassOperations.createEntity(targetA, entityA == null ? tableA : entityA, tableA);
        elements.add(liquibaseOperations.createTable(tableA));
        newClassOperations.createEntity(targetB, entityB == null ? tableB : entityB, tableB);
        elements.add(liquibaseOperations.createTable(tableB));

        elements.addAll(addPropertiesToClass(targetA, tableA, propertiesA));
        elements.addAll(addPropertiesToClass(targetB, tableB, propertiesB));

        removeClassOperations.removeClass(target);

        final AnnotationMetadata annotation = classTypeDetails.getAnnotation(MigrationEntity.MIGRATION_ENTITY);
        final AnnotationAttributeValue<String> name = annotation.getAttribute("table");
        elements.add(liquibaseOperations.dropTable(name.getValue(), true));
        liquibaseOperations.createChangeSet(elements, author, id);
    }

    private List<Element> addPropertiesToClass(JavaType target, String table, List<FieldMetadata> properties) {
        List<Element> elements = new LinkedList<Element>();
        for (FieldMetadata field : properties) {
            final ClassOrInterfaceTypeDetails classTypeDetails = typeLocationService.getTypeDetails(target);
            final AnnotationMetadata annotation = field.getAnnotation(JpaJavaType.COLUMN);
            final AnnotationAttributeValue<String> name = annotation.getAttribute("name");
            final AnnotationAttributeValue<String> columnDefinition = annotation.getAttribute("columnDefinition");
            newPropertyOperations.addFieldToClass(field.getFieldName(), field.getFieldType(), name.getValue(), columnDefinition.getValue(), classTypeDetails);
            elements.add(liquibaseOperations.addColumn(table, name.getValue(), columnDefinition.getValue()));
        }
        return elements;
    }
}