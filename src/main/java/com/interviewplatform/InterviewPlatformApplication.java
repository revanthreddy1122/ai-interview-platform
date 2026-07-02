package com.interviewplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InterviewPlatformApplication {

    public static void main(String[] args) {
        System.out.println(">>> DEBUG SPRING_DATASOURCE_URL = [" + System.getenv("SPRING_DATASOURCE_URL") + "]");
        SpringApplication.run(InterviewPlatformApplication.class, args);
    }
}