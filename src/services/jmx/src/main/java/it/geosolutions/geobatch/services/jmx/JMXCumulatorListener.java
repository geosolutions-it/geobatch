package it.geosolutions.geobatch.services.jmx;

import it.geosolutions.geobatch.flow.event.listeners.cumulator.CumulatingProgressListener;

import java.util.Iterator;

public class JMXCumulatorListener extends CumulatingProgressListener implements JMXProgressListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    protected JMXCumulatorListener() {
        super(null, null);
    }

    public static String printMessages(CumulatingProgressListener listener) {
        StringBuffer buf = new StringBuffer();
        Iterator<String> it = listener.getMessages().iterator();
        while (it.hasNext()) {
            buf.append(it.next()).append("\n");
        }
        return buf.toString();
    }

    @Override
    public void setTask(String currentTask) {
        msg(currentTask);
    }

}
