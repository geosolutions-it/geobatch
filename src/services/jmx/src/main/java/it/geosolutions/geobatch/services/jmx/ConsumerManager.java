package it.geosolutions.geobatch.services.jmx;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ConsumerManager extends Serializable {

	/**
	 * the key of the ServiceID which should match to the GeoBatch's action you want to call
	 */
	public final static String SERVICE_ID_KEY = "SERVICE_ID";
	
    /**
     * the key of the input list of files will be passed to the running action  
     */
    public final static String INPUT_KEY = "INPUT";

    /**
     * returns the status of the selected consumer
     * 
     * @param uuid
     * @return {@link ConsumerStatus}
     */
    public ConsumerStatus getStatus();
    
    /**
     * Used to dispose the consumer instance from the consumer registry.
     * 
     * @param uuid the unique id of the remote consumer to dispose
     * @throws Exception if:
     *             <ul>
     *             <li>the consumer is uuid is null</li>
     *             <li>the consumer is already disposed</li>
     *             <li>the connection is lost</li>
     *             </ul>
     */
    public void dispose() throws Exception;
    
    /**
     * create the configured action on the remote GeoBatch server through the
     * JMX connection
     * 
     * @param config A map containing the list of needed parameters, inputs and
     *            outputs used by the action
     * @throws Exception if:
     *             <ul>
     *             <li>the passed map is null</li>
     *             <li>the passed map doesn't contains needed keys</li>
     *             <li>the connection is lost</li>
     *             </ul>
     */
    public void run(Serializable event) throws Exception;
	
    public List<Map<String, String>> getConfigurations();
    
    public Map<String, String> getConfiguration(int i);
    
    public String getUuid();
    
    public Collection<JMXProgressListener> getListeners();
    
    public <T extends JMXProgressListener> Collection<T> getListeners(Class<T> clazz);
}
