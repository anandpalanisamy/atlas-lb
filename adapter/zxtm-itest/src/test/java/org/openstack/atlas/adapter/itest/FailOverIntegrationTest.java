package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeType;

import java.rmi.RemoteException;

import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

public class FailOverIntegrationTest extends ZeusTestBase {
    protected static Node failOverNode1;
    protected static Node failOverNode2;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        setupSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    protected static void setupIvars() {
        ZeusTestBase.setupIvars();
        failOverNode1 = new Node();
        failOverNode1.setIpAddress("128.0.0.1");
        failOverNode1.setPort(80);
        failOverNode1.setCondition(ENABLED);
        failOverNode1.setType(NodeType.SECONDARY);

        failOverNode2 = new Node();
        failOverNode2.setIpAddress("128.0.0.2");
        failOverNode2.setPort(81);
        failOverNode2.setCondition(DISABLED);
        failOverNode2.setType(NodeType.SECONDARY);
    }

    protected static String failOverPoolName() throws InsufficientRequestException {
        return ZxtmNameBuilder.generatePoolName(lb.getId(), lb.getAccountId(), true);
    }

    @Test
    public void testSimpleFailOverOperations() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        shouldAddFailOverPoolWhenAddingASecondaryNode();
//        shouldRemoveFailOverPoolWhenRemovingAllSecondaryNodes();
    }

    private void shouldAddFailOverPoolWhenAddingASecondaryNode() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        lb.getNodes().add(failOverNode1);
        lb.getNodes().add(failOverNode2);

        zxtmAdapter.setNodes(config, lb.getId(), lb.getAccountId(), lb.getNodes());
        assertFailOverSettings();
    }

    private void shouldRemoveFailOverPoolWhenRemovingAllSecondaryNodes() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        lb.getNodes().remove(failOverNode1);
        lb.getNodes().remove(failOverNode2);

        zxtmAdapter.setNodes(config, lb.getId(), lb.getAccountId(), lb.getNodes());

        final String[] failPools = getServiceStubs().getPoolBinding().getFailpool(new String[]{poolName()});
        Assert.assertEquals(0, failPools.length);
    }

    private void assertFailOverSettings() throws RemoteException, InsufficientRequestException {
        final int defaultNodeWeight = 1;
        String failOverNode1ZeusString = IpHelper.createZeusIpString(failOverNode1.getIpAddress(), failOverNode1.getPort());
        String failOverNode2ZeusString = IpHelper.createZeusIpString(failOverNode2.getIpAddress(), failOverNode2.getPort());

        final String[] failPools = getServiceStubs().getPoolBinding().getFailpool(new String[]{poolName()});
        Assert.assertEquals(1, failPools.length);
        Assert.assertEquals(failOverPoolName(), failPools[0]);

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{failOverPoolName()});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(1, enabledNodes[0].length);
        Assert.assertEquals(failOverNode1ZeusString, enabledNodes[0][0]);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{failOverPoolName()});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(1, disabledNodes[0].length);
        Assert.assertEquals(failOverNode2ZeusString, disabledNodes[0][0]);

        final PoolWeightingsDefinition[][] weightingsDefinitions = getServiceStubs().getPoolBinding().getWeightings(new String[]{failOverPoolName()});
        Assert.assertEquals(1, weightingsDefinitions.length);
        Assert.assertEquals(2, weightingsDefinitions[0].length);

        for (PoolWeightingsDefinition weightingsDefinition : weightingsDefinitions[0]) {
            if (weightingsDefinition.getNode().equals(failOverNode1ZeusString))
                Assert.assertEquals(defaultNodeWeight, weightingsDefinition.getWeighting());
            else if (weightingsDefinition.getNode().equals(failOverNode2ZeusString))
                Assert.assertEquals(defaultNodeWeight, weightingsDefinition.getWeighting());
            else Assert.fail("Unrecognized node weighting definition.");
        }
    }

    @Test
    public void shouldAddFailOverPoolWhenCreatingALoadBalancerWithSecondaryNodes() {
        try {
            removeLoadBalancer();
            lb.getNodes().add(failOverNode1);
            lb.getNodes().add(failOverNode2);
            zxtmAdapter.createLoadBalancer(config, lb);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.http, virtualServerBasicInfos[0].getProtocol());
            Assert.assertEquals(lb.getPort().intValue(), virtualServerBasicInfos[0].getPort());
            Assert.assertEquals(poolName(), virtualServerBasicInfos[0].getDefault_pool());

            String trafficIpGroupName = trafficIpGroupName(lb.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp());

            final String[][] trafficManagers = getServiceStubs().getTrafficIpGroupBinding().getTrafficManager(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, trafficManagers.length);
            Assert.assertEquals(3, trafficManagers[0].length);

            final String[][] vips = getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, vips.length);
            Assert.assertEquals(1, vips[0].length);
            Assert.assertEquals(vip1.getIpAddress(), vips[0][0]);

            final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
            Assert.assertEquals(1, enabledNodes.length);
            Assert.assertEquals(1, enabledNodes[0].length);
            Assert.assertEquals(IpHelper.createZeusIpString(node1.getIpAddress(), node1.getPort()), enabledNodes[0][0]);

            final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
            Assert.assertEquals(1, disabledNodes.length);
            Assert.assertEquals(1, disabledNodes[0].length);
            Assert.assertEquals(IpHelper.createZeusIpString(node2.getIpAddress(), node2.getPort()), disabledNodes[0][0]);

            final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});
            Assert.assertEquals(1, drainingNodes.length);
            Assert.assertEquals(0, drainingNodes[0].length);

            final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, enabledNodes);
            Assert.assertEquals(1, enabledNodeWeights.length);
            Assert.assertEquals(1, enabledNodeWeights[0].length);
            Assert.assertEquals(1, enabledNodeWeights[0][0].getWeighting());

            final PoolWeightingsDefinition[][] disabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, disabledNodes);
            Assert.assertEquals(1, disabledNodeWeights.length);
            Assert.assertEquals(1, disabledNodeWeights[0].length);
            Assert.assertEquals(1, disabledNodeWeights[0][0].getWeighting());

            final PoolWeightingsDefinition[][] drainingNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, drainingNodes);
            Assert.assertEquals(1, drainingNodeWeights.length);
            Assert.assertEquals(0, drainingNodeWeights[0].length);

            final PoolLoadBalancingAlgorithm[] algorithms = getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName()});
            Assert.assertEquals(1, algorithms.length);
            Assert.assertEquals(PoolLoadBalancingAlgorithm.roundrobin.toString(), algorithms[0].getValue());

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedFor, virtualServerRules[0][0]);

            assertFailOverSettings();

            // Remove so later tests aren't affected
            lb.getNodes().remove(failOverNode1);
            lb.getNodes().remove(failOverNode2);
            removeLoadBalancer();
        } catch (ObjectDoesNotExist odne) {
            // Ignore
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
