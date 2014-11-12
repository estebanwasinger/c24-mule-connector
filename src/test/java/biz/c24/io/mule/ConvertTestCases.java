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

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.mule.util.FileUtils;


public class ConvertTestCases extends C24ConnectorTestCase
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
