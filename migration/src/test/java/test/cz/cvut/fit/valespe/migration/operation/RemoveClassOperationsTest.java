package test.cz.cvut.fit.valespe.migration.operation;

import cz.cvut.fit.valespe.migration.operation.RemoveClassOperations;
import cz.cvut.fit.valespe.migration.operation.impl.RemoveClassOperationsImpl;
import org.junit.Test;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

import java.io.StringBufferInputStream;

import static org.mockito.Mockito.*;

public class RemoveClassOperationsTest {

    private static final JavaType CLASS_TYPE = new JavaType("test.Class");
    private static final String CLASS_PATH = "class-path";

    private ProjectOperations projectOperations = mock(ProjectOperations.class);
    private PathResolver pathResolver = mock(PathResolver.class);
    private FileManager fileManager = mock(FileManager.class);
    private TypeManagementService typeManagementService = mock(TypeManagementService.class);
    private RemoveClassOperations removeClassOperations =
            new RemoveClassOperationsImpl(projectOperations, pathResolver, fileManager, typeManagementService);

    @Test
    public void removesClass() {
        when(pathResolver.getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, CLASS_TYPE)).thenReturn(CLASS_PATH);

        removeClassOperations.removeClass(CLASS_TYPE);

        verify(fileManager, times(1)).delete(CLASS_PATH);
    }

}
