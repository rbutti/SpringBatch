package com.rave.batch;

import java.util.ArrayList;
import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.rave.model.Report;

public class DatabaseWriter extends AbstractItemWriter {

	@Autowired
	DataSource dataSource;
	
	private static int count =0;
	
	@Override
	public void writeItems(List<Object> items) throws Exception {
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	
		
		List<Object[]> objectList = new ArrayList<Object[]>();
		for (Object item : items) {
			Report report = (Report)item;
			Object[] objectArray = {report.getDate(), report.getImpressions(), report.getClicks(), report.getEarning()};
			objectList.add(objectArray);
		}
		jdbcTemplate.batchUpdate("insert into RAW_REPORT(DATE,IMPRESSIONS,CLICKS,EARNINGS) values (?,?, ?, ?)", objectList);
		count++;
		if(count == 2){
			System.out.println("Exception occured for chunk size = "+ items.size());
			throw new SkipException();
		}
		
		System.out.println("Number of Records Persisted = "+ items.size());
		
	}

}
