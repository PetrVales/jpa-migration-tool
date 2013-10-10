package cz.cvut.valespe.migration.splitclass;

import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaType;

import java.util.List;

public interface SplitclassOperations {

    void createClass(JavaType original, JavaType target, List<FieldMetadata> propertiesA, String table, String schema, String catalog, String tablespace);

    void removeClass(JavaType target);

    void createTable(List<FieldMetadata> properties, String table, String schema, String catalog, String tablespace);
}