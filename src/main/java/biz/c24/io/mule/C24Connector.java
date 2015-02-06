/*
 * Copyright C24 Technologies Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.c24.io.mule;

import biz.c24.api.LicenseException;
import biz.c24.io.api.C24;
import biz.c24.io.api.C24.C24Reader;
import biz.c24.io.api.C24.C24Writer;
import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.SimpleDataObject;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.transform.Transform;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.api.annotations.*;
import org.mule.api.annotations.display.Summary;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.common.DefaultResult;
import org.mule.common.Result;
import org.mule.common.metadata.*;
import org.mule.config.i18n.CoreMessages;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;


import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * C24 Mule Connector
 * 
 * @author C24 Technologies Limited
 * 
 * The C24 Connector allows you to use C24 iO message parsing, validation, transformation and generation
 * in your Mule flows.
 *
 */
@Connector(name = "c24", schemaVersion = "1.1.0", friendlyName = "C24-iO Connector", minMuleVersion="3.4",
            description = "The C24 Connector allows you to use C24 iO message parsing, validation, transformation and generation in your Mule flows")
public class C24Connector {


    /**
     * The C24 iO licence to use (required for licensed data models only)
     */
    @Optional
    @Configurable
    @Summary("The path to the licence file to use. Not required for C24-iO Open Edition or if you are only using unlicensed models.")
    private String licenceFile;
    
    private static final transient Log LOG = LogFactory.getLog(C24Connector.class);

    /**
     * C24-iO Message Parsing
     * <p/>
     * {@sample.xml ../../../doc/C24-connector.xml.sample c24:parse}
     *
     * @param type The fully-qualified classname of the type that we expect to parse. The returned object will be an instance of this type.
     * @param source The String, Reader or InputStream to parse into a C24 Object
     * @param encoding The encoding of the input data
     * @param format The format to read (defaults to the model's default input format).
     * @param event The Mule event
     * @return A Java object representing the parsed form of the source
     * @throws C24Exception if the message cannot be parsed
     */
    @Processor
    @Inject
    public ComplexDataObject parse(String type,
                                   @Optional @Default("#[payload]") Object source,
                                   @Optional String encoding,
                                   @Optional C24.Format format,
                                   MuleEvent event) throws C24Exception {

        try {
            Class<? extends ComplexDataObject> typeClass = (Class<? extends ComplexDataObject>) Class.forName(type);

            return parse(typeClass, source, encoding, format, event);

        } catch (ClassNotFoundException e) {
            throw new C24Exception(CoreMessages.createStaticMessage(type + " cannot be found."), event, e);
        }

    }

    /**
     * C24-iO validation.
     * Validates the message, either returning the message or throwing an exception if it is invalid.
     * <p/>
     * {@sample.xml ../../../doc/C24-connector.xml.sample c24:validate}
     *
     * @param source The parsed message to validate
     * @param event The Mule event
     * @return The message
     * @throws C24Exception if the message is invalid.
     */
    @Processor
    @Inject
    public ComplexDataObject validate(@Optional @Default("#[payload]") Object source,
                                      MuleEvent event) throws C24Exception {
        try {
            if(source instanceof ComplexDataObject) {
                ComplexDataObject cdo = (ComplexDataObject) source;
                C24.validate(cdo);
                return cdo;
            } else {
                throw new C24Exception(CoreMessages.createStaticMessage("Message supplied to validate was not a C24-iO object (type " + source.getClass().getName() + " does not extend ComplexDataObject)."), event);
            }
        } catch (ValidationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Message is invalid."), event, e);
        }

    }


    /**
     * C24-iO Transform.
     * Transforms an object between models using a C24-iO 1:1 transform
     * <p/>
     * {@sample.xml ../../../doc/C24-connector.xml.sample c24:transform}
     *
     * @param transform The Transform to use
     * @param source The source data to be transformed
     * @param event The Mule event
     * @return The output from the transform
     * @throws C24Exception if the message cannot be transformed
     */
    @Processor
    @Inject
    public Object transform(String transform,
                            @Optional @Default("#[payload]") Object source,
                            MuleEvent event) throws C24Exception {

        Transform xform = getTransform(transform, event);

        return transform(xform, source, event);
    }

    /**
     * C24-iO Transform n:m
     * Transforms an object between models using a C24-iO transform
     * <p/>
     * {@sample.xml ../../../doc/C24-connector.xml.sample c24:transform}
     *
     * @param transform The Transform to use
     * @param inputs The inputs to the C24-iO transform.
     * @param event The Mule event
     * @return The outputs from the transform
     * @throws C24Exception if the message cannot be transformed
     */
    @Processor
    @Inject
    public List<List<Object>> transformAdvanced(String transform,
                                                List<Object> inputs,
                                                MuleEvent event) throws C24Exception {

        try {
            Transform xform = getTransform(transform, event);

            int numInputs = xform.getInputCount();
            if(numInputs != inputs.size()) {
                throw new C24Exception(CoreMessages.createStaticMessage("Transform " + transform + " requires " + numInputs + " inputs, but " + inputs.size() + " were supplied"), event);
            }

            Object[][] typedInputs = new Object[numInputs][];
            for(int i=0; i < numInputs; i++) {

                Object curr = inputs.get(i);
                typedInputs[i] = new Object[]{curr};
            }

            return toList(xform.transform(typedInputs));

        } catch (ValidationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Message is invalid."), event, e);
        }

    }


    /**
     * C24-iO marshal
     * Converts a bound Java message to a wire format.
     * <p/>
     * {@sample.xml ../../../doc/C24-connector.xml.sample c24:marshal}
     *
     * @param source The Java message
     * @param format The format to generate (defaults to the model's default output format).
     * @param encoding The character encoding to use (defaults to the model's default encoding)
     * @param event The Mule Event
     * @return A String containing the marshalled form of the source
     * @throws C24Exception if the message cannot be marshalled
     */
    @Processor
    @Inject
    public String marshal(@Optional @Default("#[payload]") Object source,
                          @Optional C24.Format format,
                          @Optional String encoding,
                          MuleEvent event) throws C24Exception {
        C24Writer<? extends ComplexDataObject> writer;

        try {
            if(source instanceof ComplexDataObject) {
                writer = C24.write((ComplexDataObject)source);
            } else if(source instanceof SimpleDataObject) {
                writer = C24.write((SimpleDataObject)source);
            } else {
                throw new C24Exception(CoreMessages.createStaticMessage("Message supplied to marshal was not a C24-iO object (type " + source.getClass().getName() + " does not extend ComplexDataObject or SimpleDataObject)."), event);
            }

            if(format != null) {
                writer.as(format);
            }
            if(encoding != null) {
                writer.using(encoding);
            }

            StringWriter str = new StringWriter();
            writer.to(str);
            return str.toString();

        } catch (IOException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to marshal message."), event, e);
        }


    }


    /**
     * c24 Converter.
     * A convenience method to parse, validate, transform and marshal a message in a single operation.
     * <p/>
     * {@sample.xml ../../../doc/C24-connector.xml.sample c24:convert}
     *
     * @param transform The Transform to use
     * @param source The String, Reader or InputStream to parse into a C24 Object
     * @param encoding The encoding of the input data
     * @param validateInput Whether the source message should be validated post-parsing
     * @param validateOutput Whether the result of the transformation should be checked for validity
     * @param event The Mule event
     * @return The marshaled result of the transformation
     * @throws C24Exception An exception when there's an error
     */
    @Processor
    @Inject
    public String convert(/* @MetaDataKeyParam */ String transform,
                          @Optional @Default("#[payload]") Object source,
                          @Optional @Default("") String encoding,
                          @Optional @Default("true") boolean validateInput,
                          @Optional @Default("true") boolean validateOutput,
                          MuleEvent event) throws C24Exception {


        try {

            Transform t = getTransform(transform, event);

            if(t.getInputCount() != 1 || t.getOutputCount() != 1) {
                throw new C24Exception(CoreMessages.createStaticMessage("The Convert operation only supports 1:1 transformations. Please use the Transform Advanced operation for n:m transforms."), event);
            }

            ComplexDataObject src = null;
            if(source instanceof ComplexDataObject) {
                src = (ComplexDataObject) source;
            } else {
                src = parse(t.getInput(0).getType().getValidObjectClass(), source, encoding, null, event);
            }

            if(validateInput) {
                C24.validate(src);
            }

            ComplexDataObject result = (ComplexDataObject) transform(t, src, event);

            if(validateOutput) {
                C24.validate(result);
            }

            return marshal(result, null, null, event);

        } catch(ValidationException vEx) {
            throw new C24Exception(CoreMessages.createStaticMessage("Message is invalid"), event, vEx);
        }
    }

    protected void debug(String str) {
        LOG.debug(str);
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
    
    
    /**
     * Returns an instance of the specified transform
     * @param transformName
     * @param event
     * @return
     * @throws C24Exception
     */
    private Transform getTransform(String transformName, MuleEvent event) throws C24Exception {

        try {
            Class<?> transformClass = Class.forName(transformName);
         
            // Check we have a valid transformation
            if(!Transform.class.isAssignableFrom(transformClass)) {
                throw new C24Exception(CoreMessages.createStaticMessage(transformClass.getName() + " is not a valid C24 Transform"), event);
            }
            
            return (Transform) transformClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform"), event, e);
        } catch (InstantiationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform"), event, e);
        } catch (IllegalAccessException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform"), event, e);
        }
    }
    
    /**
     * Create a C24Reader capable of parsing the specified type using the (optional) encoding and format
     * @param type
     * @param encoding
     * @param format
     * @return
     */
    private C24Reader<ComplexDataObject> getReader(Class<? extends ComplexDataObject> type,
                                                             String encoding,
                                                             C24.Format format) {
        
        C24Reader<ComplexDataObject> reader = C24.parse(type);
        
        if(encoding != null && encoding.length() > 0) {
            reader.using(encoding);
        }
        if(format != null) {
            reader.as(format);
        }
        
        return reader;
        
    }
    
    /**
     * Parse an instance of the specified type from source
     * @param type
     * @param source
     * @param encoding
     * @param format
     * @param event
     * @return
     * @throws C24Exception
     */
    private ComplexDataObject parse(Class<? extends ComplexDataObject> type,
                                    Object source,
                                    String encoding,
                                    C24.Format format,
                                    MuleEvent event) throws C24Exception {
        

        if(!ComplexDataObject.class.isAssignableFrom(type)) {
            throw new C24Exception(CoreMessages.createStaticMessage(type + " is not a valid C24-iO type. It must extend ComplexDataObject."), event);
        }
        
        C24Reader<? extends ComplexDataObject> reader = getReader(type, encoding, format);
                   
        try {
            
            ComplexDataObject result;
            
            if(source instanceof Reader) {
                result = reader.from((Reader)source);
                ((Reader) source).close();
            } else if(source instanceof InputStream) {
                result = reader.from((InputStream)source);
                ((InputStream) source).close();
            } else if(source instanceof String) {
                result = reader.from((String)source);            
            } else {
                throw new C24Exception(CoreMessages.createStaticMessage("Can't instantiate reader for unknown payload type: " + source.getClass()), event);
            }    
            
            return result;
            
        } catch (IOException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to parse message."), event, e);
        } finally {
            closeQuietly(source);
        }
    }

    private void closeQuietly(Object source) {
        try {
            if(source instanceof Reader) {
                ((Reader)source).close();
            } else if(source instanceof InputStream) {
                ((InputStream)source).close();
            }
        } catch (IOException e) {
            //ignore as an earlier exception takes precedence
        }
    }
    
    /**
     * Invoke a 1:1 C24-iO transform and return the result
     * @param xform
     * @param source
     * @param event
     * @return
     * @throws C24Exception
     */
    private Object transform(Transform xform,
                             Object source,
                             MuleEvent event) throws C24Exception {
        
        try {
            if(xform.getInputCount() != 1 || xform.getOutputCount() != 1) {
                throw new C24Exception(CoreMessages.createStaticMessage("The Transform operation only supports 1:1 transformations. Please use the Transform Advanced operation for n:m transforms."), event);
            }
            
            return C24.transform(source, xform);
        } catch (ValidationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Message is invalid."), event, e);
        }
    }

    
    /**
     * Convert the output of a C24-iO transform to List form
     * @param array
     * @return
     */
    private static List<List<Object>> toList(Object[][] array) {
        
        List<List<Object>> ret = null;
        
        if(array != null) {
            ret = new ArrayList<List<Object>>(array.length);
            
            for(int i=0; i < array.length; i++) {
                ret.add(Arrays.asList(array[i]));
            }
        }
        
        return ret;
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
                        
                        Collection<URL> urls = ClasspathHelper.forJavaClassPath();
                        urls.addAll(ClasspathHelper.forClassLoader());
                        
                        builder.setUrls(urls)
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
    
    
    /**
     * DataSense implementation to get the metadata keys
     * @return The metadata keys
     */
    public Result<List<MetaDataKey>> getMetaDataKeys() {
        try {
            debug("Entering getMetaDataKeys");
            List<MetaDataKey> keys = new ArrayList<MetaDataKey>();
            Collection<Class<? extends Transform>> classes = getReflections().getSubTypesOf(Transform.class);
            debug("Creating metadata keys");
            for (Class<?> c : classes) {
                keys.add(new DefaultMetaDataKey(c.getName(), c.getName().replaceAll(".*\\.", "")));
            }
            debug("done");

            return new DefaultResult<List<MetaDataKey>>(keys);
        } catch(Exception ex) {
            LOG.debug(ex);
            return null;
        }
        
    }
    
    /**
     * DataSense implementation to get the metadata for the supplied key
     * @param key The metadata key
     * @return The metadata corresponding to key
     */
    public Result<MetaData> getMetaData(MetaDataKey key) {
        try {
            debug("Entering getMetaData");
            debug("Getting class for " + key.getId());
            Class<?> object = Class.forName(key.getId());
            debug("got class");
            MetaDataModel model = new DefaultPojoMetaDataModel(object);
            debug("Constructing and returning");

            return new DefaultResult<MetaData>(new DefaultMetaData(model));
        } catch(Exception ex) {
            LOG.debug(ex);
            return null;
        }
    }
    
    
}
