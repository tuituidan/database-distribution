package com.tuituidan.openhub;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DatabaseDistributionApplication.
 *
 * @author tuituidan
 */
@SpringBootApplication
public class DatabaseDistributionApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC+8"));
       SpringApplication.run(DatabaseDistributionApplication.class, args);
    }
}
