package uk.gov.hmcts.reform.roleassignment.controller.advice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.reform.roleassignment.controller.WelcomeController;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

class RoleAssignmentControllerAdviceTest {

    private transient RoleAssignmentControllerAdvice csda = new RoleAssignmentControllerAdvice();

    private transient HttpServletRequest servletRequestMock = mock(HttpServletRequest.class);

    private transient WelcomeController welcomeController = new WelcomeController();

    @Test
    void customValidationError() {
        InvalidRequest invalidRequestException = mock(InvalidRequest.class);
        ResponseEntity<Object> responseEntity = csda.customValidationError(invalidRequestException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void customValidationBadRequestError() {
        BadRequestException badRequestException = mock(BadRequestException.class);
        ResponseEntity<Object> responseEntity = csda.customValidationBadRequestError(badRequestException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void customRequestHeaderError() {
        HttpMediaTypeNotAcceptableException customContentTypeException = mock(
            HttpMediaTypeNotAcceptableException.class);
        ResponseEntity<Object> responseEntity = csda.customRequestHeaderError(customContentTypeException);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void handleMethodArgumentNotValidException() {
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
        ResponseEntity<Object> responseEntity =
            csda.handleMethodArgumentNotValidException(servletRequestMock, methodArgumentNotValidException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException resourceNotFoundException =
            mock(ResourceNotFoundException.class);
        ResponseEntity<Object> responseEntity =
            csda.handleResourceNotFoundException(servletRequestMock,resourceNotFoundException);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void handleHttpMessageConversionException() {
        HttpMessageConversionException httpMessageConversionException =
            mock(HttpMessageConversionException.class);
        ResponseEntity<Object> responseEntity =
            csda.handleHttpMessageConversionException(servletRequestMock, httpMessageConversionException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void handleUnknownException() {
        Exception exception = mock(Exception.class);
        ResponseEntity<Object> responseEntity = csda.handleUnknownException(servletRequestMock, exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getStatusCodeValue());

    }

    @Test
    void getTimeStamp() {
        String time = csda.getTimeStamp();
        assertEquals(time.substring(0,16), new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH).format(new Date()));
    }

    @Test
    void testInvalidRequest() {
        Assertions.assertThrows(InvalidRequest.class, () -> {
            welcomeController.getException("invalidRequest");
        });
    }

    @Test
    void testResourceNotFoundException() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            welcomeController.getException("resourceNotFoundException");
        });
    }

    @Test
    void testHttpMessageConversionException() {
        Assertions.assertThrows(HttpMessageConversionException.class, () -> {
            welcomeController.getException("httpMessageConversionException");
        });
    }

    @Test
    void testBadRequestException() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            welcomeController.getException("badRequestException");
        });
    }

    @Test
    void notReadableException_RoleType() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException("Role Type");
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains("Role Type"));
    }

    @Test
    void notReadableException_UUID() {
        HttpMessageNotReadableException httpMessageNotReadableException = new HttpMessageNotReadableException("UUID");
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains("UUID"));
    }

    @Test
    void nullException() {
        NullPointerException nullPointerException = mock(NullPointerException.class);
        ResponseEntity<ErrorResponse> responseEntity = csda.nullException(nullPointerException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
    }
}
