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
import static org.hamcrest.CoreMatchers.*;

import biz.c24.io.mule.C24Exception;
import org.junit.Test;

import biz.c24.io.gettingstarted.customer.Customer;
import org.junit.experimental.categories.Category;
import org.mule.modules.c24.automation.C24ConnectorTestParent;
import org.mule.modules.c24.automation.RegressionTests;
import org.mule.modules.tests.ConnectorTestUtils;

public class ValidateTestCases extends C24ConnectorTestParent {

    @Category({RegressionTests.class})
    @Test
    public void testValid() {
        try {
            Customer customer = getBeanFromContext("customerTestData");
            Customer result = runFlowAndGetPayload("validateFlow", "customerTestData");
            assertEquals(customer.getName(), result.getName());
        } catch (Exception e) {
            fail(ConnectorTestUtils.getStackTrace(e));
        }
    }

    @Category({RegressionTests.class})
    @Test (expected = C24Exception.class)
    public void testInvalid() throws Exception {
            runFlowAndGetPayload("validateFlow", "invalidPayload");
    }


}
