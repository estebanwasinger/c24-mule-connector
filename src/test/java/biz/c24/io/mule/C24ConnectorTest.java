/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */

package biz.c24.io.mule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.mule.common.Result;
import org.mule.common.Result.Status;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.datatype.DataType;

import biz.c24.io.api.transform.Transform;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class C24ConnectorTest {

    private C24Connector connector = new C24Connector();
    
    private List<MetaDataKey> getMetaDataKeys() {
        Result<List<MetaDataKey>> result = connector.getMetaDataKeys();
        assertThat(result, is(not(nullValue())));
        assertTrue(result.getStatus().compareTo(Status.SUCCESS) == 0);
        return connector.getMetaDataKeys().get();
    }
    
    private MetaData getMetaData(MetaDataKey key) {
        Result<MetaData> result = connector.getMetaData(key);
        assertThat(result, is(not(nullValue())));
        assertTrue(result.getStatus().compareTo(Status.SUCCESS) == 0);
        return result.get();
    }
    
    private void testDataSense() throws ClassNotFoundException {
        
        List<MetaDataKey> result = getMetaDataKeys();
        assertThat(result, is(not(nullValue())));
        
        boolean foundTestTransform = false;
        
        for(MetaDataKey key : result) {
            
            assertTrue(Transform.class.isAssignableFrom(Class.forName(key.getId())));
            
            if(connector.getBasePackage() != null) {
                if(connector.getBasePackage().contains("*")) {
                    assertTrue(key.getId().matches(connector.getBasePackage()));
                } else {
                    assertTrue(key.getId().startsWith(connector.getBasePackage()));
                }
            }
            
            if(key.getDisplayName().equals("GenerateContactListTransform")) {
                foundTestTransform = true;
                assertThat(key.getId(), is("biz.c24.io.gettingstarted.transform.GenerateContactListTransform"));
                
                MetaData metaData = getMetaData(key);
                assertThat(metaData, is(not(nullValue())));
                assertThat(metaData.getPayload().getDataType(), is(DataType.POJO));
            }
        }
        
        assertThat(foundTestTransform, is(true));
        
    }
    
    @Test
    public void testUnconstrainedDataSense() throws ClassNotFoundException {
        connector.setBasePackage(null);
        testDataSense();
    }
    
    @Test
    public void testConstrainedMatchingDataSense() throws ClassNotFoundException {
        connector.setBasePackage("biz.c24.io.*");
        testDataSense();
    }

    
    @Test
    public void testConstrainedNonMatchingDataSense() {
        connector.setBasePackage("nonexistent.test.*");
        
        List<MetaDataKey> result = getMetaDataKeys();
        //assertTrue(result.getStatus().compareTo(Status.SUCCESS) == 0);
        assertThat(result, is(not(nullValue())));
        assertThat(result.size(), is(0));

    }
    
    
    private String readFile(String filename) throws IOException {
        InputStream input = C24ConnectorTest.class.getClassLoader().getResourceAsStream(filename);
        byte[] bytes = new byte[input.available()];
        input.read(bytes, 0, bytes.length);
        return new String(bytes);
    }
    
    @Test
    public void testConnector() throws Exception {
        C24Connector connector = new C24Connector();
        InputStream input = C24ConnectorTest.class.getClassLoader().getResourceAsStream("Customers.xml");

        String output = connector.convert("biz.c24.io.gettingstarted.transform.GenerateContactListTransform", input, null, true, true, null);
        
        assertThat(output, is(readFile("Customers.txt")));
    }
    
}
