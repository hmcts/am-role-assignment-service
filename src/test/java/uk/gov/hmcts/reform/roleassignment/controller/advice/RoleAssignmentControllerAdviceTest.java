package uk.gov.hmcts.reform.roleassignment.controller.advice;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.UnauthorizedException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.UnprocessableEntityException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.ACTORIDTYPE;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.BAD_REQUEST;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.BOOLEAN;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.CLASSIFICATION;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.GRANTTYPE;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.INTEGER;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.LOCALDATETIME;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.ROLECATEGORY;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.ROLETYPE;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.STATUS;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.UUID;

class RoleAssignmentControllerAdviceTest {

    private transient RoleAssignmentControllerAdvice csda = new RoleAssignmentControllerAdvice();

    private transient HttpServletRequest servletRequestMock = mock(HttpServletRequest.class);

    @Test
    void customValidationError() {
        InvalidRequest invalidRequestException = mock(InvalidRequest.class);
        ResponseEntity<Object> responseEntity = csda.customValidationError(invalidRequestException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void customValidationBadRequestError() {
        BadRequestException badRequestException = mock(BadRequestException.class);
        ResponseEntity<Object> responseEntity = csda.customValidationBadRequestError(badRequestException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void customValidationFeignUnauthorizedError() {
        FeignException.Unauthorized unauthorizedException = mock(FeignException.Unauthorized.class);
        ResponseEntity<Object> responseEntity = csda.customValidationFeignUnauthorizedError(unauthorizedException);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void customValidationUnauthorizedError() {
        UnauthorizedException unauthorizedException = mock(UnauthorizedException.class);
        ResponseEntity<Object> responseEntity = csda.customValidationUnauthorizedError(unauthorizedException);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void customValidationForbiddenRequestError() {
        ForbiddenException forbiddenException = mock(ForbiddenException.class);
        ResponseEntity<Object> responseEntity = csda.customForbiddenException(forbiddenException);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void customRequestHeaderError() {
        HttpMediaTypeNotAcceptableException customContentTypeException = mock(
            HttpMediaTypeNotAcceptableException.class);
        ResponseEntity<Object> responseEntity = csda.customRequestHeaderError(customContentTypeException);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void customRequestHeaderError_withWrappedCause() {
        String wrappedMessage = "Wrapped bad request message";
        HttpMediaTypeNotAcceptableException customContentException = mock(HttpMediaTypeNotAcceptableException.class);
        BadRequestException badRequestException = new BadRequestException(wrappedMessage);
        Mockito.when(customContentException.getCause()).thenReturn(badRequestException);
        ResponseEntity<Object> responseEntity = csda.customRequestHeaderError(customContentException);
        assertEquals(wrappedMessage, ((ErrorResponse)responseEntity.getBody()).getErrorDescription());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void customConsequenceBadRequestError() {
        HttpMediaTypeNotAcceptableException customContentTypeException = mock(
            HttpMediaTypeNotAcceptableException.class);
        ResponseEntity<Object> responseEntity =
            csda.customConsequenceBadRequestError(new BadRequestException(BAD_REQUEST));
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void handleMethodArgumentNotValidException() {
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
        ResponseEntity<Object> responseEntity =
            csda.handleMethodArgumentNotValidException(servletRequestMock, methodArgumentNotValidException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException resourceNotFoundException =
            mock(ResourceNotFoundException.class);
        ResponseEntity<Object> responseEntity =
            csda.handleResourceNotFoundException(servletRequestMock,resourceNotFoundException);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void handleUnprocessableEntityException() {
        UnprocessableEntityException unprocessableEntityException =
            mock(UnprocessableEntityException.class);
        ResponseEntity<Object> responseEntity =
            csda.handleUnProcessableEntityExcepton(servletRequestMock,unprocessableEntityException);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), responseEntity.getStatusCode().value());
    }


    @Test
    void handleHttpMessageConversionException() {
        HttpMessageConversionException httpMessageConversionException =
            mock(HttpMessageConversionException.class);
        ResponseEntity<Object> responseEntity =
            csda.handleHttpMessageConversionException(servletRequestMock, httpMessageConversionException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
    }

    @Test
    void handleUnknownException() {
        Exception exception = mock(Exception.class);
        ResponseEntity<Object> responseEntity = csda.handleUnknownException(servletRequestMock, exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getStatusCode().value());

    }

    @Test
    void getTimeStamp() {
        String time = csda.getTimeStamp();
        assertEquals(time.substring(0,16), new SimpleDateFormat("dd-MM-yyyy HH:mm",
                                                                Locale.ENGLISH).format(new Date()));
    }

    @Test
    void notReadableException_RoleType() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(ROLETYPE, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(ROLETYPE));
    }

    @Test
    void notReadableException_Empty() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException("", mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        Assertions.assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().getErrorDescription().isEmpty());
    }

    @Test
    void notReadableException_UUID() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(UUID, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(UUID));
    }

    @Test
    void notReadableException_NotInDeserializedItems() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException("I AM NOT DESERIALIZED", mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertEquals("I AM NOT DESERIALIZED",
                     Objects.requireNonNull(responseEntity.getBody()).getErrorDescription());
    }

    @Test
    void notReadableException_Classification() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(CLASSIFICATION, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(CLASSIFICATION));
    }


    @Test
    void notReadableException_ActorIdType() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(ACTORIDTYPE, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(ACTORIDTYPE));
    }

    @Test
    void notReadableException_GrantType() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(GRANTTYPE, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(GRANTTYPE));
    }

    @Test
    void notReadableException_RoleCategory() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(ROLECATEGORY, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(ROLECATEGORY));
    }

    @Test
    void notReadableException_Boolean() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(BOOLEAN, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(BOOLEAN));
    }

    @Test
    void notReadableException_LocalDateTime() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(LOCALDATETIME, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(LOCALDATETIME));
    }

    @Test
    void notReadableException_Integer() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(INTEGER, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(INTEGER));
    }

    @Test
    void notReadableException_Status() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(STATUS, mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertTrue(Objects.requireNonNull(responseEntity.getBody()).getErrorDescription().contains(STATUS));
    }

    @Test
    void notReadableException_EmptyExceptionMessage() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException("", mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> responseEntity = csda.notReadableException(httpMessageNotReadableException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        Assertions.assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().getErrorDescription().contains(""));
    }

    @Test
    void notReadableException_nullExceptionMessage() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException(null, mock(HttpInputMessage.class)
        );
        Assertions.assertThrows(NullPointerException.class,
            () -> csda.notReadableException(httpMessageNotReadableException));
    }

    @Test
    void nullException() {
        NullPointerException nullPointerException = mock(NullPointerException.class);
        ResponseEntity<ErrorResponse> responseEntity = csda.nullException(nullPointerException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
    }
}
