/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */
package biz.c24.io.mule;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.MetaDataSwitch;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.MetaDataKeyParam;
import org.mule.api.annotations.param.MetaDataKeyParamAffectsType;
import org.mule.api.annotations.param.Optional;
import org.mule.common.DefaultResult;
import org.mule.common.Result;
import org.mule.common.Result.Status;
import org.mule.common.metadata.ConnectorMetaDataEnabled;
import org.mule.common.metadata.DefaultMetaData;
import org.mule.common.metadata.DefaultMetaDataKey;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.builder.DefaultMetaDataBuilder;
import org.mule.common.metadata.builder.PojoMetaDataBuilder;
import org.mule.config.i18n.CoreMessages;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import biz.c24.api.LicenseException;
import biz.c24.io.api.C24;
import biz.c24.io.api.C24.C24Reader;
import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.transform.Transform;

/**
 * C24 Mule Connector
 * 
 * @author C24 Technologies Limited
 * 
 * The C24 Connector allows you to use C24 iO message parsing, validation, transformation and generation
 * in your Mule flows.
 *
 */
@Module(name = "c24", schemaVersion = "1.0.0", friendlyName = "C24 Connector", metaData = MetaDataSwitch.DYNAMIC)
//        namespace = "http://schema.c24.biz/mule", schemaLocation = "http://schema.c24.biz/mule.xsd")
public class C24Connector implements ConnectorMetaDataEnabled  {
    
    /**
     * The C24 iO licence to use (required for licensed data models only)
     */
    @Optional
    @Configurable
    private String licenceFile;
    
    protected transient Log logger = LogFactory.getLog(getClass());
    
    protected FileWriter writer = null;
    
    private final boolean DEBUG = false;
    
    protected java.io.Writer getWriter() {
        if(writer == null) {
            try {
                writer = new FileWriter(new java.io.File("/tmp/op.txt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return writer;
    }
    
    protected void debug(String str) {
        if(DEBUG) {
            try {
                getWriter().write(str);
                getWriter().write('\n');
                getWriter().flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private String basePackage = null;
    
    /**
     * Restricts the options provided to DataSense to matches beneath the specified package
     * @param basePackage
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        // Force a reload of metadata
        this.reflections = null;
    }
    
    /**
     * Returns the root package to which all DataSense matches must belong to
     * @return
     */
    public String getBasePackage() {
        return basePackage;
    }
    
   
    /**
     * Returns the path to the C24 iO licence file that will be used
     * See the C24 iO C24.init().withLicence(...) documentation to understand
     * how the file system & classpath will be searched to find an appropriate licence 
     * @return
     */
    public String getLicenceFile() {
        return licenceFile;
    }
    
    /**
     * Set the path to the C24 iO licence file that will be used
     * See the C24 iO C24.init().withLicence(...) documentation to understand
     * how the file system & classpath will be searched to find an appropriate licence 
     * @param licenceFile
     * @throws IOException 
     * @throws LicenseException 
     */
    public void setLicenceFile(String licenceFile) throws LicenseException, IOException {
        this.licenceFile = licenceFile;
        C24.init().withLicence(licenceFile);
    }
    
    
    private Transform getTransform(String transformName, MuleEvent event) throws C24Exception, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // Check we have a valid transformation
        Class<?> transformClass =  Class.forName(transformName);
         
        if(!Transform.class.isAssignableFrom(transformClass)) {
            throw new C24Exception(CoreMessages.createStaticMessage(transformClass.getName() + " is not a valid C24 Transform"), event);
        }
        
        Transform t = (Transform) transformClass.newInstance();
        
        if(t.getInputCount() != 1 || t.getOutputCount() != 1) {
            throw new C24Exception(CoreMessages.createStaticMessage("The Mule C24 Connector only currently supports 1:1 transformations"), event);
        }
        
        return t;
    }
    
    /**
     * c24 Transformer
     * <p/>
     * {@sample.xml ../../../doc/C24-connector.xml.sample c24:transform}
     *
     * @param transform The Transform to use
     * @param source The String, Reader or InputStream to parse into a C24 Object
     * @param encoding The encoding of the input data
     * @param validateInput Whether the source message should be validated post-parsing
     * @param validateOutput Whether the result of the transformation should be checked for validity
     * @param event The Mule event
     * @return The marshaled result of the transformation
     * @throws Exception An exception when there's an error
     */
    @Processor
    @Inject
    public Object transform(@MetaDataKeyParam(affects=MetaDataKeyParamAffectsType.OUTPUT) String transform,
                            @Default("#[payload]") Object source, 
                            @Default("") String encoding,
                            @Default("true") boolean validateInput,
                            @Default("true") boolean validateOutput,
                            MuleEvent event) throws Exception {

        
         try {
             
            Transform t = getTransform(transform, event);
             
            ComplexDataObject src = null;
            if(source instanceof ComplexDataObject) {
                src = (ComplexDataObject) source;
            } else {
                C24Reader<? extends ComplexDataObject> reader = C24.parse(t.getInput(0).getType().getValidObjectClass());
                if(encoding != null && encoding.length() > 0) {
                    reader.using(encoding);
                }
                
                if(source instanceof Reader) {
                    src = reader.from((Reader)source);
                } else if(source instanceof InputStream) {
                    src = reader.from((InputStream)source);            
                } else if(source instanceof String) {
                    src = reader.from((String)source);            
                } else {
                    throw new C24Exception(CoreMessages.createStaticMessage("Can't instantiate reader for unknown payload type: " + source.getClass()), event);
                }
            }
            
            if(validateInput) {
                C24.validate(src);
            }
            
            ComplexDataObject result = C24.transform(src, t);
            
            if(validateOutput) {
                C24.validate(result);
            }
            
            StringWriter writer = new StringWriter();
            C24.write(result).to(writer);
            
            return writer.toString();
            
        } catch(ValidationException vEx) {
            throw new C24Exception(CoreMessages.createStaticMessage("Message is invalid"), event, vEx);
        } catch (IOException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Message is invalid"), event, e);
        }
    }
    
    private volatile Reflections reflections = null;
    
    private Reflections getReflections() {
        debug("getting reflections");
        if(reflections == null) {
            synchronized(this) {
                if(reflections == null) {
                    debug("reflections is null");
                    // See if we can use pre-built metadata
                    Reflections r = Reflections.collect();
                    debug("tried to pick up collected metadata");
                    if(r == null) {
                        debug("got none");
                        // No. Scan the classloader instead.
                        ConfigurationBuilder builder = new ConfigurationBuilder();
                        if(getBasePackage() != null) {
                            builder.filterInputsBy(new FilterBuilder().include(getBasePackage()));
                        }
                        debug("build config");
                        builder.setUrls(ClasspathHelper.forClassLoader())
                               .setScanners(new SubTypesScanner());
                        
                        r = new Reflections(builder);
                        debug("created reflections");
                        reflections = r;
                    }
                }
            }
        }
        
        return reflections;
        
    }
    
    @Override
    public Result<List<MetaDataKey>> getMetaDataKeys() {
        try {
            debug("Entering getMetaDataKeys");
            List<MetaDataKey> keys = new ArrayList<MetaDataKey>();
            Collection<Class<? extends Transform>> classes = getReflections().getSubTypesOf(Transform.class);
            debug("Creating metadata keys");
            for (Class<?> c : classes) {
                keys.add(new DefaultMetaDataKey(c.getName(), c.getName()));
            }
            debug("done");

            return new DefaultResult<List<MetaDataKey>>(keys);
        } catch(Exception ex) {
            return new DefaultResult<List<MetaDataKey>>(null, Status.FAILURE, "Could not retrieve metadata keys: " + ex.getMessage());
        }
        
    }
    
    @Override
    public Result<MetaData> getMetaData(MetaDataKey key) {
        try {
            debug("Entering getMetaData");
            debug("Getting class for " + key.getId());
            Class<?> object = Class.forName(key.getId());
            debug("got class");
            PojoMetaDataBuilder<?> metaDataBuilder = new DefaultMetaDataBuilder().createPojo(object);
            debug("Constructing and returning");
            return new DefaultResult<MetaData>(new DefaultMetaData(metaDataBuilder.build()));
        } catch(Exception ex) {
            return new DefaultResult<MetaData>(null, Status.FAILURE, "Could not retrieve metadata for key " + key.getId() + ": " + ex.getMessage());
        }
    }
}
