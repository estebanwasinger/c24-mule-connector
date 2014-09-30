/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */
package biz.c24.io.mule;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.MetaDataSwitch;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.Summary;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.annotations.param.Payload;
import org.mule.common.DefaultResult;
import org.mule.common.Result;
import org.mule.common.metadata.DefaultMetaData;
import org.mule.common.metadata.DefaultMetaDataKey;
import org.mule.common.metadata.DefaultPojoMetaDataModel;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.MetaDataModel;
import org.mule.config.i18n.CoreMessages;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import biz.c24.api.LicenseException;
import biz.c24.io.api.C24;
import biz.c24.io.api.ParserException;
import biz.c24.io.api.Utils;
import biz.c24.io.api.C24.C24Reader;
import biz.c24.io.api.C24.C24Writer;
import biz.c24.io.api.data.Base64BinaryDataType;
import biz.c24.io.api.data.BooleanDataType;
import biz.c24.io.api.data.ByteDataType;
import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.DataType;
import biz.c24.io.api.data.DecimalDataType;
import biz.c24.io.api.data.DoubleDataType;
import biz.c24.io.api.data.FloatDataType;
import biz.c24.io.api.data.GenericDateDataType;
import biz.c24.io.api.data.ISO8601DateDataType;
import biz.c24.io.api.data.ISO8601DateTimeDataType;
import biz.c24.io.api.data.ISO8601TimeDataType;
import biz.c24.io.api.data.IntDataType;
import biz.c24.io.api.data.IntegerDataType;
import biz.c24.io.api.data.LongDataType;
import biz.c24.io.api.data.NegativeIntegerDataType;
import biz.c24.io.api.data.NonNegativeIntegerDataType;
import biz.c24.io.api.data.NonPositiveIntegerDataType;
import biz.c24.io.api.data.NumberDataType;
import biz.c24.io.api.data.PositiveIntegerDataType;
import biz.c24.io.api.data.SQLBlob;
import biz.c24.io.api.data.SQLBlobDataType;
import biz.c24.io.api.data.SQLClob;
import biz.c24.io.api.data.SQLClobDataType;
import biz.c24.io.api.data.SQLDateDataType;
import biz.c24.io.api.data.SQLTimeDataType;
import biz.c24.io.api.data.SQLTimestampDataType;
import biz.c24.io.api.data.ShortDataType;
import biz.c24.io.api.data.SimpleDataObject;
import biz.c24.io.api.data.SimpleDataType;
import biz.c24.io.api.data.StringDataType;
import biz.c24.io.api.data.UnsignedByteDataType;
import biz.c24.io.api.data.UnsignedIntDataType;
import biz.c24.io.api.data.UnsignedLongDataType;
import biz.c24.io.api.data.UnsignedShortDataType;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.data.WhiteSpaceEnum;
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

@Connector(name = "c24", schemaVersion = "1.1.0", friendlyName = "C24-iO Connector", minMuleVersion="3.4", metaData = MetaDataSwitch.OFF)
//@Module(name = "c24", schemaVersion = "1.0.0", friendlyName = "C24 Connector", metaData = MetaDataSwitch.DYNAMIC)
//        namespace = "http://schema.c24.biz/mule", schemaLocation = "http://schema.c24.biz/mule.xsd")
public class C24Connector {
    
    /**
     * The C24 iO licence to use (required for licensed data models only)
     */
    @Optional
    @Configurable
    @Summary("The path to the licence file to use. Not required for C24-iO Open Edition or if you are only using unlicensed models.")
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
        
        return t;
    }
    
    
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
    // Mule doesn't support @Processor and generics
    //public <T extends ComplexDataObject> T parse(String type,
    public ComplexDataObject parse(String type,
                                   @Optional @Default("#[payload]") Object source,
                                   @Optional String encoding, 
                                   @Optional C24.Format format,
                                   MuleEvent event) throws C24Exception {
    
        try {
                Class<? extends ComplexDataObject> typeClass = (Class<? extends ComplexDataObject>) Class.forName(type);

            
            if(!ComplexDataObject.class.isAssignableFrom(typeClass)) {
                throw new C24Exception(CoreMessages.createStaticMessage(type + " is not a valid C24-iO type. It must extend ComplexDataObject."), event);
            }
            
            C24Reader<? extends ComplexDataObject> reader = C24.parse(typeClass);
            if(encoding != null && encoding.length() > 0) {
                reader.using(encoding);
            }
            if(format != null) {
                reader.as(format);
            }
            
            if(source instanceof Reader) {
                return reader.from((Reader)source);
            } else if(source instanceof InputStream) {
                return reader.from((InputStream)source);            
            } else if(source instanceof String) {
                return reader.from((String)source);            
            } else {
                throw new C24Exception(CoreMessages.createStaticMessage("Can't instantiate reader for unknown payload type: " + source.getClass()), event);
            }    
        } catch (ClassNotFoundException e) {
            throw new C24Exception(CoreMessages.createStaticMessage(type + " cannot be found."), event, e);
        } catch (IOException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to parse message."), event, e);
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

        try {
            Transform xform = getTransform(transform, event);
            
            
            if(xform.getInputCount() != 1 || xform.getOutputCount() != 1) {
                throw new C24Exception(CoreMessages.createStaticMessage("The Transform operation only supports 1:1 transformations. Please use the Transform Advanced operation for n:m transforms."), event);
            }
            
            return C24.transform(source, xform);
        } catch (ClassNotFoundException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
        } catch (InstantiationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
        } catch (IllegalAccessException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
        } catch (ValidationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Message is invalid."), event, e);
        }

    }

    
    /**
     * C24-iO Transform n:m
     * Transforms an object between models using a C24-iO transform
     * <p/>
     * {@sample.xml ../../../doc/C24-connector.xml.sample c24:transform}
     *
     * @param transform The Transform to use
     * @param source The source data to be transformed
     * @param inputs The inputs to the C24-iO transform. Use #[payload] to select the inbound payload.
     * @param output The index of the transform output to use as the outbound payload.
     * @param event The Mule event
     * @return The outputs from the transform
     * @throws C24Exception if the message cannot be transformed
     */
    @Processor
    @Inject
    public List<List<Object>> transformAdvanced(String transform,
                                       @Payload Object source,
                                       List<String> inputs,
                                       MuleEvent event) throws C24Exception {

        try {
            Transform xform = getTransform(transform, event);
            
            int numInputs = xform.getInputCount();
            if(numInputs != inputs.size()) {
                throw new C24Exception(CoreMessages.createStaticMessage("Transform " + transform + " requires " + numInputs + " inputs, but " + inputs.size() + " were supplied"), event);
            }
            
            Object[][] typedInputs = new Object[numInputs][];
            for(int i=0; i < numInputs; i++) {
                String curr = inputs.get(i);
            
                if(curr.equals("#[payload]")) {
                    typedInputs[i] = new Object[]{source};
                } else {
                    DataType type = xform.getInput(i).getType();
                    if(type instanceof SimpleDataType) {
                        typedInputs[i] = new Object[]{parseObject((SimpleDataType) type, curr)};
                    } else {
                        throw new C24Exception(CoreMessages.createStaticMessage("Transform input type " + type.getName() + " is only supported by the C24-iO Mule Connector when it is mapped to the message inbound payload."), event);
                    }
                }
            }
            
            Object[][] result = xform.transform(typedInputs);
            
            List<List<Object>> ret = new ArrayList<List<Object>>(result.length);
            
            if(result != null) {
                for(int i=0; i < result.length; i++) {
                    ret.add(Arrays.asList(result[i]));
                }
            }
            
            return ret;

        } catch (ClassNotFoundException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
        } catch (InstantiationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
        } catch (IllegalAccessException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
        } catch (ValidationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Message is invalid."), event, e);
        } catch (ParserException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to parse transform input."), event, e);
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
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to marshall message."), event, e);       
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
        } catch (ClassNotFoundException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
        } catch (InstantiationException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
        } catch (IllegalAccessException e) {
            throw new C24Exception(CoreMessages.createStaticMessage("Failed to instantiate transform."), event, e);
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
    
//    @MetaDataKeyRetriever
    //@Override
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
            return null;
        }
        
    }
    
//    @MetaDataRetriever
    //@Override
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
            return null;
        }
    }
    
    
    /**
     * TODO - This is cloned from LoaderUtils.parseObjectAsXML. Refactor!
     * @param t
     * @param str
     * @return
     * @throws ParserException
     */
    private static Object parseObject(SimpleDataType t, String str) throws ParserException {
        if (t instanceof NumberDataType) {
            if (str.startsWith("+")) {
                str = str.substring(1);
            }
            if (str.startsWith(".")) {
                str = "0" + str;
            }
        }
        if (!(t instanceof StringDataType)) {
            str = Utils.whitespace(str, WhiteSpaceEnum.COLLAPSE);
        }
        if (t instanceof GenericDateDataType) {
            GenericDateDataType date = (GenericDateDataType)t;
            if (date.isDate() && date.isTime()) {
                return ((ISO8601DateTimeDataType)ISO8601DateTimeDataType.getInstance()).parseDate(str);
            }
            if (date.isDate()) {
                return ((ISO8601DateDataType)ISO8601DateDataType.getInstance()).parseDate(str);
            }
            if (date.isTime()) {
                return ((ISO8601TimeDataType)ISO8601TimeDataType.getInstance()).parseDate(str);
            }
        }
        if (t instanceof SQLDateDataType) {
            return new java.sql.Date(
                    ((ISO8601DateDataType)ISO8601DateDataType.getInstance()).parseDate(str).getTime()
            );
        }
        if (t instanceof SQLTimeDataType) {
            return new java.sql.Time(((ISO8601TimeDataType)ISO8601TimeDataType.getInstance()).parseDate(str).getTime());
        }
        if (t instanceof SQLTimestampDataType) {
            return new java.sql.Timestamp(
                    ((ISO8601DateTimeDataType)ISO8601DateTimeDataType.getInstance()).parseDate(str).getTime()
            );
        }
        if (t instanceof SQLBlobDataType) {
            return new SQLBlob((byte[])((Base64BinaryDataType)Base64BinaryDataType.getInstance()).parseObject(str));
        }
        if (t instanceof SQLClobDataType) {
            return new SQLClob(str);
        }
        if (t instanceof ByteDataType && ((ByteDataType)t).isFormatUsed()) {
            return ((SimpleDataType)ByteDataType.getInstance()).parseObject(str);
        }
        if (t instanceof ShortDataType && ((ShortDataType)t).isFormatUsed()) {
            return ((SimpleDataType)ShortDataType.getInstance()).parseObject(str);
        }
        if (t instanceof IntDataType && ((IntDataType)t).isFormatUsed()) {
            return ((SimpleDataType)IntDataType.getInstance()).parseObject(str);
        }
        if (t instanceof LongDataType && ((LongDataType)t).isFormatUsed()) {
            return ((SimpleDataType)LongDataType.getInstance()).parseObject(str);
        }
        if (t instanceof UnsignedByteDataType && ((UnsignedByteDataType)t).isFormatUsed()) {
            return ((SimpleDataType)UnsignedByteDataType.getInstance()).parseObject(str);
        }
        if (t instanceof UnsignedShortDataType && ((UnsignedShortDataType)t).isFormatUsed()) {
            return ((SimpleDataType)UnsignedShortDataType.getInstance()).parseObject(str);
        }
        if (t instanceof UnsignedIntDataType && ((UnsignedIntDataType)t).isFormatUsed()) {
            return ((SimpleDataType)UnsignedIntDataType.getInstance()).parseObject(str);
        }
        if (t instanceof UnsignedLongDataType && ((UnsignedLongDataType)t).isFormatUsed()) {
            return ((SimpleDataType)UnsignedLongDataType.getInstance()).parseObject(str);
        }
        if (t instanceof PositiveIntegerDataType && ((PositiveIntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)PositiveIntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof NonNegativeIntegerDataType && ((NonNegativeIntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)NonNegativeIntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof NegativeIntegerDataType && ((NegativeIntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)NegativeIntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof NonPositiveIntegerDataType && ((NonPositiveIntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)NonPositiveIntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof IntegerDataType && ((IntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)IntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof DecimalDataType && ((DecimalDataType)t).isFormatUsed()) {
            return ((SimpleDataType)DecimalDataType.getInstance()).parseObject(str);
        }
        if (t instanceof FloatDataType && ((FloatDataType)t).isFormatUsed()) {
            return ((SimpleDataType)FloatDataType.getInstance()).parseObject(str);
        }
        if (t instanceof DoubleDataType && ((DoubleDataType)t).isFormatUsed()) {
            return ((SimpleDataType)DoubleDataType.getInstance()).parseObject(str);
        }
        if (t instanceof BooleanDataType) {
            if (str.equals("1")) {
                return Boolean.TRUE;
            }
            if (str.equals("0")) {
                return Boolean.FALSE;
            }
        }

        return t.parseObject(str);
    }

    
}
