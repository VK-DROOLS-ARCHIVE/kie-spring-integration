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
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.internal.command.CommandFactory;
import org.kie.spring.integration.helper.KieServerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.ws.SimpleWebServiceInboundGateway;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ws.context.MessageContext;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Out-of-container tests for ws:inbound-gateway message processing.
 */
@ContextConfiguration("/META-INF/spring/integration/kie-server.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class InboundGatewayTests {

	@Autowired
	private SimpleWebServiceInboundGateway gateway;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    KieServerHelper kieServerHelper;


    @Test
    public void testContext() throws Exception {
        assertNotNull(applicationContext);
    }

    @Test
    public void testKieBase() throws Exception {
        KieBase kbase = (KieBase) applicationContext.getBean("drl_kiesample");
        assertNotNull(kbase);
    }

	/**
	 * Emulate the Spring WS MessageDispatcherServlet by calling the gateway
	 * with a DOMSource object representing the payload of the original SOAP
	 * 'sayHello' message.  Expect an 'helloResponse' DOMSource object
	 * to be returned in synchronous fashion, which the MessageDispatcherServlet
	 * would in turn wrap in a SOAP envelope and return to the client.
	 */
	@Test
	public void testSendAndReceive() throws Exception {

        BatchExecutionCommand batchExecutionCommand = getBatchExecutionCommand();
        MessageContext messageContext = kieServerHelper.createXMLMessageContext(batchExecutionCommand, false);

		gateway.invoke(messageContext);
		Object reply = messageContext.getResponse().getPayloadSource();
        assertTrue(reply instanceof DOMSource);

		DOMSource replySource = (DOMSource) reply;
        dumpDOM(replySource);
		//Element element = (Element) replySource.getNode().getFirstChild();
		//assertThat(element.getTagName(), equalTo("helloResponse"));
        //System.out.println("Response :: "+element.getTextContent());
    }

    private BatchExecutionCommand getBatchExecutionCommand() {
        Command insertPersonCmd = CommandFactory.newInsert(applicationContext.getBean("person"), "vkiran");
        Command fireAllRulesCmd = CommandFactory.newFireAllRules();
        List<Command> commands = new ArrayList<Command>();
        commands.add(insertPersonCmd);
        commands.add(fireAllRulesCmd);

        return CommandFactory.newBatchExecution(commands, "ksession1");
    }


    private void dumpDOM(DOMSource domSource) throws Exception {
        System.out.println("---------- response --------------");
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        StreamResult result = new StreamResult(System.out);
        transformer.transform(domSource, result);
        System.out.println("---------- end of response --------------");
    }
}
