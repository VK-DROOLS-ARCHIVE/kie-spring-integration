package org.kie.spring.integration.ws;


import com.thoughtworks.xstream.XStream;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.runtime.help.impl.XStreamHelper;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
import java.util.Map;

public class KieStatelessServer implements ApplicationContextAware {

    private static final Logger log               = LoggerFactory.getLogger(KieStatelessServer.class);

    private ApplicationContext applicationContext;

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

    private String toXML(ExecutionResultImpl executionResults, XStream xStream){
        StringBuilder stringBuilder = new StringBuilder("<execution-results>");
        Map<String, Object> results = executionResults.getResults();
        for (String key : results.keySet()){
            stringBuilder.append("<result identifier=\"").append(key).append("\">");
            stringBuilder.append(xStream.toXML(results.get(key)));
            stringBuilder.append("</result>");
        }
        Map<String, Object> factHandles = executionResults.getFactHandles();
        for(String handle : factHandles.keySet()){
            DefaultFactHandle defaultFactHandle = (DefaultFactHandle) factHandles.get(handle);
            stringBuilder.append("<fact-handle identifier=\"").append(handle).append("\" external-form=\"").append(defaultFactHandle.toExternalForm()).append("\" />");
        }
        stringBuilder.append("</execution-results>");
        return stringBuilder.toString();
    }

    private StatelessKieSession lookupKieSession(BatchExecutionCommandImpl batchExecutionCommand) {
        String lookup = batchExecutionCommand.getLookup();
        return (StatelessKieSession) applicationContext.getBean(lookup);
    }

    private static InputStream convertoToInputStream(DOMSource xmlSource) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(outputStream);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void dumpDOM(DOMSource domSource) throws Exception {
        System.out.println("---------- request --------------");
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        StreamResult result = new StreamResult(System.out);
        transformer.transform(domSource, result);
        System.out.println("\n---------- end of request --------------");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("Application Context Set.");
    }
}
