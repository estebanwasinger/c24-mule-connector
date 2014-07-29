/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */

package com.mulesoft.c24;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.mule.construct.Flow;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.util.FileUtils;

public class C24ConnectorTest extends FunctionalTestCase
{
	@Override
    protected String getConfigResources()
    {
        return "democ24app.xml";
    }

    @Test
    public void testFlow() throws Exception
    {

        String payload = FileUtils.readFileToString(new File("src/test/resources/Customers.xml"));
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        runFlowAndExpect("democ24appFlow1", expected, payload);
        assertTrue(true);
    }

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
        assertEquals(expect, this.runFlow(flowName, payload));
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
