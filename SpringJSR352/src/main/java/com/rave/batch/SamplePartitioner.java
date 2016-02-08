package com.rave.batch;

import javax.batch.api.partition.PartitionMapper;
import javax.batch.api.partition.PartitionPlan;
import javax.batch.api.partition.PartitionPlanImpl;
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;

public class SamplePartitioner implements PartitionMapper {

	@Inject
	private StepContext stepContext;
	
	@Override
	public PartitionPlan mapPartitions() throws Exception {
		
		stepContext.setPersistentUserData("hello");
		PartitionPlan plan = new PartitionPlanImpl();
		plan.setPartitions(1);
		plan.setPartitionProperties(null);
		return plan;
	}

}
