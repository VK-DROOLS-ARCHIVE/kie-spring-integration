package org.kie.spring.integration.helper;


import com.thoughtworks.xstream.XStream;
import org.drools.core.runtime.help.impl.XStreamHelper;
import org.kie.api.command.BatchExecutionCommand;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.pox.dom.DomPoxMessage;
import org.springframework.ws.pox.dom.DomPoxMessageFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.StringReader;

public class KieServerHelper {

    public MessageContext createXMLMessageContext(String xml, boolean namespaceAware) throws Exception {
        DomPoxMessageFactory messageFactory = new DomPoxMessageFactory();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(namespaceAware);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DomPoxMessage request = new DomPoxMessage(document, transformer, "text/xml");
        return new DefaultMessageContext(request, messageFactory);
    }

    public MessageContext createXMLMessageContext(BatchExecutionCommand batchExecutionCommand, boolean namespaceAware) throws Exception {
        return createXMLMessageContext(toXML(batchExecutionCommand), namespaceAware);
    }

    public String toXML(BatchExecutionCommand batchExecutionCommand) throws Exception {
        XStream xStream = new XStream();
        XStreamHelper.setAliases(xStream);
        return xStream.toXML(batchExecutionCommand);
    }
}
