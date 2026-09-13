package br.com.solution.addressbook.api.error;

import br.com.solution.addressbook.application.error.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiError> handleDomain(DomainException exception) {
        return response(statusFor(exception.getCode()), exception.getCode(), exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR",
                "Revise os campos informados.", fields);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied() {
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Voce nao tem permissao para esta acao.", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleConflict() {
        return response(HttpStatus.CONFLICT, "DATA_CONFLICT", "Dados em conflito com um cadastro existente.", null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleUploadTooLarge() {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "PHOTO_TOO_LARGE",
                "A foto deve ter no maximo 2 MB.", null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> handleMalformedRequest() {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Formato da requisicao invalido.", null);
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    ResponseEntity<ApiError> handleMissingRequestValue() {
        return response(HttpStatus.BAD_REQUEST, "MISSING_REQUEST_VALUE",
                "Parte obrigatoria da requisicao nao informada.", null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Nao foi possivel concluir a operacao.", null);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                              Map<String, String> fields) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), code, message, fields));
    }

    private static HttpStatus statusFor(String code) {
        return switch (code) {
            case "INVALID_CREDENTIALS" -> HttpStatus.UNAUTHORIZED;
            case "ACCESS_DENIED", "ROLE_CHANGE_FORBIDDEN", "ACCOUNT_INACTIVE",
                    "PROFILE_PHOTO_SELF_ONLY" -> HttpStatus.FORBIDDEN;
            case "USER_NOT_FOUND", "ADDRESS_NOT_FOUND", "PROFILE_PHOTO_NOT_FOUND",
                    "ZIP_CODE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CPF_ALREADY_EXISTS", "LAST_ACTIVE_ADMIN", "SELF_DEACTIVATION_FORBIDDEN",
                    "ACCOUNT_INACTIVE_READ_ONLY" -> HttpStatus.CONFLICT;
            case "PHOTO_TOO_LARGE" -> HttpStatus.PAYLOAD_TOO_LARGE;
            case "POSTAL_CODE_PROVIDER_UNAVAILABLE" -> HttpStatus.BAD_GATEWAY;
            case "INVALID_CPF", "INVALID_ZIP_CODE", "PRIMARY_ADDRESS_REQUIRED",
                    "EMPTY_PROFILE_PHOTO", "PHOTO_READ_ERROR", "INVALID_PHOTO_TYPE" ->
                    HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
