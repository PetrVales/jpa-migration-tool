package cz.cvut.valespe.migration.splitclass;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class SplitclassCommands implements CommandMarker {
    
    @Reference private SplitclassOperations operations;
    @Reference private TypeLocationService typeLocationService;
    
    @CliAvailabilityIndicator({ "migrate split class" })
    public boolean isCommandAvailable() {
//        return operations.isCommandAvailable();
        return true;
    }

//    migrate split class --class ~.Original --classA ~.A --tableA a_table --propertiesA a common --classB ~.B --tableB b_table --propertiesB b common
    @CliCommand(value = "migrate split class", help = "")
    public void splitClass(
            @CliOption(key = {"", "class"}, mandatory = true, help = "The java type to apply this annotation to") final JavaType target,
            @CliOption(key = "classA", mandatory = true, help = "The java type to apply this annotation to") final JavaType targetA,
            @CliOption(key = "classB", mandatory = true, help = "The java type to apply this annotation to") final JavaType targetB,
            @CliOption(key = "tableA", mandatory = true, help = "The java type to apply this annotation to") final String tableA,
            @CliOption(key = "tableB", mandatory = true, help = "The java type to apply this annotation to") final String tableB,
            @CliOption(key = "propertiesA", mandatory = true, help = "The name of the field to add") final String propertiesAText,
            @CliOption(key = "propertiesB", mandatory = true, help = "The name of the field to add") final String propertiesBText,
            @CliOption(key = "schema", mandatory = false, help = "The JPA table schema name to use for this entity") final String schema,
            @CliOption(key = "catalog", mandatory = false, help = "The JPA table catalog name to use for this entity") final String catalog,
            @CliOption(key = "tablespace", mandatory = false, help = "The JPA table catalog name to use for this entity") final String tablespace) {
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

        operations.createClass(target, targetA, propertiesA, tableA, schema, catalog, tablespace);
        operations.createClass(target, targetB, propertiesB, tableB, schema, catalog, tablespace);
        operations.createTable(targetA, propertiesA, tableA, schema, catalog, tablespace);
        operations.createTable(targetB, propertiesB, tableB, schema, catalog, tablespace);
        operations.removeClass(target);
    }
}