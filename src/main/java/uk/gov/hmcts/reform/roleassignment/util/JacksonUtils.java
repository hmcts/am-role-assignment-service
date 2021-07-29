package uk.gov.hmcts.reform.roleassignment.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Named
@Singleton
public class JacksonUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JacksonUtils.class);

    private JacksonUtils() {
    }

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


    public static List<RoleConfigRole> getConfiguredRoles() {
        return configuredRoles.get("roles");
    }

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
        List<RoleConfigRole> allRoles = getRoleConfigs();
        configuredRoles.put("roles", allRoles);
    }

    public static List<RoleConfigRole> getRoleConfigs() {
        var listType = MAPPER.getTypeFactory().constructCollectionType(
            ArrayList.class,
            RoleConfigRole.class
        );

        List<RoleConfigRole> allRoles = new ArrayList<>();
        try {
            Path dirPath = getAbsolutePath(Constants.ROLES_DIR);
            LOG.info("Roles absolute path is {}", dirPath);

            Files.walk(dirPath).filter(Files::isRegularFile).sorted().forEachOrdered(f -> {
                try {
                    LOG.debug("Reading role {}", f);
                    allRoles.addAll(MAPPER.readValue(Files.newInputStream(f), listType));
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            });

        } catch (IOException | URISyntaxException e) {
            LOG.error(e.getMessage());
        }

        LOG.info("Loaded {} roles from drool", allRoles.size());
        return allRoles;
    }

    private static List<Path> listFilesInOrder(final Path dirPath) throws IOException {

        try (final Stream<Path> fileStream = Files.walk(dirPath).filter(Files::isRegularFile)) {
            //fileStream.map(Path::toFile).sorted().collect(Collectors.toList()).forEach(System.out::println);
            fileStream.sorted().collect(Collectors.toList()).forEach(System.out::println);
            return Files.walk(dirPath)
                .map(Path::toFile)
                .collect(Collectors.toMap(Function.identity(), File::getName))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(File::toPath)  // remove this line if you would rather work with a List<File> instead of List<Path>
                .collect(Collectors.toList());
        }
    }

    private static Path getAbsolutePath(String path) throws URISyntaxException, IOException {
        URI uri = JacksonUtils.class.getClassLoader().getResource(path).toURI();
        LOG.debug("Filtering ROOT dir {}", uri);

        final String[] array = uri.toString().split("!");
        return array.length > 1 ? FileSystems.newFileSystem(URI.create(array[0]), new HashMap<>())
            .getPath(array[1], Arrays.copyOfRange(array, 2, array.length)) : Paths.get(uri);
    }

}
