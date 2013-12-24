package org.kie.spring.integration.ws;


import com.thoughtworks.xstream.XStream;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.runtime.help.impl.XStreamHelper;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.integration.AbstractKieServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.xml.source.DomSourceFactory;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class KieStatelessServer extends AbstractKieServer {

    private static final Logger log               = LoggerFactory.getLogger(KieStatelessServer.class);


    public Source execute(DOMSource request) {

        XStream xStream = new XStream();
        XStreamHelper.setAliases(xStream);
        String outputXml="";
        try {
            dumpDOM(request);
            InputStream inputStream = convertoToInputStream(request);
            BatchExecutionCommandImpl batchExecutionCommand = (BatchExecutionCommandImpl) xStream.fromXML(inputStream);
            StatelessKieSession statelessKieSession = lookupKieSession(batchExecutionCommand);
            ExecutionResults results = statelessKieSession.execute(batchExecutionCommand);
            outputXml = toXML((ExecutionResultImpl) results, xStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DomSourceFactory().createSource(outputXml);
    }

    private static InputStream convertoToInputStream(DOMSource xmlSource) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(outputStream);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void dumpDOM(DOMSource domSource) throws Exception {
        log.debug("---------- request --------------\n");
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        StreamResult result = new StreamResult(System.out);
        transformer.transform(domSource, result);
        log.debug("\n---------- end of request --------------");
    }



}
