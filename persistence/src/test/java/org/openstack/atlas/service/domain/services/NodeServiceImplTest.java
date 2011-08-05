package org.openstack.atlas.service.domain.services;

import org.mockito.Matchers;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.NodeMap;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.services.impl.NodeServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class NodeServiceImplTest {

    public static class NodeOperations {
        Integer accountId = 1234;
        LoadBalancerRepository lbRepository;
        NodeRepository nodeRepository;
        NodeServiceImpl nodeService;
        LoadBalancer lb;
        LoadBalancer lb2;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> lbjvs;
        VirtualIp vip;
        Node node;
        Node node2;
        Set<Node> nodes;
        Set<Node> nodes2;

        @Before
        public void standUp() {
            lbRepository = mock(LoadBalancerRepository.class);
            nodeRepository = mock(NodeRepository.class);
            nodeService = new NodeServiceImpl();
            nodeService.setNodeRepository(nodeRepository);
            nodeService.setLoadBalancerRepository(lbRepository);
        }

        @Before
        public void standUpObjects() {
            lb = new LoadBalancer();
            lb2 = new LoadBalancer();
            lbjv = new LoadBalancerJoinVip();
            lbjvs = new HashSet<LoadBalancerJoinVip>();
            vip = new VirtualIp();
            node = new Node();
            node2 = new Node();
            nodes = new HashSet<Node>();
            nodes2 = new HashSet<Node>();

            node.setPort(12);
            node2.setPort(11);

            node.setId(12);
            node2.setId(10);

            node.setIpAddress("192.1.1.1");
            node2.setIpAddress("193.1.1.1");

            node.setCondition(NodeCondition.ENABLED);
            node2.setCondition(NodeCondition.DISABLED);

            nodes.add(node);
            nodes2.add(node2);

            lb.setNodes(nodes);
            lb2.setNodes(nodes2);

            vip.setIpAddress("192.3.3.3");
            lbjv.setVirtualIp(vip);
            lbjvs.add(lbjv);
            lb.setLoadBalancerJoinVipSet(lbjvs);
        }

        @Test
        public void shouldReturnFalseWhenDuplicateNodesDetected() throws EntityNotFoundException {
            Assert.assertFalse(nodeService.detectDuplicateNodes(lb, lb2));
        }

        @Test
        public void shouldReturnTrueWhenDuplicateNodesDetected() throws EntityNotFoundException {
            node2.setIpAddress("192.1.1.1");
            node2.setPort(12);
            lb2.getNodes().add(node2);
            Assert.assertTrue(nodeService.detectDuplicateNodes(lb, lb2));
        }

        @Test
        public void shouldAllowValidIps() {
            Assert.assertTrue(nodeService.areAddressesValidForUse(nodes, lb));
        }

        @Test
        public void shouldNotAllowInvalidIps() {
            node2.setIpAddress("192.3.3.3");
            nodes2.add(node2);
            Assert.assertFalse(nodeService.areAddressesValidForUse(nodes2, lb));
        }

        public static class WhenUpdatingNodes {
            Integer accountId = 1234;
            LoadBalancerRepository lbRepository;
            NodeRepository nodeRepository;
            NodeServiceImpl nodeService;
            LoadBalancer lb;
            LoadBalancer lb2;
            LoadBalancerJoinVip lbjv;
            Set<LoadBalancerJoinVip> lbjvs;
            VirtualIp vip;
            Node node;
            Node node2;
            Set<Node> nodes;
            Set<Node> nodes2;

            @Before
            public void standUp() {
                lbRepository = mock(LoadBalancerRepository.class);
                nodeRepository = mock(NodeRepository.class);
                nodeService = new NodeServiceImpl();
                nodeService.setNodeRepository(nodeRepository);
                nodeService.setLoadBalancerRepository(lbRepository);
            }

            @Before
            public void standUpObjects() {
                lb = new LoadBalancer();
                lb2 = new LoadBalancer();
                lbjv = new LoadBalancerJoinVip();
                lbjvs = new HashSet<LoadBalancerJoinVip>();
                vip = new VirtualIp();
                node = new Node();
                node2 = new Node();
                nodes = new HashSet<Node>();
                nodes2 = new HashSet<Node>();

                node.setPort(12);
                node2.setPort(11);

                node.setId(12);
                node2.setId(10);

                node.setIpAddress("192.1.1.1");
                node2.setIpAddress("193.1.1.1");

                node.setCondition(NodeCondition.ENABLED);
                node2.setCondition(NodeCondition.DISABLED);

                nodes.add(node);
                nodes2.add(node2);

                lb.setNodes(nodes);
                lb2.setNodes(nodes2);

                vip.setIpAddress("192.3.3.3");
                lbjv.setVirtualIp(vip);
                lbjvs.add(lbjv);
                lb.setLoadBalancerJoinVipSet(lbjvs);
            }

            @Test(expected = BadRequestException.class)
            public void shouldFailIfUpdatingLastPrimaryToFailOver() throws UnprocessableEntityException {
                node.setType(NodeType.PRIMARY);
                Node requestNode = new Node();
                requestNode.setType(NodeType.FAIL_OVER);

                nodeService.verifyNodeTypeIsNotLastPrimary(requestNode, node, lb);
            }

            @Test
            public void shouldMapUpdatedValuesAndLeaveOthersAlone() throws EntityNotFoundException, BadRequestException, ImmutableEntityException, UnprocessableEntityException {
                node.setType(NodeType.PRIMARY);
                node2.setType(NodeType.FAIL_OVER);
                node2.setWeight(3000);
                node2.setCondition(NodeCondition.ENABLED);
                nodes2.add(node2);
                lb.getNodes().addAll(nodes);
                lb.getNodes().addAll(nodes2);
                lb.setStatus(LoadBalancerStatus.ACTIVE);

                Node node3 = new Node();
                Node node4 = new Node();
                Set<Node> nodes3 = new HashSet<Node>();
                Set<Node> nodes4 = new HashSet<Node>();
                LoadBalancer dbLb = new LoadBalancer();

                node3.setType(NodeType.PRIMARY);
                node3.setCondition(NodeCondition.ENABLED);
                node3.setId(12);
                node3.setIpAddress("172.1.1.1");
                node3.setPort(79);
                node3.setWeight(299);

                node4.setType(NodeType.FAIL_OVER);
                node4.setCondition(NodeCondition.ENABLED);
                node4.setId(10);
                node4.setIpAddress("172.1.1.3");
                node4.setPort(29);
                node4.setWeight(288);

                nodes3.add(node3);
                nodes4.add(node4);

                dbLb.getNodes().addAll(nodes3);
                dbLb.getNodes().addAll(nodes4);
                dbLb.setStatus(LoadBalancerStatus.ACTIVE);

                when(lbRepository.getByIdAndAccountId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(dbLb);
                nodeService.updateNode(lb);

                Assert.assertEquals((Object) 11, node2.getPort());
                Assert.assertEquals((Object) 10, node2.getId());
                Assert.assertEquals("193.1.1.1", node2.getIpAddress());
                Assert.assertEquals(NodeCondition.ENABLED, node2.getCondition());
                Assert.assertEquals((Object) 3000, node2.getWeight());
                Assert.assertEquals(NodeType.FAIL_OVER, node2.getType());
            }
        }

        @Test
        public void shouldFailWhenUpdatingLastPrimaryToFailOver() throws EntityNotFoundException, BadRequestException, ImmutableEntityException, UnprocessableEntityException {
            node.setType(NodeType.FAIL_OVER);
            lb.getNodes().addAll(nodes);
            lb.setStatus(LoadBalancerStatus.ACTIVE);

            Node node3 = new Node();
            Set<Node> nodes3 = new HashSet<Node>();
            LoadBalancer dbLb = new LoadBalancer();

            node3.setType(NodeType.PRIMARY);
            node3.setCondition(NodeCondition.ENABLED);
            node3.setId(12);
            node3.setIpAddress("172.1.1.1");
            node3.setPort(79);
            node3.setWeight(299);

            nodes3.add(node3);

            dbLb.getNodes().addAll(nodes3);
            dbLb.setStatus(LoadBalancerStatus.ACTIVE);

            when(lbRepository.getByIdAndAccountId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(dbLb);

            try {
                nodeService.updateNode(lb);
            } catch (UnprocessableEntityException e) {
                Assert.assertEquals(UnprocessableEntityException.class, e.getClass());
                Assert.assertEquals(NodeType.PRIMARY, dbLb.getNodes().iterator().next().getType());
            }
        }
    }

    public static class WhenDeletingNodes {
        Integer accountId = 1234;
        LoadBalancerRepository lbRepository;
        NodeRepository nodeRepository;
        NodeServiceImpl nodeService;
        LoadBalancer lb;
        LoadBalancer lb2;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> lbjvs;
        VirtualIp vip;
        Node node;
        Node node2;
        Set<Node> nodes;
        Set<Node> nodes2;

        @Before
        public void standUp() {
            lbRepository = mock(LoadBalancerRepository.class);
            nodeRepository = mock(NodeRepository.class);
            nodeService = new NodeServiceImpl();
            nodeService.setNodeRepository(nodeRepository);
            nodeService.setLoadBalancerRepository(lbRepository);
        }

        @Before
        public void standUpObjects() {
            lb = new LoadBalancer();
            lb2 = new LoadBalancer();
            lbjv = new LoadBalancerJoinVip();
            lbjvs = new HashSet<LoadBalancerJoinVip>();
            vip = new VirtualIp();
            node = new Node();
            node2 = new Node();
            nodes = new HashSet<Node>();
            nodes2 = new HashSet<Node>();

            node.setPort(12);
            node2.setPort(11);

            node.setId(12);
            node2.setId(10);

            node.setIpAddress("192.1.1.1");
            node2.setIpAddress("193.1.1.1");

            node.setCondition(NodeCondition.ENABLED);
            node2.setCondition(NodeCondition.DISABLED);

            nodes.add(node);
            nodes2.add(node2);

            lb.setNodes(nodes);
            lb2.setNodes(nodes2);

            vip.setIpAddress("192.3.3.3");
            lbjv.setVirtualIp(vip);
            lbjvs.add(lbjv);
            lb.setLoadBalancerJoinVipSet(lbjvs);
        }

        @Test
        public void shouldReturnFalseIfNotLastActivePrimary() {
            node2.setCondition(NodeCondition.ENABLED);
            node2.setType(NodeType.PRIMARY);
            lb.addNode(node2);
            Assert.assertFalse(nodeService.isLastEnabledPrimaryNode(lb, node));
        }

        @Test
        public void shouldReturnTrueIfLastActivePrimary() {
            Assert.assertTrue(nodeService.isLastEnabledPrimaryNode(lb, node));
        }

        @Test
        public void shouldFailIfBatchDeletingLastPrimary() throws BadRequestException {
            node.setType(NodeType.PRIMARY);
            node2.setType(NodeType.FAIL_OVER);
            NodeMap nodeMap = new NodeMap();
            nodeMap.addNode(node);
            nodeMap.addNode(node2);
            Set<Integer> idsToDelete = new HashSet<Integer>();
            idsToDelete.add(node.getId());

            Set<Integer> primaryNodesLeft = nodeMap.nodesInTypeAfterDelete(NodeType.PRIMARY, idsToDelete);
            Assert.assertFalse(primaryNodesLeft.size() >= 1);
        }

        @Test
        public void shouldPassBatchDeletingIfNotLastPrimary() throws BadRequestException {
            node.setType(NodeType.PRIMARY);
            node2.setType(NodeType.FAIL_OVER);
            NodeMap nodeMap = new NodeMap();
            nodeMap.addNode(node);
            nodeMap.addNode(node2);
            Set<Integer> idsToDelete = new HashSet<Integer>();
            idsToDelete.add(node2.getId());

            Set<Integer> primaryNodesLeft = nodeMap.nodesInTypeAfterDelete(NodeType.PRIMARY, idsToDelete);
            Assert.assertTrue(primaryNodesLeft.size() >= 1);
        }

        @Test
        public void shouldFailWhenDeletingLastPrimaryToFailOver() throws EntityNotFoundException, BadRequestException, ImmutableEntityException, UnprocessableEntityException {
            node.setType(NodeType.FAIL_OVER);
            lb.getNodes().addAll(nodes);
            lb.setStatus(LoadBalancerStatus.ACTIVE);

            Node node3 = new Node();
            Set<Node> nodes3 = new HashSet<Node>();
            LoadBalancer dbLb = new LoadBalancer();

            node3.setType(NodeType.PRIMARY);
            node3.setCondition(NodeCondition.ENABLED);
            node3.setId(12);
            node3.setIpAddress("172.1.1.1");
            node3.setPort(79);
            node3.setWeight(299);

            nodes3.add(node3);

            dbLb.getNodes().addAll(nodes3);
            dbLb.setStatus(LoadBalancerStatus.ACTIVE);

            when(lbRepository.getByIdAndAccountId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(dbLb);

            try {
                nodeService.deleteNode(lb);
            } catch (UnprocessableEntityException e) {
                Assert.assertEquals(UnprocessableEntityException.class, e.getClass());
                Assert.assertEquals(NodeType.PRIMARY, dbLb.getNodes().iterator().next().getType());
            }
        }
    }
}
