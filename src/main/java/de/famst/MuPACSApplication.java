package de.famst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.stream.StreamSupport;

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
public class MuPACSApplication implements AsyncConfigurer
{
    private static final Logger LOG = LoggerFactory.getLogger(MuPACSApplication.class);

    public static void main(String[] args)
    {
        LOG.info("starting application");
        SpringApplication.run(MuPACSApplication.class, args);
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

    /**
     * Dumps all application properties on startup.
     * This helps verify configuration and diagnose issues.
     */
    @Bean
    public static CommandLineRunner propertyDumper(ConfigurableEnvironment environment)
    {
        Logger log = LoggerFactory.getLogger(MuPACSApplication.class);

        return args -> {
            log.info("=".repeat(100));
            log.info("APPLICATION PROPERTIES DUMP");
            log.info("=".repeat(100));

            // Use TreeMap to sort properties alphabetically
            TreeMap<String, Object> allProperties = new TreeMap<>();

            // Iterate through all property sources
            StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> (EnumerablePropertySource<?>) ps)
                .forEach(ps -> {
                    Arrays.stream(ps.getPropertyNames())
                        .forEach(key -> {
                            try {
                                Object value = environment.getProperty(key);
                                // Mask sensitive properties
                                if (isSensitiveProperty(key)) {
                                    allProperties.put(key, "***MASKED***");
                                } else {
                                    allProperties.put(key, value);
                                }
                            } catch (Exception e) {
                                allProperties.put(key, "<error retrieving value>");
                            }
                        });
                });

            // Log application.properties entries (mupacs.* prefix and common Spring properties)
            log.info("");
            log.info("--- MuPACS Configuration ---");
            allProperties.entrySet().stream()
                .filter(e -> e.getKey().startsWith("mupacs."))
                .forEach(e -> log.info("  {} = {}", e.getKey(), e.getValue()));

            log.info("");
            log.info("--- Server Configuration ---");
            allProperties.entrySet().stream()
                .filter(e -> e.getKey().startsWith("server."))
                .forEach(e -> log.info("  {} = {}", e.getKey(), e.getValue()));

            log.info("");
            log.info("--- Database Configuration ---");
            allProperties.entrySet().stream()
                .filter(e -> e.getKey().startsWith("spring.datasource.") ||
                           e.getKey().startsWith("spring.jpa.") ||
                           e.getKey().startsWith("spring.h2."))
                .forEach(e -> log.info("  {} = {}", e.getKey(), e.getValue()));

            log.info("");
            log.info("--- Spring Configuration ---");
            allProperties.entrySet().stream()
                .filter(e -> e.getKey().startsWith("spring.") &&
                           !e.getKey().startsWith("spring.datasource.") &&
                           !e.getKey().startsWith("spring.jpa.") &&
                           !e.getKey().startsWith("spring.h2."))
                .forEach(e -> log.info("  {} = {}", e.getKey(), e.getValue()));

            log.info("");
            log.info("--- Logging Configuration ---");
            allProperties.entrySet().stream()
                .filter(e -> e.getKey().startsWith("logging."))
                .forEach(e -> log.info("  {} = {}", e.getKey(), e.getValue()));

            log.info("");
            log.info("--- Other Properties ---");
            allProperties.entrySet().stream()
                .filter(e -> !e.getKey().startsWith("mupacs.") &&
                           !e.getKey().startsWith("server.") &&
                           !e.getKey().startsWith("spring.") &&
                           !e.getKey().startsWith("logging.") &&
                           !e.getKey().startsWith("java.") &&
                           !e.getKey().startsWith("sun.") &&
                           !e.getKey().startsWith("os.") &&
                           !e.getKey().startsWith("user.") &&
                           !e.getKey().startsWith("file.") &&
                           !e.getKey().startsWith("line.") &&
                           !e.getKey().startsWith("path.") &&
                           !e.getKey().startsWith("awt."))
                .forEach(e -> log.info("  {} = {}", e.getKey(), e.getValue()));

            log.info("");
            log.info("Total properties loaded: {}", allProperties.size());
            log.info("=".repeat(100));
        };
    }

    /**
     * Checks if a property key contains sensitive information that should be masked.
     */
    private static boolean isSensitiveProperty(String key)
    {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password") ||
               lowerKey.contains("secret") ||
               lowerKey.contains("token") ||
               lowerKey.contains("apikey") ||
               lowerKey.contains("api-key") ||
               lowerKey.contains("credential");
    }


}
