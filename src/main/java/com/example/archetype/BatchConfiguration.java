package com.example.archetype;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    @Bean
    public Job archetypeJob(JobRepository jobRepository, Step archetypeStep) {
        return new JobBuilder("archetypeJob", jobRepository)
                .start(archetypeStep)
                .build();
    }

    @Bean
    public Step archetypeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("archetypeStep", jobRepository)
                .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED, transactionManager)
                .build();
    }
}
