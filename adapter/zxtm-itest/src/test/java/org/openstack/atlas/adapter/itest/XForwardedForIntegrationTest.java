package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import java.rmi.RemoteException;

public class XForwardedForIntegrationTest extends ZeusTestBase {
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

    @Test
    public void testRemovingXFFRules() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        assertXffConfigurationIsPresent();
        shouldRemoveXffWhenUpdatingProtocolToNonHttp();
        shouldAddXffWhenUpdatingProtocolToNonHttp();
    }

    private void shouldRemoveXffWhenUpdatingProtocolToNonHttp() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        zxtmAdapter.updateProtocol(config, lb.getId(), lb.getAccountId(), LoadBalancerProtocol.SMTP);
        assertXffConfigurationIsRemoved();
    }

    private void shouldAddXffWhenUpdatingProtocolToNonHttp() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        zxtmAdapter.updateProtocol(config, lb.getId(), lb.getAccountId(), LoadBalancerProtocol.HTTP);
        assertXffConfigurationIsPresent();
    }

    private void assertXffConfigurationIsRemoved() throws RemoteException, InsufficientRequestException {
        final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(0, virtualServerRules[0].length);
    }

    private void assertXffConfigurationIsPresent() throws RemoteException, InsufficientRequestException {
        final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedFor, virtualServerRules[0][0]);
    }
}
