package uk.gov.hmcts.reform.roleassignment.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.io.Resources;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentSubset;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfigRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Named
@Singleton
public class JacksonUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JacksonUtils.class);



    private JacksonUtils() {
    }

    @Getter
    private static final Map<String, List<RoleConfigRole>> configuredRoles = new HashMap<>();

    public static final JsonFactory jsonFactory = JsonFactory.builder()
        // Change per-factory setting to prevent use of `String.intern()` on symbols
        .disable(JsonFactory.Feature.INTERN_FIELD_NAMES)
        .build();

    public static final ObjectMapper MAPPER = JsonMapper.builder(jsonFactory)
        .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .build();


    public static Map<String, JsonNode> convertValue(Object from) {
        return MAPPER.convertValue(from, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }

    public static JsonNode convertValueJsonNode(Object from) {
        return MAPPER.convertValue(from, JsonNode.class);
    }

    public static TypeReference<HashMap<String, JsonNode>> getHashMapTypeReference() {
        return new TypeReference<HashMap<String, JsonNode>>() {
        };
    }

    //Find Subset for Incoming Records
    public static Set<RoleAssignmentSubset> convertRequestedRolesIntoSubSet(AssignmentRequest assignmentRequest)
        throws InvocationTargetException, IllegalAccessException {
        RoleAssignmentSubset subset;
        Set<RoleAssignmentSubset> roleAssignmentSubsets = new HashSet<>();
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            subset = RoleAssignmentSubset.builder().build();
            BeanUtils.copyProperties(subset, roleAssignment);
            roleAssignmentSubsets.add(subset);
        }

        return roleAssignmentSubsets;

    }

    //Find Subset for Existing  Records
    public static Map<UUID, RoleAssignmentSubset> convertExistingRolesIntoSubSet(AssignmentRequest assignmentRequest)
        throws InvocationTargetException, IllegalAccessException {
        RoleAssignmentSubset subset;
        Map<UUID, RoleAssignmentSubset> roleAssignmentSubsets = new HashMap<>();
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            subset = RoleAssignmentSubset.builder().build();
            BeanUtils.copyProperties(subset, roleAssignment);
            if (roleAssignment.getRoleType().equals(RoleType.CASE)) {
                //Remove the caseType and jurisdiction entries as it was added by application.
                subset.getAttributes().remove("jurisdiction");
                subset.getAttributes().remove("caseType");
            }
            roleAssignmentSubsets.put(roleAssignment.getId(), subset);
        }

        return roleAssignmentSubsets;

    }

    static {

        var listType = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(
            ArrayList.class,
            RoleConfigRole.class
        );
        List<RoleConfigRole> allRoles = new ArrayList<>();
        try {
            final String ROOT = "roleconfig";

            List<String> files = getRootPath2(ROOT);
            files.forEach(f -> {
                try {
                    allRoles.addAll(JacksonUtils.MAPPER.readValue(JacksonUtils.class.getClassLoader()
                                                  .getResourceAsStream(ROOT + File.separator + f), listType));
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            });

            Path dirPath = getRootPath("roleconfig");
            List<RoleConfigRole> allRoles1 = readfiles(listType, dirPath);
            allRoles.addAll(allRoles1);
            dirPath = getRootPath1("roleconfig");
            List<RoleConfigRole> allRoles2 = readfiles(listType, dirPath);
            allRoles.addAll(allRoles2);

        } catch (IOException | URISyntaxException e) {
            LOG.error(e.getMessage());
        }
        LOG.info("Loaded {} roles from drool", allRoles.size());
        configuredRoles.put("roles", allRoles);
    }

    private static List<RoleConfigRole> readfiles(com.fasterxml.jackson.databind.type.CollectionType listType,
                                                  Path dirPath) throws IOException {
        List<RoleConfigRole> allRoles = new ArrayList<>();
        Files.walk(dirPath).filter(Files::isRegularFile).forEach(f -> {
            try {
                allRoles.addAll(JacksonUtils.MAPPER.readValue(Files.newInputStream(f), listType));
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        });
        System.out.println(dirPath + "=====" + allRoles.size());
        return allRoles;
    }

    private static Path getRootPath(String root) throws URISyntaxException, IOException {
        URL url = JacksonUtils.class.getClassLoader().getResource(root);
        System.out.println("***Path:" + url);
        URI uri = url.toURI();
        System.out.println("Path.uri:" + uri);
        Path dirPath = FileSystems.getDefault().getPath(uri.getPath());
        System.out.println("dirPath:" + dirPath);
        return dirPath;
    }

    private static Path getRootPath1(String root) throws URISyntaxException, IOException {
        System.out.println("***Path:" + JacksonUtils.class.getClassLoader().getResource(root));
        URI uri = JacksonUtils.class.getClassLoader().getResource(root).toURI();
        System.out.println("Path.uri:" + uri);
        final String[] array = uri.toString().split("!");
        final FileSystem fs = array.length > 1 ? FileSystems.newFileSystem(URI.create(array[0]),new HashMap<>()) :
            FileSystems.getDefault();
        Path dirPath = fs.getPath(array[array.length - 1]);
        System.out.println("dirPath:" + dirPath);

        return dirPath;
    }

    private static List<String> getRootPath2(String root) throws IOException {
        URL url = JacksonUtils.class.getClassLoader().getResource(root);
        System.out.println("***Path:" + url);
        return Resources.readLines(url, Charset.defaultCharset());
    }
}
