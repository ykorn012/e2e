package com.isd.cep.handler;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.isd.cep.event.CDEvent;
import com.isd.cep.subscriber.StatementSubscriber;

/**
 * This class handles incoming CD Events. It processes them through the EPService, to which
 * it has attached the 3 queries.
 */
@Component
@Scope(value = "singleton")
public class CDEventHandler implements InitializingBean{

    /** Logger */
    private static Logger LOG = LoggerFactory.getLogger(CDEventHandler.class);

    /** Esper service */
    private EPServiceProvider epService;
    private EPStatement criticalEventStatement;
    private EPStatement warningEventStatement;
    private EPStatement monitorEventStatement;

    @Autowired
    @Qualifier("criticalEventSubscriber")
    private StatementSubscriber criticalEventSubscriber;

    @Autowired
    @Qualifier("warningEventSubscriber")
    private StatementSubscriber warningEventSubscriber;

    @Autowired
    @Qualifier("monitorEventSubscriber")
    private StatementSubscriber monitorEventSubscriber;

    /**
     * Configure Esper Statement(s).
     */
    public void initService() {

        LOG.debug("Initializing Servcie ..");
        Configuration config = new Configuration();
        config.addEventTypeAutoName("com.isd.cep.event");
        config.addVariable("CurrentEvent", String.class, "DEFAULT");
        config.addVariable("var_sampling_Count", Integer.class, 10);
        epService = EPServiceProviderManager.getDefaultProvider(config);        
        //epService.getEPAdministrator().createEPL("create variable integer var_sampling_count = 10");
        
        createCriticalCDCheckExpression();
        createWarningCDCheckExpression();
        createCDMonitorExpression();        
    }

    /**
     * EPL to check for a sudden critical rise across 4 events, where the last event is 1.5x greater
     * than the first event. This is checking for a sudden, sustained escalating rise in the
     * CD
     */
    public void createCriticalCDCheckExpression() {
        
        LOG.debug("create Critical Sampling CD Check Expression in Etching Equipment #1");
        criticalEventStatement = epService.getEPAdministrator().createEPL(criticalEventSubscriber.getStatement());
        criticalEventStatement.setSubscriber(criticalEventSubscriber);
    }

    /**
     * EPL to check for 2 consecutive CD events over the threshold - if matched, will alert
     * listener.
     */
    public void createWarningCDCheckExpression() {

        LOG.debug("create Warning Sampling CD Check Expression in Etching Equipment #1");
        warningEventStatement = epService.getEPAdministrator().createEPL(warningEventSubscriber.getStatement());
        warningEventStatement.setSubscriber(warningEventSubscriber);
    }

    /**
     * EPL to monitor the average CD every 10 seconds. Will call listener on every event.
     */
    public void createCDMonitorExpression() {

        LOG.debug("create Sampling CD Average Monitor in Etching Equipment #1");
        monitorEventStatement = epService.getEPAdministrator().createEPL(monitorEventSubscriber.getStatement());
        monitorEventStatement.setSubscriber(monitorEventSubscriber);
    }

    /**
     * Handle the incoming CDEvent.
     */
    public void handle(CDEvent event){

        LOG.debug(event.toString());
        epService.getEPRuntime().sendEvent(event);
        
        if(epService.getEPRuntime().getVariableValue("CurrentEvent").equals("CRITICAL-COMPLETE")) { 
        	//System.out.println("CRITICAL");
        	monitorEventStatement.destroy();   
        	LOG.debug("★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★");
        	LOG.debug("★★★★★★★★★★★★  Change Dynamic Sampling Count 10 => 15 ★★★★★★★★★★★★ ");
        	LOG.debug("★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★");
        	epService.getEPRuntime().setVariableValue("var_sampling_Count", 15);
            monitorEventStatement = epService.getEPAdministrator().createEPL(monitorEventSubscriber.getStatement());
            monitorEventStatement.setSubscriber(monitorEventSubscriber);
        	epService.getEPRuntime().setVariableValue("CurrentEvent", "DEFAULT");
        	try {
        		Thread.sleep(2000);
        	} catch (InterruptedException e) {
                LOG.error("Thread Interrupted", e);
            }   
        }
    }

    @Override
    public void afterPropertiesSet() {
        
        LOG.debug("Configuring..");
        initService();
    }    

}
