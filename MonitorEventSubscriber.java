package com.isd.cep.subscriber;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

/**
 * Wraps Esper Statement and Listener. No dependency on Esper libraries.
 */
@Component
public class MonitorEventSubscriber implements StatementSubscriber {
	
	/** Logger */
    private static Logger LOG = LoggerFactory.getLogger(MonitorEventSubscriber.class);
    
    private EPServiceProvider epService;
    
    private int samplingCount = 10;

    /**
     * {@inheritDoc}
     */    
    
    public String getStatement() {

        // Example of simple EPL with a batch Window
        //return "select avg(CD) as avg_val from CDEvent.win:time_batch(10 sec)";
    	
    	return "select avg(CD) as avg_val from CDEvent#length_batch(var_sampling_Count)";
        
    }

    /**
     * Listener method called when Esper has detected a pattern match.
     */
    public void update(Map<String, Double> eventMap) throws Exception{ 

        // average temp over 5 secs
        Double avg = (Double) eventMap.get("avg_val");
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================================================");
        sb.append("\n- [MONITOR] " + samplingCount + " Sampling Wafers Metrology in Etching Equipment #1 - [Wafers' Average CD = " + (Math.round(avg*100)/100.0) + "]");
        sb.append("\n- [Calling Virtual Metrology Model] Etching Equipment #1");
        sb.append("\n=================================================================================================");
        Thread.sleep(1000);
        
        epService = EPServiceProviderManager.getDefaultProvider();
        if(epService.getEPRuntime().getVariableValue("CurrentEvent").equals("CRITICAL")) { 
        	epService.getEPRuntime().setVariableValue("CurrentEvent", "CRITICAL-COMPLETE");
        	this.samplingCount = 15;
        }
        
        LOG.debug(sb.toString());
    }
    
}
