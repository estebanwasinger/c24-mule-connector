/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */

package biz.c24.io.mule;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.construct.Flow;
import org.mule.modules.tests.ConnectorTestCase;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.util.FileUtils;


public class C24ConnectorFlowTest extends ConnectorTestCase
{
    
    // =============================
    // Backporting from DevKit 3.5.0
    // =============================
    
    
    protected <T> MuleEvent runFlow(String flowName) throws Exception
    {
        return runFlow(flowName, null);
    }
    
    protected <T> MuleEvent runFlow(String flowName, T payload) throws Exception
    {
        Flow flow = lookupFlowConstruct(flowName);
        return flow.process(getTestEvent(payload));
    }

    
    // =================================
    // End Backporting from DevKit 3.5.0
    // =================================
    
    
	@Override
    protected String getConfigResources()
    {
        return "automation-test-flows.xml";
    }

    @Test
    public void testFlow() throws Exception
    {
        String payload = FileUtils.readFileToString(new File("src/test/resources/Customers.xml"));
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        runFlowAndExpect("contactListTransformFlow", expected, payload);
    }
    
    @Test
    public void testInputStreamFlow() throws Exception
    {
        InputStream payload = getClass().getClassLoader().getResourceAsStream("Customers.xml");
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        runFlowAndExpect("contactListTransformFlow", expected, payload);
    }
    
    @Test
    public void testInvalidFlow() throws Exception
    {
        String payload = FileUtils.readFileToString(new File("src/test/resources/InvalidCustomers.xml"));
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        try {
            runFlowAndExpect("contactListTransformFlow", expected, payload);
            fail("Invalid flow not detected");
        } catch(C24Exception ex) {
            // Expected behaviour
        }
    }   
    
    @Test
    public void testInvalidFlowNonValidating() throws Exception
    {
        String payload = FileUtils.readFileToString(new File("src/test/resources/InvalidCustomers.xml"));
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));

        runFlowAndExpect("contactListNonValidatingTransformFlow", expected, payload);

    }    
    
    @Test
    public void testIllegalFlow() throws Exception
    {
        String payload = FileUtils.readFileToString(new File("src/test/resources/IllegalCustomers.xml"));
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        try {
            runFlowAndExpect("contactListTransformFlow", expected, payload);
            fail("Illegal flow not detected");
        } catch(C24Exception ex) {
            // Expected behaviour
        }
    } 
    
    
    //contactListNonValidatingTransformFlow

    /**
    * Run the flow specified by name and assert equality on the expected output
    *
    * @param flowName The name of the flow to run
    * @param expect The expected output
    */
    protected <T> void runFlowAndExpect(String flowName, T expect) throws Exception
    {
        assertEquals(expect, this.runFlow(flowName));
    }
    
    /**
     * Run the flow specified by name using the specified payload and assert
     * equality on the expected output
     *
     * @param flowName The name of the flow to run
     * @param expect The expected output
     * @param payload The payload of the input event
     */
    protected <T, U> void runFlowAndExpect(String flowName, T expect, U payload) throws Exception
    {
        assertEquals(expect, this.runFlow(flowName, payload).getMessage().getPayload());
    }
    

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name)
    {
        return (Flow) muleContext.getRegistry().lookupFlowConstruct(name);
    }

}
