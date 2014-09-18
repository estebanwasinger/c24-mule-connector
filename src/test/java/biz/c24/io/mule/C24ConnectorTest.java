/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */

package biz.c24.io.mule;

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
    
    private void testDataSense() throws ClassNotFoundException {
        
        Result<List<MetaDataKey>> result = connector.getMetaDataKeys();
        assertTrue(result.getStatus().compareTo(Status.SUCCESS) == 0);
        assertThat(result.get(), is(not(nullValue())));
        
        boolean foundTestTransform = false;
        
        for(MetaDataKey key : result.get()) {
            
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
                
                MetaData metaData = connector.getMetaData(key).get();
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
        
        Result<List<MetaDataKey>> result = connector.getMetaDataKeys();
        assertTrue(result.getStatus().compareTo(Status.SUCCESS) == 0);
        assertThat(result.get(), is(not(nullValue())));
        assertThat(result.get().size(), is(0));

    }
    
  /*  
    @Test
    public void testConnector() {
        C24Connector connector = new C24Connector();
  
    }
   */ 
}
