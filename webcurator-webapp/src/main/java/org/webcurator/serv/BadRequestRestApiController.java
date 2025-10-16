package org.webcurator.serv;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.webcurator.rest.common.FailureResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@RestControllerAdvice
public class BadRequestRestApiController {

    /**
     * Handle malformed or unreadable JSON body
     **/
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String contentUri = req.getContextPath();
        String url = req.getRequestURI().substring(contentUri.length());

        ex.getMostSpecificCause();
        String msg = ex.getMostSpecificCause().getMessage();


        return FailureResponse.error(HttpStatus.BAD_REQUEST, "Invalid request body: " + msg, url);
    }

    /**
     * Handle validation errors for @Valid annotated DTOs
     **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String contentUri = req.getContextPath();
        String url = req.getRequestURI().substring(contentUri.length());

        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return FailureResponse.error(HttpStatus.BAD_REQUEST, "Validation failed: " + details, url);
    }

    /**
     * Handle missing query or form parameters
     **/
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String contentUri = req.getContextPath();
        String url = req.getRequestURI().substring(contentUri.length());

        return FailureResponse.error(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName(), url);
    }

    /**
     * Handle wrong type for query/path parameters
     **/
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String contentUri = req.getContextPath();
        String url = req.getRequestURI().substring(contentUri.length());

        String message = String.format(
                "Invalid type for parameter '%s': expected %s but got '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getValue()
        );


        return FailureResponse.error(HttpStatus.BAD_REQUEST, message, url);
    }

    /*
     * Catch-all for other bad requests or unexpected errors
     **/
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleGeneric(Exception ex, HttpServletRequest req) throws IOException {
//        String contentUri = req.getContextPath();
//        String url = req.getRequestURI().substring(contentUri.length());
//
//        return FailureResponse.error(HttpStatus.BAD_REQUEST, "Unexpected error: " + ex.getMessage(), url);
//    }
}
