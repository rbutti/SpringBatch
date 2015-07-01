package com.rave;

import java.util.Properties;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.JobExecution;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class App {
	
	public static void main(String[] args) {

		String[] springConfig  = 
			{	"spring/batch/config/database.xml", 
				"spring/batch/config/context.xml"
			};
		
		ApplicationContext context = 
				new ClassPathXmlApplicationContext(springConfig);
	
		
		final Properties params = new Properties();

		try {

			JobExecution jobExecution = runJob(BatchUtil.JobCommand.START, "job-report", params, 0l);
			System.out.println("Exit Status : " + jobExecution.getBatchStatus());

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done");

	}
	
	
	/**
	 * Utility method that runs the batch job
	 */
	protected static JobExecution runJob(final BatchUtil.JobCommand command, final String jobName, final Properties params, final long executionId)
			throws Exception {

		JobOperator jobOperator = BatchRuntime.getJobOperator();

		long jobExecutionId = -1;
		if(command == BatchUtil.JobCommand.START) {
			jobExecutionId = jobOperator.start(jobName, params);
		} else if(command == BatchUtil.JobCommand.RESTART) {
			if(executionId == jobExecutionId) {
				throw new IllegalArgumentException("jobExecutionId cannot be -1.");
			}
			jobExecutionId = jobOperator.restart(executionId, params);
		}

		JobExecution jobExecution = jobOperator.getJobExecution(jobExecutionId);
		jobExecution = BatchUtil.waitForJobToEnd(jobOperator, jobExecutionId, 1000, 10);

		return jobExecution;
	}
	
	
}
