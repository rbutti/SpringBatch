package com.rave;

import java.util.Properties;

import javax.batch.runtime.JobExecution;

import org.springframework.batch.core.jsr.launch.JsrJobOperator;


public class App {
	
	public static void main(String[] args) {

		final Properties params = new Properties();

		try {

			JobExecution jobExecution = runJob(BatchUtil.JobCommand.START, "job-report", params);
			System.out.println("Exit Status : " + jobExecution.getBatchStatus());

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done");

	}
	
	
	/**
	 * Utility method that runs the batch job
	 */
	protected static JobExecution runJob(final BatchUtil.JobCommand command, final String jobName, final Properties params)
			throws Exception {

		JsrJobOperator jobOperator = new JsrJobOperator();

		long jobExecutionId = -1;
		if(command == BatchUtil.JobCommand.START) {
			jobExecutionId = jobOperator.start(jobName, params);
		} 

		JobExecution jobExecution = jobOperator.getJobExecution(jobExecutionId);
		jobExecution = BatchUtil.waitForJobToEnd(jobOperator, jobExecutionId, 1000, 10);

		return jobExecution;
	}
	
	
}
