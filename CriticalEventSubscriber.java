package com.isd.cep.subscriber;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.isd.cep.event.CDEvent;

/**
 * Wraps Esper Statement and Listener. No dependency on Esper libraries.
 */
@Component
public class CriticalEventSubscriber implements StatementSubscriber {

    /** Logger */
    private static Logger LOG = LoggerFactory.getLogger(CriticalEventSubscriber.class);

    /** Used as the minimum starting threshold for a critical event. */
    private static final String CRITICAL_EVENT_THRESHOLD = "100";
    
    /**
     * If the last event in a critical sequence is this much greater than the first - issue a
     * critical alert.
     */
    private static final String CRITICAL_EVENT_MULTIPLIER = "1.5";
    
    private EPServiceProvider epService;
    
    /**
     * {@inheritDoc}
     */
    public String getStatement() {
        
        // Example using 'Match Recognise' syntax.
        String crtiticalEventExpression = "select * from CDEvent "
                + "match_recognize ( "
                + "       measures A as cd1, B as cd2, C as cd3, D as cd4 "
                + "       pattern (A B C D) " 
                + "       define "
                + "               A as A.CD > " + CRITICAL_EVENT_THRESHOLD + ", "
                + "               B as (A.CD < B.CD), "
                + "               C as (B.CD < C.CD), "
                + "               D as (C.CD < D.CD) and D.CD > (A.CD * " + CRITICAL_EVENT_MULTIPLIER + ")" + ")";
        
        return crtiticalEventExpression;
    }
    
    /**
     * Listener method called when Esper has detected a pattern match.
     */
    public void update(Map<String, CDEvent> eventMap) {

        // 1st CD in the Critical Sequence
        CDEvent cd1 = (CDEvent) eventMap.get("cd1");
        // 2nd CD in the Critical Sequence
        CDEvent cd2 = (CDEvent) eventMap.get("cd2");
        // 3rd CD in the Critical Sequence
        CDEvent cd3 = (CDEvent) eventMap.get("cd3");
        // 4th CD in the Critical Sequence
        CDEvent cd4 = (CDEvent) eventMap.get("cd4");

        StringBuilder sb = new StringBuilder();
       
        sb.append("**************************************************************************************");
        sb.append("\n- [CRITICAL] : Etching Equipment #1 - Actual Metrology CRITICAL EVENT DETECTED!!!\n");
        sb.append("               [Detect] " + cd1 + " > ");
        sb.append("\n               [Detect] " + cd2 + " > ");
        sb.append("\n               [Detect] " + cd3 + " > ");
        sb.append("\n               [Detect] " + cd4);
        sb.append("\n**************************************************************************************");
        
        epService = EPServiceProviderManager.getDefaultProvider();
        epService.getEPRuntime().setVariableValue("CurrentEvent", "CRITICAL");
        LOG.debug(sb.toString());
    }    
}
