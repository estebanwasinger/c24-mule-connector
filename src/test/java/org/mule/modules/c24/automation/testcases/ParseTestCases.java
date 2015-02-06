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

import biz.c24.io.gettingstarted.customer.CustomersFile;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.modules.c24.automation.C24ConnectorTestParent;
import org.mule.modules.c24.automation.RegressionTests;
import org.mule.modules.tests.ConnectorTestUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ParseTestCases extends C24ConnectorTestParent {

    @Category({RegressionTests.class})
    @Test
    public void testParse() throws Exception {
        try {
            Object obj = runFlowAndGetPayload("parse", "inputPayload");
            assertTrue(obj instanceof CustomersFile);
            CustomersFile cf = (CustomersFile)obj;
            CustomersFile testFixture = getCustomerTestFixture();
            assertThat(cf.getCustomer().length, is(testFixture.getCustomer().length));
        } catch (Exception e) {
            fail(ConnectorTestUtils.getStackTrace(e));
        }
    }

}
