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

import static org.junit.Assert.*;
import org.junit.Test;

import biz.c24.io.gettingstarted.customer.Customer;
import org.junit.experimental.categories.Category;
import org.mule.modules.c24.automation.C24ConnectorTestParent;
import org.mule.modules.c24.automation.RegressionTests;

public class MarshalTestCases extends C24ConnectorTestParent {

    @Category({RegressionTests.class})
    @Test
    public void testDefaultMarshal() throws Exception {
        String output = runFlowAndGetPayload("marshalFlow", "customerTestData");
        String expected = getBeanFromContext("marshalledPayload");
        assertEquals(expected, output);
    }

    @Category({RegressionTests.class})
    @Test
    public void testFormatMarshal() throws Exception {
        String output = runFlowAndGetPayload("jsonMarshalFlow", "customerTestData");
        String expected = getBeanFromContext("marshalledJsonPayload");
        assertEquals(expected.replace("\t", ""), output.replace("\t", ""));

    }

}
