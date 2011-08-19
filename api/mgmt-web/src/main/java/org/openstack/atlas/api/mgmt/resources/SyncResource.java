package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.SyncLocation;
import org.openstack.atlas.util.common.ListUtil;

import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class SyncResource extends ManagementDependencyProvider {

    private List<String> loadBalancerIds;

    @PUT
    public Response sync() {
        List<String> badIds = new ArrayList<String>();
        for (String id : loadBalancerIds) {
            try {
                Integer integer = Integer.parseInt(id);
                loadBalancerService.get(integer).getId();
            } catch (Exception e) {
                badIds.add(id);
            }
        }

        if (badIds.size() != 0) {
            String output = ListUtil.generateCommaSeparatedString(badIds);
            return Response.status(400).entity("The following loadbalancer ids were not found: " + output).build();
        }

        for (String id : loadBalancerIds) {
            try {
                org.openstack.atlas.service.domain.pojos.Sync domainSyncObject = new org.openstack.atlas.service.domain.pojos.Sync();
                domainSyncObject.setLoadBalancerId(Integer.parseInt(id));
                domainSyncObject.setLocationToSyncFrom(SyncLocation.DATABASE);

                EsbRequest req = new EsbRequest();
                req.setSyncObject(domainSyncObject);

                loadBalancerService.get(Integer.parseInt(id));
                getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.SYNC, req);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        String output = ListUtil.generateCommaSeparatedString(loadBalancerIds);
        return Response.status(200).entity("The following ids were accepted: " + output).build();
    }

    public void setLoadBalancerIds(List<String> loadBalancerIds) {
        this.loadBalancerIds = loadBalancerIds;
    }
}