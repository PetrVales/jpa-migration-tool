package cz.cvut.fit.valespe.migration.metadata.provider;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.metadata.MigrationEntityValues;
import cz.cvut.fit.valespe.migration.metadata.NewClassMetadata;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link cz.cvut.fit.valespe.migration.metadata.NewClassMetadata}. This type is called by Roo to retrieve the metadata for this newProperty-on.
 * Use this type to reference external types and services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique newProperty-on ITD identifier.
 * 
 * @since 1.1
 */
@Component
@Service
public final class NewClassMetadataProvider extends AbstractItdMetadataProvider {

    /**
     * The activate method for this OSGi component, this will be called by the OSGi container upon bundle activation 
     * (result of the 'addon install' command) 
     * 
     * @param context the component context can be used to get access to the OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(MigrationEntity.class.getName()));
    }
    
    /**
     * The deactivate method for this OSGi component, this will be called by the OSGi container upon bundle deactivation 
     * (result of the 'addon uninstall' command) 
     * 
     * @param context the component context can be used to get access to the OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(MigrationEntity.class.getName()));
    }
    
    /**
     * Return an instance of the Metadata offered by this newProperty-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata, String itdFilename) {
        // Pass dependencies required by the metadata in through its constructor
        return new NewClassMetadata(
                metadataIdentificationString,
                aspectName,
                governorPhysicalTypeMetadata,
                getMigrationEntityValues(governorPhysicalTypeMetadata)
        );
    }
    
    /**
     * Define the unique ITD file name extension, here the resulting file name will be **_ROO_Migration_Entity.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "Migration_Entity";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = NewClassMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = NewClassMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }
    
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return NewClassMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return NewClassMetadata.getMetadataIdentiferType();
    }

    private MigrationEntityValues getMigrationEntityValues(final PhysicalTypeMetadata governorPhysicalType) {
        final MigrationEntityValues annotationValues = new MigrationEntityValues(governorPhysicalType);
        if (annotationValues.isAnnotationFound()) {
            return annotationValues;
        }
        throw new IllegalStateException(getClass().getSimpleName()
                + " was triggered but not by migration entity.");
    }

}