/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.spring.integration.ws;

import org.junit.Test;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.internal.command.CommandFactory;
import org.kie.spring.beans.Person;
import org.kie.spring.integration.helper.KieServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * System tests ensuring the Spring WS MessageDispatcherServlet is correctly
 * set up and configured to delegate incoming requests to our ws:inbound-gateway.
 *
 * Use 'mvn package' to create a war file for this project, then deploy before
 * attempting to run this test.
 */
public class InContainerTests {

    private static final Logger logger               = LoggerFactory.getLogger(InContainerTests.class);
	private static final String WS_URI = "http://localhost:8080/kie-inbound-gateway-6.0.1.Final/kieservice";
	private final WebServiceTemplate template = new WebServiceTemplate();

	@Test
	public void testWebServiceRequestAndResponse() throws Exception{
		StringResult result = new StringResult();

        String xml = new KieServerHelper().toXML(getBatchExecutionCommand());

		Source payload = new StringSource(xml);

		template.sendSourceAndReceiveToResult(WS_URI, payload, result);
		logger.info("RESULT: " + result.toString());
        assertNotNull(result);
		assertThat(result.toString(), containsString("result identifier=\"vkiran\""));
        //Person.setHappy should have been set to TRUE by the  rules
        assertThat(result.toString(), containsString("<happy>true</happy>"));
	}

    private BatchExecutionCommand getBatchExecutionCommand() {
        Person p = new Person();
        p.setName("HAL");
        p.setHappy(false);
        Command insertPersonCmd = CommandFactory.newInsert(p, "vkiran");
        Command fireAllRulesCmd = CommandFactory.newFireAllRules();
        List<Command> commands = new ArrayList<Command>();
        commands.add(insertPersonCmd);
        commands.add(fireAllRulesCmd);

        return CommandFactory.newBatchExecution(commands, "ksession1");
    }

}
