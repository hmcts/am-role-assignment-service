package uk.gov.hmcts.reform.roleassignment.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;

import javax.inject.Named;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.DATE_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.NUMBER_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.NUMBER_TEXT_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.TEXT_HYPHEN_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.TEXT_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.UUID_PATTERN;

@Named
@Singleton
@Slf4j
public class ValidationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class);

    private ValidationUtil() {
    }


    public static boolean validateTextField(String field) {
        validateInputParams(TEXT_PATTERN, field);
        return (field != null);
    }

    public static boolean validateNumberTextField(String field) {
        validateInputParams(NUMBER_TEXT_PATTERN, field);
        return (field != null);
    }

    public static boolean validateTextHyphenField(String field) {
        validateInputParams(TEXT_HYPHEN_PATTERN, field);
        return (field != null);
    }

    public static void validateDateTime(String strDate) {
        if (strDate.length() < 16) {
            throw new BadRequestException(String.format(
                "Incorrect date format %s",
                strDate));
        }
        SimpleDateFormat sdfrmt = new SimpleDateFormat(DATE_PATTERN);
        sdfrmt.setLenient(false);
        try {
            Date javaDate = sdfrmt.parse(strDate);
        } catch (ParseException e) {
            throw new BadRequestException(String.format(
                "Incorrect date format %s",
                strDate));
        }
    }

    public static boolean validateDateOrder(String beginTime, String endTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        boolean result;
        try {
            Date beginTimeP = sdf.parse(beginTime);
            Date endTimeP = sdf.parse(endTime);
            // we don't need to pass the current time as it can be generated here.
            // this will allow us to call this validate order directly inside
            Date createTimeP = new Date();
            //Date createTimeP = sdf.parse(createTime);

            if (beginTimeP.before(createTimeP)) {
                throw new BadRequestException(
                    String.format("The begin time: %s takes place before the current time: %s",
                                  beginTime, createTimeP));
            } else if (endTimeP.before(createTimeP)) {
                throw new BadRequestException(
                    String.format("The end time: %s takes place before the current time: %s", endTime, createTimeP));
            } else if (endTimeP.before(beginTimeP)) {
                throw new BadRequestException(
                    String.format("The end time: %s takes place before the begin time: %s", endTime, beginTime));
            } else {
                result = true;
            }
        } catch (ParseException e) {
            result = false;
        }
        return result;
    }

    public static void isValidSecurityClassification(String securityClassification) {
        try {
            Enum.valueOf(Classification.class, securityClassification);
        } catch (final IllegalArgumentException ex) {
            LOG.info("The security classification is not valid");
            throw new BadRequestException("The security classification " + securityClassification + " is not valid");
        }
    }

    private static void validateInputParams(String pattern, String... inputString) {
        for (String input : inputString) {
            if (StringUtils.isEmpty(input)) {
                throw new BadRequestException("An input parameter is Null/Empty");
            } else if (!Pattern.matches(pattern, input)) {
                throw new BadRequestException("The input parameter: \"" + input + "\", does not comply with the "
                                              + "required pattern");
            }
        }
    }

    public static void validateLists(Collection<?>... inputList) {
        for (Collection<?> collection : inputList) {
            if (CollectionUtils.isEmpty(collection)) {
                throw new BadRequestException("The Collection is empty");
            }
        }
    }

    public static void validateAssignmentRequest(AssignmentRequest assignmentRequest) throws ParseException {
        validateRoleRequest(assignmentRequest.getRequest());
        validateLists(assignmentRequest.getRequestedRoles());
        validateRequestedRoles(assignmentRequest.getRequestedRoles());
    }

    public static void validateRoleRequest(Request roleRequest) {
        validateInputParams(UUID_PATTERN, roleRequest.assignerId.toString());
    }

    public static void validateRequestedRoles(Collection<RequestedRole> requestedRoles) throws ParseException {
        for (RequestedRole requestedRole : requestedRoles) {
            validateInputParams(UUID_PATTERN, requestedRole.getActorId().toString());
            if (requestedRole.getBeginTime() != null && requestedRole.getEndTime() != null) {
                validateDateTime(requestedRole.getBeginTime().toString());
                validateDateTime(requestedRole.getEndTime().toString());
                validateDateOrder(
                    requestedRole.getBeginTime().toString(),
                    requestedRole.getEndTime().toString());
            }
            validateInputParams(NUMBER_PATTERN, requestedRole.getAttributes().get("caseId").textValue());
        }
    }

}
