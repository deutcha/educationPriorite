package com.devpro.devlearningroadmapmanager.Exception;


import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({EntityNotFoundException.class})
    public @ResponseBody ErrorEntity handleException(EntityNotFoundException e, HttpServletRequest request) {
        return new ErrorEntity(
                request.getRequestURI(),
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                new Date()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({RuntimeException.class})
    public @ResponseBody ErrorEntity handleRunException(RuntimeException e, HttpServletRequest request) {
        return new ErrorEntity(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                new Date()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody ErrorEntity handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ErrorEntity(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                "Erreur de validation - " + errorMessage,
                new Date()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({FileNotFoundException.class})
    public @ResponseBody ErrorEntity handleFileException(RuntimeException e, HttpServletRequest request) {
        return new ErrorEntity(
                request.getRequestURI(),
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                new Date()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({FileUploadException.class})
    public @ResponseBody ErrorEntity handleFileUpdateException(RuntimeException e, HttpServletRequest request) {
        return new ErrorEntity(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                new Date()
        );
    }

}
