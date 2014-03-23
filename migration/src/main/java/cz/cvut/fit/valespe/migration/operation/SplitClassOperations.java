package cz.cvut.fit.valespe.migration.operation;

import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaType;

import java.util.List;

public interface SplitClassOperations {

    void createClass(JavaType original, JavaType target, List<FieldMetadata> propertiesA, String table);

    void removeClass(JavaType target);

    void createTable(List<FieldMetadata> properties, String table);
}