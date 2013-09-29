package cz.cvut.valespe.migration.mergeclass;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class MergeclassCommands implements CommandMarker {
    
    @Reference private MergeclassOperations operations;
    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    
    @CliAvailabilityIndicator({ "migrate merge class" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }
    
    @CliCommand(value = "migrate merge class", help = "Merge two classes into one and generate migration")
    public void mergeClass(
            @CliOption(key = "class", mandatory = true, help = "The java type to apply this annotation to") JavaType target,
            @CliOption(key = "classA", mandatory = true, help = "The java type to apply this annotation to") JavaType classA,
            @CliOption(key = "classB", mandatory = true, help = "The java type to apply this annotation to") JavaType classB,
            @CliOption(key = "table", mandatory = false, help = "The JPA table name to use for this entity") final String table,
            @CliOption(key = "schema", mandatory = false, help = "The JPA table schema name to use for this entity") final String schema,
            @CliOption(key = "catalog", mandatory = false, help = "The JPA table catalog name to use for this entity") final String catalog,
            @CliOption(key = "tablespace", mandatory = false, help = "The JPA table catalog name to use for this entity") final String tablespace) {
        final ClassOrInterfaceTypeDetails classATypeDetails = typeLocationService.getTypeDetails(classA);
        final ClassOrInterfaceTypeDetails classBTypeDetails = typeLocationService.getTypeDetails(classB);
        Validate.notNull(classATypeDetails, "The specified type, '%s', doesn't exist", classA.getSimpleTypeName());
        Validate.notNull(classBTypeDetails, "The specified type, '%s', doesn't exist", classB.getSimpleTypeName());

        operations.mergeClasses(target, classA, classB, table, schema, catalog, tablespace);
        mergeTables(target);
    }

    private void mergeTables(JavaType target) {
        final ClassOrInterfaceTypeDetails targetTypeDetails = typeLocationService.getTypeDetails(target);
        operations.createTable(targetTypeDetails);
    }

}
