package com.rave.batch;

import java.util.ArrayList;
import java.util.List;

import javax.batch.api.listener.JobListener;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Transactional(propagation=Propagation.NEVER)
public class SampleListener2 implements JobListener {

	@Autowired
	DataSource dataSource;
	
	@Inject
	private PlatformTransactionManager transactionManager;

	@Override
	public void beforeJob() throws Exception {}

	@Override
	public void afterJob() throws Exception {

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

		List<Object[]> objectList = new ArrayList<Object[]>();

		Object[] objectArray = {System.currentTimeMillis(), "Before Job", "Before Job", "1.0"};
		objectList.add(objectArray);

		jdbcTemplate.batchUpdate("insert into RAW_REPORT(DATE,IMPRESSIONS,CLICKS,EARNINGS) values (?,?, ?, ?)", objectList);
		transactionManager.commit(status);
		System.out.println("Before Job 1");
	}

}
