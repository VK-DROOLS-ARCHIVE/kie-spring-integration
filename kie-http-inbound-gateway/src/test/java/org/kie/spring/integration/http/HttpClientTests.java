/*
 * Copyright 2002-2011 the original author or authors.
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
package org.kie.spring.integration.http;


import com.thoughtworks.xstream.XStream;
import org.drools.core.runtime.help.impl.XStreamHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.internal.command.CommandFactory;
import org.kie.spring.beans.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@ContextConfiguration("/META-INF/spring/integration/http-outbound-config.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class HttpClientTests {
	
	private static Logger logger = LoggerFactory.getLogger(HttpClientTests.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RequestGateway requestGateway;

    @Test
    public void testSendAndReceive() throws Exception {
        XStream xStream = new XStream();
        XStreamHelper.setAliases(xStream);
        String inputXml = xStream.toXML(getBatchExecutionCommand());

		String result = requestGateway.execute(inputXml);

        assertNotNull(result);
        assertThat(result, containsString("result identifier=\"vkiran\""));
        //Person.setHappy should have been set to TRUE by the  rules
        assertThat(result, containsString("<happy>true</happy>"));
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
