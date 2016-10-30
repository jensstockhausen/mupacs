package de.famst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
public class MupacsApplication extends AsyncConfigurerSupport
{
    private static Logger LOG = LoggerFactory.getLogger(CommandLineHandler.class);

    public static void main(String[] args)
    {
        try ( ConfigurableApplicationContext context = SpringApplication.run(MupacsApplication.class, args); )
        {
            for (String beanName:context.getBeanDefinitionNames())
            {
                LOG.info("Bean: [{}]", beanName);
            }
        }
    }

    @Override
    public Executor getAsyncExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Importer-");
        executor.initialize();
        return executor;
    }


}
