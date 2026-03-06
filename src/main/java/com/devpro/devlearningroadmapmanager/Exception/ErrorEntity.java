package com.devpro.devlearningroadmapmanager.Exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

public record ErrorEntity(
        String apiPath,
        HttpStatus errorCode,
        String errorMessage,
        Date errorTime
) {
}
