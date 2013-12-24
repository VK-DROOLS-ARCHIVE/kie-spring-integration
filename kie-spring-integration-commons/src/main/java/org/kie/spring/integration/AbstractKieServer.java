package org.kie.spring.integration;

import com.thoughtworks.xstream.XStream;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.runtime.help.impl.XStreamHelper;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public abstract class AbstractKieServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    protected String toXML(BatchExecutionCommand batchExecutionCommand) throws Exception {
        XStream xStream = new XStream();
        XStreamHelper.setAliases(xStream);
        return xStream.toXML(batchExecutionCommand);
    }

    protected String toXML(ExecutionResultImpl executionResults, XStream xStream){
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

    protected StatelessKieSession lookupKieSession(BatchExecutionCommandImpl batchExecutionCommand) {
        String lookup = batchExecutionCommand.getLookup();
        return (StatelessKieSession) applicationContext.getBean(lookup);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
