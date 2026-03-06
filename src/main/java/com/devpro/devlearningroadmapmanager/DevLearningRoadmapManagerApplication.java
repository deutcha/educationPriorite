package com.devpro.devlearningroadmapmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DevLearningRoadmapManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevLearningRoadmapManagerApplication.class, args);
    }

}
