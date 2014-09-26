package biz.c24.io.mule;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import org.junit.Test;
import org.mule.util.FileUtils;

import biz.c24.io.gettingstarted.customer.CustomersFile;

public class ParseTest extends C24ConnectorTestCase {
    
    @Test
    public void testParse() throws Exception {
        String payload = FileUtils.readFileToString(new File("src/test/resources/Customers.xml"));
        Object obj = runFlow("customersFileParseFlow", payload).getMessage().getPayload();
        
        assertTrue(obj instanceof CustomersFile);
        
        CustomersFile cf = (CustomersFile)obj;
        
        assertThat(cf.getCustomer().length, is(4));
    }

}
