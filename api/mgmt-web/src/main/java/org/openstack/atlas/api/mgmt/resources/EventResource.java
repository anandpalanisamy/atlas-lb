package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvents;
import org.openstack.atlas.service.domain.pojos.DateTimeToolException;
import org.openstack.atlas.util.converters.exceptions.ConverterException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Calendar;

import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;

public class EventResource extends ManagementDependencyProvider {

    @GET
    @Path("account/{id: [1-9][0-9]*}/loadbalancer")
    public Response getRecentLoadBalancerEvents(@PathParam("id") int accountId, @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }


        org.openstack.atlas.service.domain.events.pojos.AccountLoadBalancerServiceEvents dEvents;
        AccountLoadBalancerServiceEvents rEvents = new AccountLoadBalancerServiceEvents();
        try {
            dEvents = getEventRepository().getRecentLoadBalancerServiceEvents(accountId, startDate, endDate);
            rEvents = getDozerMapper().map(dEvents, rEvents.getClass());
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(200).entity(rEvents).build();
    }

    @GET
    @Path("user/{username: [0-9A-Za-z ]+}")
    public Response getAllEventsByUsername(@PathParam("username") String username, @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate, @QueryParam("page") Integer page) throws DateTimeToolException, ConverterException {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }

        Calendar startCal;
        if (startDate == null) {
            startCal = Calendar.getInstance();
            startCal.add(Calendar.MONTH, -4);
        } else {
            startCal = isoTocal(startDate);
        }

        Calendar endCal;
        if (endDate == null) {
            endCal = Calendar.getInstance();
        } else {
            endCal = isoTocal(endDate);
        }

        if (page == null) {
            page = 1;
        }

        if (page <= 0) {
            page = 1;
        }

        org.openstack.atlas.service.domain.events.pojos.LoadBalancerServiceEvents dEvents;
        LoadBalancerServiceEvents rEvents = new LoadBalancerServiceEvents();
        try {
            dEvents = getEventRepository().getAllEventsForUsername(username, page, startCal, endCal);
            rEvents = getDozerMapper().map(dEvents, rEvents.getClass());
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

        return Response.status(200).entity(rEvents).build();
    }
}
