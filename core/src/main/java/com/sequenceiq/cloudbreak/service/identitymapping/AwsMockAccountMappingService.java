package com.sequenceiq.cloudbreak.service.identitymapping;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.IdentityService;
import com.sequenceiq.cloudbreak.cloud.init.CloudPlatformConnectors;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.common.exception.CloudbreakServiceException;
import com.sequenceiq.cloudbreak.converter.spi.CredentialToCloudCredentialConverter;
import com.sequenceiq.cloudbreak.dto.credential.Credential;

@Component
public class AwsMockAccountMappingService {

    private static final String FIXED_IAM_ROLE = "arn:aws:iam::${accountId}:role/mock-idbroker-admin-role";

    private static final Map<String, String> MOCK_IDBROKER_USER_MAPPINGS = Map.ofEntries(
            Map.entry("accumulo", FIXED_IAM_ROLE),
            Map.entry("ambari-qa", FIXED_IAM_ROLE),
            Map.entry("ams", FIXED_IAM_ROLE),
            Map.entry("atlas", FIXED_IAM_ROLE),
            Map.entry("docker", FIXED_IAM_ROLE),
            Map.entry("falcon", FIXED_IAM_ROLE),
            Map.entry("flume", FIXED_IAM_ROLE),
            Map.entry("hbase", FIXED_IAM_ROLE),
            Map.entry("hcat", FIXED_IAM_ROLE),
            Map.entry("hdfs", FIXED_IAM_ROLE),
            Map.entry("hive", FIXED_IAM_ROLE),
            Map.entry("httpfs", FIXED_IAM_ROLE),
            Map.entry("hue", FIXED_IAM_ROLE),
            Map.entry("impala", FIXED_IAM_ROLE),
            Map.entry("infra-solr", FIXED_IAM_ROLE),
            Map.entry("kafka", FIXED_IAM_ROLE),
            Map.entry("kms", FIXED_IAM_ROLE),
            Map.entry("knox", FIXED_IAM_ROLE),
            Map.entry("kudu", FIXED_IAM_ROLE),
            Map.entry("livy", FIXED_IAM_ROLE),
            Map.entry("mahout", FIXED_IAM_ROLE),
            Map.entry("mapred", FIXED_IAM_ROLE),
            Map.entry("oozie", FIXED_IAM_ROLE),
            Map.entry("ranger", FIXED_IAM_ROLE),
            Map.entry("sentry", FIXED_IAM_ROLE),
            Map.entry("slider", FIXED_IAM_ROLE),
            Map.entry("solr", FIXED_IAM_ROLE),
            Map.entry("spark", FIXED_IAM_ROLE),
            Map.entry("sqoop", FIXED_IAM_ROLE),
            Map.entry("storm", FIXED_IAM_ROLE),
            Map.entry("tez", FIXED_IAM_ROLE),
            Map.entry("yarn", FIXED_IAM_ROLE),
            Map.entry("yarn-ats", FIXED_IAM_ROLE),
            Map.entry("ycloudadm", FIXED_IAM_ROLE),
            Map.entry("zeppelin", FIXED_IAM_ROLE),
            Map.entry("zookeeper", FIXED_IAM_ROLE)
    );

    @Inject
    private CloudPlatformConnectors cloudPlatformConnectors;

    @Inject
    private CredentialToCloudCredentialConverter credentialConverter;

    public Map<String, String> getGroupMappings(String region, Credential credential, String adminGroupName) {
        String accountId = getAccountId(region, credential);
        if (StringUtils.isNotEmpty(adminGroupName)) {
            return replaceAccountId(getGroupMappings(adminGroupName), accountId);
        } else {
            throw new CloudbreakServiceException(String.format("Failed to get group mappings because of missing adminGroupName for accountId: %s",
                    accountId));
        }
    }

    public Map<String, String> getUserMappings(String region, Credential credential) {
        String accountId = getAccountId(region, credential);
        return replaceAccountId(MOCK_IDBROKER_USER_MAPPINGS, accountId);
    }

    private String getAccountId(String region, Credential credential) {
        IdentityService identityService = getIdentityService(credential.cloudPlatform());
        return identityService.getAccountId(region, credentialConverter.convert(credential));
    }

    private IdentityService getIdentityService(String platform) {
        return cloudPlatformConnectors.get(Platform.platform(platform), Variant.variant(platform)).identityService();
    }

    private Map<String, String> replaceAccountId(Map<String, String> mapping, String accountId) {
        return mapping.entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().replace("${accountId}", accountId)))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Map<String, String> getGroupMappings(String adminGroupName) {
        return Map.ofEntries(
                Map.entry(adminGroupName, FIXED_IAM_ROLE)
        );
    }

}
