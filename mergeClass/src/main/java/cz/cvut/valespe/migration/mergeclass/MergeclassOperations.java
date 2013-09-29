package cz.cvut.valespe.migration.mergeclass;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;

public interface MergeclassOperations {

    void mergeClasses(JavaType target, JavaType classA, JavaType classB, String table, String schema, String catalog, String tablespace);

    void createTable(ClassOrInterfaceTypeDetails targetTypeDetails);
}
