package com.example.batchprocessing.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.example.batchprocessing.model.Customer;
import com.example.batchprocessing.repository.CustomerRepository;

import lombok.AllArgsConstructor;
import net.bytebuddy.agent.builder.AgentBuilder.FallbackStrategy.Simple;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class CsvBatchConfig {
	
	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	//Create Reader
	
	@Bean
	public FlatFileItemReader<Customer> customerReader(){
		FlatFileItemReader<Customer> itemReader=new FlatFileItemReader<>();
		itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
		itemReader.setName("csv-reader");
		itemReader.setLinesToSkip(1);
		itemReader.setLineMapper(linerMapper());
		return itemReader;
	}

	private LineMapper<Customer> linerMapper() {
		DefaultLineMapper<Customer> lineMapper=new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id","firstName","lastName","email","gender","contactNo","country","dob");
		BeanWrapperFieldSetMapper<Customer> fieldExtractor=new BeanWrapperFieldSetMapper<>();
		fieldExtractor.setTargetType(Customer.class);
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldExtractor);
		
		return lineMapper;
	}
	
	//Create Processor
	
	@Bean
	public CustomerProcessor customerProcessor() {
		return new CustomerProcessor();
	}
	
	//Create Writer
	
	@Bean
	public RepositoryItemWriter<Customer> customerWriter(){
		
		RepositoryItemWriter<Customer> repositoryItemWriter=new RepositoryItemWriter<>();
		repositoryItemWriter.setRepository(customerRepo);
		repositoryItemWriter.setMethodName("save");
		
		return repositoryItemWriter;
		
	}
	
	//Create Step
	
	@Bean
	public Step step() {
		return stepBuilderFactory.get("step-1").<Customer,Customer>chunk(10)
				.reader(customerReader())
				.processor(customerProcessor())
				.writer(customerWriter())
				.taskExecutor(taskExecutor())
				.build();
	}
	
	private TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor taskExecutor=new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(10);
		return taskExecutor;
	}
	
	//Create Job

	@Bean
	public Job job() {
		return jobBuilderFactory.get("customers-import")
				.flow(step())
				.end()
				.build();
	}
	

}
