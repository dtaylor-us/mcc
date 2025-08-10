package us.dtaylor.mcpserver.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralised exception handling for the MCP server.  This advice
 * converts common runtime exceptions into structured HTTP responses
 * so that clients receive meaningful feedback.  Add further
 * handlers as required to map domain-specific exceptions to
 * appropriate status codes.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * Handles illegal argument exceptions thrown by controllers or
     * service methods.  Returns a 400 Bad Request with the error
     * message.  This covers scenarios such as missing assets or
     * invalid request parameters.
     *
     * @param ex the exception
     * @return a response entity with status 400 and body containing the
     *         exception message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles validation failures on controller methods annotated with
     * {@code @Validated}.  Extracts field error messages and returns
     * them in the response body with a 400 Bad Request status.
     *
     * @param ex the validation exception
     * @return a response entity containing field error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> {
            errors.put(err.getField(), err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
