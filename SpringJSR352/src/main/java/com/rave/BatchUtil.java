package com.rave;

import java.util.EnumSet;
import java.util.Set;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

public class BatchUtil {

    public static final Set<BatchStatus> INCOMPLETE_STATUSES = EnumSet.of(BatchStatus.STARTED, BatchStatus.STARTING);
    
    private BatchUtil() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Waits for job to finish. JSR 352 does not provide a mechanism to 
     * launch jobs in the synchronous mode. As a result, the polling needs
     * to occur
     * 
     * @param jobOperator
     * @param executionId
     * @param pollingWait
     * @param maxTries
     * @return
     */
    public static JobExecution waitForJobToEnd(JobOperator jobOperator, 
            final long executionId, final long pollingWait, final int maxTries) {
    	
    	int countTries = 0;
        JobExecution jobExecution =  null;
        do {
            try {
            	countTries++;
                Thread.sleep(pollingWait);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
            jobExecution = jobOperator.getJobExecution(executionId);
        } while(INCOMPLETE_STATUSES.contains(jobExecution.getBatchStatus()) || (countTries < maxTries));
        
        return jobExecution;
    }
    
	public enum JobCommand {
	    START("start"),
	    RESTART("restart");
	    
	    private String value;
	    
	    private JobCommand(String value) {
	        this.value = value;
	    }
	    
	    public String getValue() {
	        return this.value;
	    }
	}
}
