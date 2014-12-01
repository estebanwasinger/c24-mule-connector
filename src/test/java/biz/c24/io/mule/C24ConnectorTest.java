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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.mule.common.Result;
import org.mule.common.Result.Status;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.datatype.DataType;

import biz.c24.io.api.C24;
import biz.c24.io.api.transform.Transform;
import biz.c24.io.gettingstarted.customer.CustomersFile;
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
    
    @Test
    public void testAdvancedTransform() throws IOException, C24Exception {
        C24Connector connector = new C24Connector();
        CustomersFile input = C24.parse(CustomersFile.class).from(new File("/Customers.xml"));
        
        List<Object> params = new LinkedList<Object>();
        params.add(input);
        params.add("http://marketing.c24.biz/landing.html");
        params.add(12345);

        List<List<Object>> output = connector.transformAdvanced("biz.c24.io.gettingstarted.transform.GenerateMarketingLinksTransform", params, null);
        
        assertThat(output.size(), is(2));
        
        List<Object> vals = output.get(0);
        assertThat(vals.size(), is(4));
        Object val = vals.get(0);
        assertTrue(val instanceof String);
        assertThat((String)val, is("http://marketing.c24.biz/landing.html?campaign=12345&customer=100018"));
        
        vals = output.get(1);
        assertThat(vals.size(), is(3));
        val = vals.get(0);
        assertTrue(val instanceof String);
        assertThat((String)val, is("oliver.twist@c24.biz"));        
       
    }
    
}
