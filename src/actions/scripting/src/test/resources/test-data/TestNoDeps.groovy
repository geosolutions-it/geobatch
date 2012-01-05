/** 
 * Java Imports ...
 **/
/*
 import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration
 import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder
 import it.geosolutions.geobatch.flow.event.action.ActionException
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/** 
 * Script execute function
 **/
public Map execute(Map argsMap) throws Exception {

	// ////////////////////////////////////////////////////////////////////

	/// Extract values from config
	String v1 = k1;   // k1 value should be "v1"

	List ret = new ArrayList();
	ret.add(v1!=null?v1:"null");

	/// Extract explicit values
	ret.add(k1!=null?k1:"null");
	ret.add(k2!=null?k2:"null");
	intVar++;
	ret.add(intVar!=null?intVar.toString():"null");


	Map retMap=new HashMap();

	// from ScriptinAction
	// public static final String RETURN_KEY="return";
	
	retMap.put("return",ret);
	return retMap;
}
