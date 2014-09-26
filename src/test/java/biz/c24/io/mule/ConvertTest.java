/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */

package biz.c24.io.mule;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.mule.util.FileUtils;


public class ConvertTest extends C24ConnectorTestCase
{
    

    @Test
    public void testFlow() throws Exception
    {
        String payload = FileUtils.readFileToString(new File("src/test/resources/Customers.xml"));
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        runFlowAndExpect("contactListConvertFlow", expected, payload);
    }
    
    @Test
    public void testInputStreamFlow() throws Exception
    {
        InputStream payload = getClass().getClassLoader().getResourceAsStream("Customers.xml");
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        runFlowAndExpect("contactListConvertFlow", expected, payload);
    }
    
    @Test
    public void testInvalidFlow() throws Exception
    {
        String payload = FileUtils.readFileToString(new File("src/test/resources/InvalidCustomers.xml"));
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        try {
            runFlowAndExpect("contactListConvertFlow", expected, payload);
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

        runFlowAndExpect("contactListNonValidatingConvertFlow", expected, payload);

    }    
    
    @Test
    public void testIllegalFlow() throws Exception
    {
        String payload = FileUtils.readFileToString(new File("src/test/resources/IllegalCustomers.xml"));
        String expected = FileUtils.readFileToString(new File("src/test/resources/Customers.txt"));
        try {
            runFlowAndExpect("contactListConvertFlow", expected, payload);
            fail("Illegal flow not detected");
        } catch(C24Exception ex) {
            // Expected behaviour
        }
    } 
    
    
    //contactListNonValidatingTransformFlow


}
