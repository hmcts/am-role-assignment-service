package uk.gov.hmcts.reform.roleassignment.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType.CASE;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.NUMBER_PATTERN;

import javax.inject.Named;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Named
@Singleton
@Slf4j
public class ValidationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class);

    private ValidationUtil() {
    }

    public static void validateDateTime(String strDate) {
        LOG.info("validateDateTime");
        if (strDate.length() < 16) {
            throw new BadRequestException(String.format(
                "Incorrect date format %s",
                strDate
            ));
        }
        SimpleDateFormat sdfrmt = new SimpleDateFormat(Constants.DATE_PATTERN);
        sdfrmt.setLenient(false);
        try {
            Date javaDate = sdfrmt.parse(strDate);
            if (LOG.isInfoEnabled() && javaDate != null) {
                LOG.info(javaDate.toString());
            }
        } catch (ParseException e) {
            throw new BadRequestException(String.format(
                "Incorrect date format %s",
                strDate
            ));
        }
    }

    public static void validateDateOrder(String beginTime, String endTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_PATTERN);
        Date beginTimeP = sdf.parse(beginTime);
        Date endTimeP = sdf.parse(endTime);
        Date createTimeP = new Date();

        if (beginTimeP.before(createTimeP)) {
            throw new BadRequestException(
                String.format("The begin time: %s takes place before the current time: %s",
                              beginTime, createTimeP
                ));
        } else if (endTimeP.before(createTimeP)) {
            throw new BadRequestException(
                String.format("The end time: %s takes place before the current time: %s", endTime, createTimeP));
        } else if (endTimeP.before(beginTimeP)) {
            throw new BadRequestException(
                String.format("The end time: %s takes place before the begin time: %s", endTime, beginTime));
        }
    }

    public static void validateInputParams(String pattern, String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            throw new BadRequestException("An input parameter is Null/Empty");
        } else if (!Pattern.matches(pattern, inputString)) {
            throw new BadRequestException(
                String.format("The input parameter: \"%s\", does not comply with the required pattern", inputString));
        }
    }

    public static void validateEnumRoleType(String roleType) {
        for (RoleType realRole : RoleType.values()) {
            if (roleType != null) {
                if (realRole.name().equalsIgnoreCase(roleType)) {
                    break;
                }
            } else {
                throw new BadRequestException(
                    String.format("The Role Type parameter supplied: %s is not valid", roleType));
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
        if (roleRequest.isReplaceExisting()
            && (((roleRequest.getProcess() == null || roleRequest.getProcess().isEmpty())
            && (roleRequest.getReference() == null || roleRequest.getReference().isEmpty()))
            || ((roleRequest.getProcess() != null || !roleRequest.getProcess().isEmpty())
            && (roleRequest.getReference() == null || roleRequest.getReference().isEmpty()))
            || ((roleRequest.getProcess() == null || roleRequest.getProcess().isEmpty())
            && (roleRequest.getReference() != null || !roleRequest.getReference().isEmpty()))
            )) {
            throw new BadRequestException(V1.Error.BAD_REQUEST_MISSING_PARAMETERS);
        }
        validateInputParams(Constants.UUID_PATTERN, roleRequest.getAssignerId().toString());
    }

    public static void validateRequestedRoles(Collection<RoleAssignment> requestedRoles) throws ParseException {
        List<String> rolesName = JacksonUtils.getConfiguredRoles().get("roles").stream().map(Role::getName).collect(
            Collectors.toList());
        for (RoleAssignment requestedRole : requestedRoles) {
            if (!rolesName.contains(requestedRole.getRoleName())) {
                throw new BadRequestException(V1.Error.BAD_REQUEST_INVALID_PARAMETER + " roleName :"
                                                  + requestedRole.getRoleName());
            }

            validateInputParams(Constants.UUID_PATTERN, requestedRole.getActorId().toString());
            validateEnumRoleType(requestedRole.getRoleType().toString());
            if (requestedRole.getBeginTime() != null && requestedRole.getEndTime() != null) {
                validateDateTime(requestedRole.getBeginTime().toString());
                validateDateTime(requestedRole.getEndTime().toString());
                validateDateOrder(
                    requestedRole.getBeginTime().toString(),
                    requestedRole.getEndTime().toString()
                );
            }
            if(requestedRole.getRoleType().equals(CASE)) {
                validateInputParams(Constants.NUMBER_PATTERN, requestedRole.getAttributes().get("caseId").textValue());
            }
        }
    }

    public static void validateCaseId(String caseId) {
        validateInputParams(NUMBER_PATTERN, caseId);
        if (caseId.length() != 16) {
            throw new BadRequestException(V1.Error.INVALID_CASE_ID);
        }
    }
}
