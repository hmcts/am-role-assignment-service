package uk.gov.hmcts.reform.roleassignment.controller.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.roleassignment.controller.advice.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.roleassignment.controller.advice.ErrorConstants.RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.reform.roleassignment.controller.advice.ErrorConstants.UNKNOWN_EXCEPTION;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.roleassignment")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class RoleAssignmentControllerAdvice {

    private static final long serialVersionUID = 2L;

    private static final String LOG_STRING = "handling exception: {}";
    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentControllerAdvice.class);

    @ExceptionHandler(InvalidRequest.class)
    public ResponseEntity<Object> customValidationError(
        InvalidRequest ex) {
        return errorDetailsResponseEntity(
            ex,
            BAD_REQUEST,
            INVALID_REQUEST.getErrorCode(),
            INVALID_REQUEST.getErrorMessage()
                                         );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> customValidationBadRequestError(
        BadRequestException ex) {
        return errorDetailsResponseEntity(
            ex,
            BAD_REQUEST,
            BAD_REQUEST.value(),
            "Bad Request"
        );
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Object> customRequestHeaderError(
        HttpMediaTypeNotAcceptableException ex) {
        return errorDetailsResponseEntity(
            ex,
            UNSUPPORTED_MEDIA_TYPE,
            UNSUPPORTED_MEDIA_TYPE.value(),
            UNSUPPORTED_MEDIA_TYPE.getReasonPhrase()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValidException(
        HttpServletRequest request,
        MethodArgumentNotValidException exeception) {
        return errorDetailsResponseEntity(
            exeception,
            BAD_REQUEST,
            INVALID_REQUEST.getErrorCode(),
            INVALID_REQUEST.getErrorMessage()
                                         );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFoundException(
        HttpServletRequest request,
        ResourceNotFoundException exeception) {
        return errorDetailsResponseEntity(
            exeception,
            HttpStatus.NOT_FOUND,
            RESOURCE_NOT_FOUND.getErrorCode(),
            RESOURCE_NOT_FOUND.getErrorMessage()
                                         );
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    protected ResponseEntity<Object> handleHttpMessageConversionException(
        HttpServletRequest request,
        HttpMessageConversionException exeception) {
        return errorDetailsResponseEntity(
            exeception,
            BAD_REQUEST,
            INVALID_REQUEST.getErrorCode(),
            INVALID_REQUEST.getErrorMessage()
                                         );
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleUnknownException(
        HttpServletRequest request,
        Exception exeception) {
        return errorDetailsResponseEntity(
            exeception,
            HttpStatus.INTERNAL_SERVER_ERROR,
            UNKNOWN_EXCEPTION.getErrorCode(),
            UNKNOWN_EXCEPTION.getErrorMessage());
    }

    public String getTimeStamp() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
    }

    public static Throwable getRootException(Throwable exception) {
        Throwable rootException = exception;
        while (rootException.getCause() != null) {
            rootException = rootException.getCause();
        }
        return rootException;
    }

    private ResponseEntity<Object> errorDetailsResponseEntity(Exception ex, HttpStatus httpStatus, int errorCode,
                                                              String errorMsg) {

        logger.error(LOG_STRING, ex);
        ErrorResponse errorDetails = ErrorResponse.builder()
                                                  .errorCode(errorCode)
                                                  .errorMessage(errorMsg)
                                                  .errorDescription(getRootException(ex).getLocalizedMessage())
                                                  .timeStamp(getTimeStamp())
                                                  .build();
        return new ResponseEntity<>(
            errorDetails, httpStatus);
    }
}
