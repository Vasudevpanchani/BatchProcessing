package com.example.batchprocessing.config;

import org.springframework.batch.item.ItemProcessor;

import com.example.batchprocessing.model.Customer;

public class CustomerProcessor implements ItemProcessor<Customer, Customer>{

	
	
	@Override
	public Customer process(Customer item) throws Exception {
		
		//logic
		
		return item;
	}

}
