kie-ws-inbound-gateway
===================
This sample demonstrates a Kie Server, configured using a inbound Web Service Gateway. Take a look at `web.xml` in the WEB-INF directory where the Spring Web Services Message-dispatching Servlet is defined. Then have a look at the `spring-ws-config.xml` file (also in the WEB-INF directory) where the Spring WS EndpointMapping is defined. Finally view the Spring Integration configuration in the `kie-server.xml` file within the `org.kie.spring.integration.ws` package where the actual gateway is defined along with a channel and service-activator.

To use the gateway, you can run the tests that are located within the "src/test/java" directory. One is for standalone testing of the gateway itself, while the other tests the gateway running on a web server. The latter uses Spring Web Services' client-side support. Alternatively, you can simply start the server, and then send invocations with any standalone HTTP client testing tool. The request format should be similar to the following and should be POSTed to the service URL e.g. `http://localhost:8080/kie-inbound-gateway-6.0.1.Final/kieservice`:

	<?xml version="1.0" encoding="UTF-8"?>
	<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
		<SOAP-ENV:Header/>
		<SOAP-ENV:Body>
			<batch-execution>
				<lookup>ksession1</lookup>
				<commands>
					<insert>
						<object class="org.kie.spring.beans.Person">
							<name>HAL</name>
							<age>0</age>
							<sex/>
							<alive>false</alive>
							<happy>false</happy>
						</object>
						<outIdentifier>vkiran</outIdentifier>
						<returnObject>true</returnObject>
						<entryPoint>DEFAULT</entryPoint>
						<disconnected>false</disconnected>
					</insert>
					<fire-all-rules>
						<max>-1</max>
					</fire-all-rules>
				</commands>
			</batch-execution>
		</SOAP-ENV:Body>
	</SOAP-ENV:Envelope>