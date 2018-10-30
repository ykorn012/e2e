package com.isd.cep.event;

import java.util.Date;

/**
 * Immutable CD Event class. The process control system creates these events. The
 * CDEventHandler picks these up and processes them.
 */
public class CDEvent {
	
	/** id in wafer. */
    int index;
    
    /** CD in Etching. */
    private int cd;
    
    /** Time CD reading was taken. */
    private Date timeOfReading;
    
    /**
     * Single value constructor.
     * @param value CD in Etching Machine.
     */
    /**
     * CD constructor.
     * @param index waferId in Etching Machine
     * @param CD CD in Etching
     * @param timeOfReading Time of Reading
     */
    public CDEvent(int index, int cd, Date timeOfReading) {
    	this.index = index;
        this.cd = cd;
        this.timeOfReading = timeOfReading;
    }

    public int getIndex() {
        return index;
    }
    
    /**
     * Get the CD.
     * @return CD in Etching Machine
     */
    public int getCD() {
        return cd;
    }
       
    /**
     * Get time CD reading was taken.
     * @return Time of Reading
     */
    public Date getTimeOfReading() {
        return timeOfReading;
    }

    @Override
    public String toString() {
    	return "Etching Actual Metrology : Sampling Wafer #" + index + " CD [" + cd + " mm]";
    }

}
