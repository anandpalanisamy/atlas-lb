package org.openstack.atlas.api.resources;

import org.openstack.atlas.api.helpers.PaginationLinksBuilder;

import java.util.*;

import org.openstack.atlas.service.domain.pojos.NodeMap;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.api.helpers.LoadBalancerProperties;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.apache.abdera.model.Feed;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.*;

public class NodesResource extends CommonDependencyProvider {

    private NodeResource nodeResource;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveNodes(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker, @QueryParam("page") Integer page) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        Set<Node> dnodes;
        Nodes rnodes = new Nodes();
        try {
            dnodes = nodeService.getNodesByAccountIdLoadBalancerId(getAccountId(), getLoadBalancerId(), offset, limit, marker);
            dnodes = LoadBalancerProperties.setWeightsforNodes(dnodes);

//            List<Integer> paginatedIds = new ArrayList<Integer>();
//            NodeMap nodeMap;
            for (org.openstack.atlas.service.domain.entities.Node dnode : dnodes) {
                rnodes.getNodes().add(dozerMapper.map(dnode, org.openstack.atlas.docs.loadbalancers.api.v1.Node.class));
                //Populate the id list
//                paginatedIds.add(dnode.getId());
            }

//            nodeMap = nodeService.getNodeIds(accountId);
//            rnodes.getLinks().addAll(PaginationLinksBuilder.buildLinks(getRequestStateContainer().getUriInfo(), nodeMap.getIdsList(), paginatedIds, limit, marker));

            return Response.status(200).entity(rnodes).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response deleteNodes(@QueryParam("id") List<Integer> ids) {

        MessageDataContainer msg = new MessageDataContainer();

        boolean isLbEditable;
        LoadBalancer dlb = new LoadBalancer();
        List<String> validationErrors;
        dlb.setId(loadBalancerId);
        Collections.sort(ids);
        try {
            isLbEditable = loadBalancerService.testAndSetStatusPending(accountId, loadBalancerId);
            if (!isLbEditable) {
                throw new ImmutableEntityException("LoadBalancer is not ACTIVE");
            }
            validationErrors = nodeService.prepareForNodesDeletion(accountId, loadBalancerId, ids);
            if (validationErrors.size() > 0) {
                loadBalancerService.setStatus(dlb, LoadBalancerStatus.ACTIVE);
                return getValidationFaultResponse(validationErrors);
            }
            msg.setIds(ids);
            msg.setAccountId(accountId);
            msg.setLoadBalancerId(loadBalancerId);
            msg.setUserName(getUserName(requestHeaders));
            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_NODES, msg);            
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(202).build();
    }

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createNodes(Nodes nodes) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(Nodes.class).validate(nodes, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            loadBalancerService.get(loadBalancerId, accountId);

            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            apiLb.getNodes().addAll(nodes.getNodes());
            LoadBalancer domainLb = dozerMapper.map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);
            domainLb.setUserName(getUserName(requestHeaders));

            Nodes returnNodes = new Nodes();
            Set<Node> dbnodes = nodeService.createNodes(domainLb);
            for (Node node : dbnodes) {
                returnNodes.getNodes().add(dozerMapper.map(node, org.openstack.atlas.docs.loadbalancers.api.v1.Node.class));
            }
            asyncService.callAsyncLoadBalancingOperation(Operation.CREATE_NODES, domainLb);
            return Response.status(Response.Status.ACCEPTED).entity(returnNodes).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);

        }
    }

    @Path("{id: [-+]?[1-9][0-9]*}")
    public NodeResource retrieveNodeResource(@PathParam("id") int id) {
        nodeResource.setRequestHeaders(requestHeaders);
        nodeResource.setId(id);
        nodeResource.setAccountId(accountId);
        nodeResource.setLoadBalancerId(loadBalancerId);
        return nodeResource;
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.NODES_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                nodeService.getNodesByAccountIdLoadBalancerId(getAccountId(), getLoadBalancerId());
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    public void setNodeResource(NodeResource nodeResource) {
        this.nodeResource = nodeResource;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public int getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
