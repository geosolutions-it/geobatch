package it.geosolutions.geobatch.camel.beans;

import java.io.Serializable;
import java.util.List;

public class JMSFlowRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String flowId;

    private List<String> files;

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(final String flowId) {
        this.flowId = flowId;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(final List<String> files) {
        this.files = files;
    }

}
