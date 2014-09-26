package biz.c24.io.mule;

import static org.junit.Assert.assertEquals;

import org.mule.api.MuleEvent;
import org.mule.construct.Flow;
import org.mule.modules.tests.ConnectorTestCase;

public abstract class C24ConnectorTestCase extends ConnectorTestCase {
    
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

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name)
    {
        return (Flow) muleContext.getRegistry().lookupFlowConstruct(name);
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
        assertEquals(expect, this.runFlow(flowName, payload).getMessage().getPayload());
    }
    

}
