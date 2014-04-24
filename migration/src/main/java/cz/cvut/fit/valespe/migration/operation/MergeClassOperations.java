package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;

public interface MergeClassOperations {

    void mergeClasses(JavaType target, JavaType classA, JavaType classB, String table);

    void createTable(ClassOrInterfaceTypeDetails targetTypeDetails);
}
