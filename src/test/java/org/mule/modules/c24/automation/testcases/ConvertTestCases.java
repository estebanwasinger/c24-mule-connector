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

package org.mule.modules.c24.automation.testcases;

import biz.c24.io.mule.C24Exception;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.modules.c24.automation.C24ConnectorTestParent;
import org.mule.modules.c24.automation.RegressionTests;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;


public class ConvertTestCases extends C24ConnectorTestParent
{

    @Category({RegressionTests.class})
    @Test
    public void testFlow() throws Exception
    {
        String expected = getBeanFromContext("outputPayload");
        assertEquals(expected, this.runFlowAndGetPayload("contactListConvertFlow", "inputPayload"));
    }

    @Category({RegressionTests.class})
    @Test
    public void testInputStreamFlow() throws Exception
    {
        InputStream payload = getClass().getClassLoader().getResourceAsStream("testdata/Customers.xml");
        String expected = getBeanFromContext("outputPayload");
        runFlowAndExpect("contactListConvertFlow", expected, payload);
    }

    @Category({RegressionTests.class})
    @Test (expected = C24Exception.class)
    public void testInvalidFlow() throws Exception {
        this.runFlowAndGetPayload("contactListConvertFlow", "invalidPayload");
    }

    @Category({RegressionTests.class})
    @Test
    public void testInvalidFlowNonValidating() throws Exception
    {
        String expected = getBeanFromContext("outputPayload");
        assertEquals(expected, this.runFlowAndGetPayload("contactListNonValidatingConvertFlow", "invalidPayload"));

    }

    @Category({RegressionTests.class})
    @Test(expected = C24Exception.class)
    public void testIllegalFlow() throws Exception
    {
        this.runFlowAndGetPayload("contactListConvertFlow", "illegalPayload");
    }

}
