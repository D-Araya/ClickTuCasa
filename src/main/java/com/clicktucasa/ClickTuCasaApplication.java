package com.clicktucasa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Hito 4 microservice. This is the only
 * class in the whole codebase whose sole purpose is to bootstrap the
 * framework — it contains no business logic whatsoever.
 */
@SpringBootApplication
public class ClickTuCasaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClickTuCasaApplication.class, args);
    }
}
