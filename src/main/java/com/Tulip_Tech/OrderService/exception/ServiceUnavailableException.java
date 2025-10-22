package com.Tulip_Tech.OrderService.exception;
import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, HttpStatus status) {
        super(message);
    }
}
