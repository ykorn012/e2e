package com.cor.cep.subscriber;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cor.cep.event.CDEvent;

/**
 * Wraps Esper Statement and Listener. No dependency on Esper libraries.
 */
@Component
public class WarningEventSubscriber implements StatementSubscriber {

    /** Logger */
    private static Logger LOG = LoggerFactory.getLogger(WarningEventSubscriber.class);

    /** If 2 consecutive CD events are greater than this - issue a warning */
    private static final String WARNING_EVENT_THRESHOLD = "400";

    
    /**
     * {@inheritDoc}
     */
    public String getStatement() {
        
        // Example using 'Match Recognise' syntax.
        String warningEventExpression = "select * from CDEvent "
                + "match_recognize ( "
                + "       measures A as cd1, B as cd2 "
                + "       pattern (A B) " 
                + "       define " 
                + "               A as A.CD > " + WARNING_EVENT_THRESHOLD + ", "
                + "               B as B.CD > " + WARNING_EVENT_THRESHOLD + ")";
        
        return warningEventExpression;
    }
    
    /**
     * Listener method called when Esper has detected a pattern match.
     */
    public void update(Map<String, CDEvent> eventMap) {

        // 1st CD in the Warning Sequence
        CDEvent cd1 = (CDEvent) eventMap.get("cd1");
        // 2nd CD in the Warning Sequence
        CDEvent cd2 = (CDEvent) eventMap.get("cd2");

        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------------------------------------------------------");
        sb.append("\n- [WARNING] : Etching Equipment #1 - Virtual Metrology CD SPIKE DETECTED !!!\n");
        sb.append("              Detect #1 : " + cd1 + " > ");
        sb.append("\n              Detect #2 : " + cd2);
        sb.append("\n---------------------------------------------------------------------------------");

        LOG.debug(sb.toString());
    }    

}
