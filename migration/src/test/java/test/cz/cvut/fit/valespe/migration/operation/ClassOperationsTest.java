package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import cz.cvut.fit.valespe.migration.operation.impl.ClassOperationsImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ClassOperationsTest {

    private static final LogicalPath SRC_MAIN = mock(LogicalPath.class);
    private static final JavaType CLASS = new JavaType("className");
    private static final String CLASS_PATH = "class-path";
    private static final String ENTITY = "entity-name";
    private static final String TABLE = "table-name";

    private final PathResolver pathResolver = mock(PathResolver.class);
    private final FileManager fileManager = mock(FileManager.class);
    private final TypeManagementService typeManagementService = mock(TypeManagementService.class);
    private final TypeLocationService typeLocationService = mock(TypeLocationService.class);
    private final ClassOperations classOperations = new ClassOperationsImpl(pathResolver, fileManager, typeManagementService, typeLocationService);

    @Test
    public void createClass() {
        when(pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA)).thenReturn(SRC_MAIN);

        classOperations.createClass(CLASS, ENTITY, TABLE);

        ArgumentCaptor<ClassOrInterfaceTypeDetails> argument = ArgumentCaptor.forClass(ClassOrInterfaceTypeDetails.class);
        verify(typeManagementService, times(1)).createOrUpdateTypeOnDisk(argument.capture());

        assertFalse(argument.getValue().isAbstract());
        assertEquals(argument.getValue().getName(), CLASS);
        assertTrue(argument.getValue().getDeclaredFields().isEmpty());
        assertFalse(argument.getValue().getAnnotations().isEmpty());
    }

    @Test
    public void removesClass() {
        when(pathResolver.getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, CLASS)).thenReturn(CLASS_PATH);

        classOperations.removeClass(CLASS);

        verify(fileManager, times(1)).delete(CLASS_PATH);
    }

}
