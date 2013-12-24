package org.kie.spring.integration.http;

import com.thoughtworks.xstream.XStream;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.runtime.help.impl.XStreamHelper;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.integration.AbstractKieServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieHttpServer extends AbstractKieServer {

    private static final Logger log               = LoggerFactory.getLogger(KieHttpServer.class);

    public String execute(String input) {

        XStream xStream = new XStream();
        XStreamHelper.setAliases(xStream);
        String outputXml = "";
        try {
            dump(input);
            BatchExecutionCommandImpl batchExecutionCommand = (BatchExecutionCommandImpl) xStream.fromXML(input);
            StatelessKieSession statelessKieSession = lookupKieSession(batchExecutionCommand);
            ExecutionResults results = statelessKieSession.execute(batchExecutionCommand);
            outputXml = toXML((ExecutionResultImpl) results, xStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputXml;
    }

    private void dump(String xml) throws Exception {
        log.debug("---------- request --------------\n");
        log.debug(xml);
        log.debug("\n---------- end of request --------------");
    }

}
