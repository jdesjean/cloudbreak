package com.sequenceiq.cloudbreak.converter.stack.cluster;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.core.convert.ConversionService;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.ClusterV4Request;
import com.sequenceiq.cloudbreak.cloud.model.component.StackType;
import com.sequenceiq.cloudbreak.converter.AbstractJsonConverterTest;
import com.sequenceiq.cloudbreak.converter.util.CloudStorageValidationUtil;
import com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.CloudStorageConverter;
import com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.ClusterV4RequestToClusterConverter;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.cluster.gateway.Gateway;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintService;
import com.sequenceiq.cloudbreak.service.workspace.WorkspaceService;
import com.sequenceiq.cloudbreak.workspace.model.Workspace;

@RunWith(MockitoJUnitRunner.class)
public class ClusterRequestToClusterConverterTest extends AbstractJsonConverterTest<ClusterV4Request> {

    @InjectMocks
    private ClusterV4RequestToClusterConverter underTest;

    @Mock
    private ConversionService conversionService;

    @Mock
    private CloudStorageValidationUtil cloudStorageValidationUtil;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private BlueprintService blueprintService;

    @Mock
    private Workspace workspace;

    @Mock
    private CloudStorageConverter cloudStorageConverter;

    @Before
    public void setUp() {
        Whitebox.setInternalState(underTest, "ambariUserName", "cloudbreak");
        when(workspaceService.getForCurrentUser()).thenReturn(workspace);
    }

    @Test
    public void testConvert() {
        // GIVEN
        ClusterV4Request request = getRequest("cluster.json");

        Blueprint blueprint = new Blueprint();
        blueprint.setStackType(StackType.HDP.name());
        given(blueprintService.getByNameForWorkspaceAndLoadDefaultsIfNecessary(eq("my-blueprint"), any())).willReturn(blueprint);
        given(conversionService.convert(request.getGateway(), Gateway.class)).willReturn(new Gateway());
        // WHEN
        Cluster result = underTest.convert(request);
        // THEN
        assertAllFieldsNotNull(result, Arrays.asList("stack", "blueprint", "creationStarted", "creationFinished", "upSince", "statusReason", "clusterManagerIp",
                "fileSystem", "rdsConfigs", "attributes", "uptime", "ambariSecurityMasterKey", "proxyConfigCrn",
                "extendedBlueprintText", "environmentCrn", "variant", "description", "databaseServerCrn"));
    }

    @Test
    public void testConvertWithCloudStorageDetails() {
        // GIVEN
        ClusterV4Request request = getRequest("cluster-with-cloud-storage.json");

        given(conversionService.convert(request.getGateway(), Gateway.class)).willReturn(new Gateway());
        given(cloudStorageConverter.requestToFileSystem(request.getCloudStorage())).willReturn(new FileSystem());
        given(cloudStorageValidationUtil.isCloudStorageConfigured(request.getCloudStorage())).willReturn(true);
        Blueprint blueprint = new Blueprint();
        blueprint.setStackType(StackType.HDP.name());
        given(blueprintService.getByNameForWorkspaceAndLoadDefaultsIfNecessary(eq("my-blueprint"), any())).willReturn(blueprint);
        // WHEN
        Cluster result = underTest.convert(request);
        // THEN
        assertAllFieldsNotNull(result, Arrays.asList("stack", "blueprint", "creationStarted", "creationFinished", "upSince", "statusReason", "clusterManagerIp",
                "rdsConfigs", "attributes", "uptime", "ambariSecurityMasterKey", "proxyConfigCrn", "extendedBlueprintText",
                "environmentCrn", "variant", "description", "databaseServerCrn"));
    }

    @Test
    public void testNoGateway() {
        // GIVEN
        Blueprint blueprint = new Blueprint();
        blueprint.setStackType(StackType.HDP.name());
        given(blueprintService.getByNameForWorkspaceAndLoadDefaultsIfNecessary(eq("my-blueprint"), any())).willReturn(blueprint);
        // WHEN
        ClusterV4Request clusterRequest = getRequest("cluster-no-gateway.json");
        Cluster result = underTest.convert(clusterRequest);
        // THEN
        assertAllFieldsNotNull(result, Arrays.asList("stack", "blueprint", "creationStarted", "creationFinished", "upSince", "statusReason", "clusterManagerIp",
                "fileSystem", "rdsConfigs", "attributes", "uptime", "ambariSecurityMasterKey", "proxyConfigCrn",
                "extendedBlueprintText", "gateway", "environmentCrn", "variant", "description", "databaseServerCrn"));
        assertNull(result.getGateway());
    }

    @Override
    public Class<ClusterV4Request> getRequestClass() {
        return ClusterV4Request.class;
    }
}
