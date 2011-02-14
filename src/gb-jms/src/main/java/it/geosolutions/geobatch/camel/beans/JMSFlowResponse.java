package it.geosolutions.geobatch.camel.beans;

import java.io.Serializable;
import java.util.List;


public class JMSFlowResponse implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -700784659986505238L;
    
    private JMSFlowStatus status;
    private List<String> responses;
    

    public JMSFlowResponse(JMSFlowStatus status, List<String> responses) {
        this.status = status;
        this.responses = responses;
    }
    
    public JMSFlowStatus getStatus() {
        return status;
    }
    public void setStatus(final JMSFlowStatus status) {
        this.status = status;
    }
    public List<String> getResponses() {
        return this.responses;
    }
    public void setResponses(final List<String> responses) {
        this.responses = responses;
    }

}
