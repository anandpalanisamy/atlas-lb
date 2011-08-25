package org.openstack.atlas.api.resources.providers;

import java.util.ArrayList;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.api.atom.AtomFeedAdapter;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.dozer.DozerBeanMapper;
import org.openstack.atlas.api.config.RestApiConfiguration;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;

@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CommonDependencyProvider {

    protected final static String NOBODY = "Undefined User";
    protected final static String USERHEADERNAME = "X-PP-User";
    protected final static String VFAIL = "Validation Failure";
    protected RequestStateContainer requestStateContainer;
    protected RestApiConfiguration restApiConfiguration;
    protected AsyncService asyncService;
    protected LoadBalancerRepository lbRepository;
    protected DozerBeanMapper dozerMapper;
    protected AtomFeedAdapter atomFeedAdapter;
    protected LoadBalancerService loadBalancerService;
    protected HealthMonitorService healthMonitorService;
    protected ConnectionLoggingService connectionLoggingService;
    protected ConnectionThrottleService connectionThrottleService;
    protected VirtualIpService virtualIpService;
    protected NodeService nodeService;
    protected SessionPersistenceService sessionPersistenceService;
    protected AccountLimitService accountLimitService;
    protected AccessListService accessListService;
    protected AlgorithmsService algorithmsService;
    protected UsageService usageService;
    protected ProtocolsService protocolsService;

    public void setProtocolsService(ProtocolsService protocolsService) {
        this.protocolsService = protocolsService;
    }

    public void setAlgorithmsService(AlgorithmsService algorithmsService) {
        this.algorithmsService = algorithmsService;
    }

    public void setSessionPersistenceService(SessionPersistenceService sessionPersistenceService) {
        this.sessionPersistenceService = sessionPersistenceService;
    }

    public void setConnectionThrottleService(ConnectionThrottleService connectionThrottleService) {
        this.connectionThrottleService = connectionThrottleService;
    }

    public void setAccessListService(AccessListService accessListService) {
        this.accessListService = accessListService;
    }

    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    public void setConnectionLoggingService(ConnectionLoggingService connectionLoggingService) {
        this.connectionLoggingService = connectionLoggingService;
    }

    public void setHealthMonitorService(HealthMonitorService healthMonitorService) {
        this.healthMonitorService = healthMonitorService;
    }

    public void setVirtualIpService(VirtualIpService virtualIpService) {
        this.virtualIpService = virtualIpService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setUsageService(UsageService usageService) {
        this.usageService = usageService;
    }

    public void setAsyncService(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    public void setLbRepository(LoadBalancerRepository lbRepository) {
        this.lbRepository = lbRepository;
    }

    public void setDozerMapper(DozerBeanMapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public void setAtomFeedAdapter(AtomFeedAdapter atomFeedAdapter) {
        this.atomFeedAdapter = atomFeedAdapter;
    }

    public void setAccountLimitService(AccountLimitService accountLimitService) {
        this.accountLimitService = accountLimitService;
    }

    public String getUserName(HttpHeaders headers){
        if(headers == null || headers.getRequestHeader(USERHEADERNAME).size()<1){
            return NOBODY;
        }
        String userName = headers.getRequestHeader(USERHEADERNAME).get(0);
        if(userName == null){
            return NOBODY;
        }
        return userName;
    }

    public static String getStackTraceMessage(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Exception: %s:%s\n", e.getMessage(), e.getClass().getName()));
        for (StackTraceElement se : e.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    public String getExceptionMessage(Exception e) {
        return String.format("%s\n%s\n", e.toString(), e.getMessage());
    }

    public Response getValidationFaultResponse(ValidatorResult result) {
        List<String> vmessages = result.getValidationErrorMessages();
        int status = 400;
        BadRequest badreq = HttpResponseBuilder.buildBadRequestResponse(VFAIL, vmessages);
        Response vresp = Response.status(status).entity(badreq).build();
        return vresp;
    }

    public Response getValidationFaultResponse(String errorStr){
        List<String> errorStrs = new ArrayList<String>();
        errorStrs.add(errorStr);
        return getValidationFaultResponse(errorStrs);
    }

    public Response getValidationFaultResponse(List<String> errorStrs) {
        BadRequest badreq;
        int status = 400;
        badreq = HttpResponseBuilder.buildBadRequestResponse(VFAIL, errorStrs);
        Response resp = Response.status(status).entity(badreq).build();
        return resp;
    }

    public RequestStateContainer getRequestStateContainer() {
        return requestStateContainer;
    }

    public void setRequestStateContainer(RequestStateContainer requestStateContainer) {
        this.requestStateContainer = requestStateContainer;
    }

    public void setRestApiConfiguration(RestApiConfiguration restApiConfiguration) {
        this.restApiConfiguration = restApiConfiguration;
    }

}
