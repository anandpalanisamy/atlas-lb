package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.context.NodeTypeContext;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.AlgorithmType;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.ProtocolPortBindings;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

public class LoadBalancerValidator implements ResourceValidator<LoadBalancer> {
    private Validator<LoadBalancer> validator;
    private final int MIN_PORT = 1;
    private final int MAX_PORT = 65535;
    private final int LB_NAME_LENGTH = 128;

    public LoadBalancerValidator() {
        validator = build(new ValidatorBuilder<LoadBalancer>(LoadBalancer.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getProtocol()).if_().exist().then().must().adhereTo(new MustBeInArray(ProtocolPortBindings.getKeysAsArray())).withMessage("Load balancer protocol is invalid. Please specify a valid protocol.");
                result(validationTarget().getAlgorithm()).if_().exist().then().must().adhereTo(new MustBeInArray(AlgorithmType.values())).withMessage("Load balancer algorithm is invalid. Please specify a valid algorithm.");

                result(validationTarget().getPort()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MIN_PORT, MAX_PORT)).withMessage("Load balancer port is invalid. Please specify a valid port.");
                result(validationTarget().getId()).must().not().exist().withMessage("Load balancer id field cannot be modified.");
                result(validationTarget().getStatus()).must().not().exist().withMessage("Load balancer status field cannot be modified.");
                result(validationTarget().getLoadBalancerUsage()).must().beEmptyOrNull().withMessage("Load balancer current usage field cannot be modified.");
                result(validationTarget().getCluster()).must().not().exist().withMessage("Load balancer cluster field cannot be modified.");
                result(validationTarget().getCreated()).must().not().exist().withMessage("Load balancer created field cannot be modified.");
                result(validationTarget().getUpdated()).must().not().exist().withMessage("Load balancer updated field cannot be modified.");
                result(validationTarget().getName()).if_().exist().then().must().adhereTo(new MustHaveLengthVerifier(LB_NAME_LENGTH)).withMessage("Load Balancer name must be less than or equal to " + LB_NAME_LENGTH);

                // POST EXPECTATIONS
                result(validationTarget().getName()).must().exist().forContext(POST).withMessage("Must provide a name for the load balancer.");
                result(validationTarget().getName()).must().not().beEmptyOrNull().forContext(POST).withMessage("Load balancer name is invalid. Please specify a valid name");
                result(validationTarget().getProtocol()).must().exist().forContext(POST).withMessage("Must provide a valid protocol for the load balancer.");
                result(validationTarget().getVirtualIps()).must().exist().forContext(POST).withMessage("Must provide exactly one virtual ip for the load balancer.");
                result(validationTarget().getVirtualIps()).must().haveSizeOfExactly(1).forContext(POST).withMessage("Must have only one virtual ip for the load balancer");
                result(validationTarget().getVirtualIps()).if_().exist().then().must().adhereTo(new SharedOrNewVipVerifier()).forContext(POST).withMessage("Must specify either a shared or new virtual ip.");
                result(validationTarget().getVirtualIps()).if_().exist().then().must().delegateTo(new VirtualIpValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getConnectionLogging()).if_().exist().then().must().delegateTo(new ConnectionLoggingValidator().getValidator(), POST);

                // Need to determine how to get validation working for the collections.
                result(validationTarget().getAccessList()).if_().exist().then().must().cannotExceedSize(100).withMessage("Must not provide more than one hundred access list items");
                result(validationTarget().getAccessList()).if_().exist().then().must().delegateTo(new NetworkItemValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getNodes()).must().exist().forContext(POST).withMessage("Must provide at least one node for the load balancer.");
                result(validationTarget().getNodes()).must().adhereTo(new DuplicateNodeVerifier()).forContext(POST).withMessage("Duplicate nodes detected. Please ensure that the ip address and port are unique for each node.");
                result(validationTarget().getNodes()).must().adhereTo(new ActiveNodeVerifier()).forContext(POST).withMessage("Please ensure that at least one node has an ENABLED condition.");
                result(validationTarget().getNodes()).if_().exist().then().must().delegateTo(new NodeValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getNodes()).must().haveSizeOfAtLeast(1).forContext(POST).withMessage("Must have at least one node.");
                result(validationTarget().getSessionPersistence()).if_().exist().then().must().delegateTo(new SessionPersistenceValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getHealthMonitor()).if_().exist().then().must().delegateTo(new HealthMonitorValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getConnectionThrottle()).if_().exist().then().must().delegateTo(new ConnectionThrottleValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getNodes()).if_().exist().then().must().cannotExceedSize(25).withMessage("Must not provide more than twenty five nodes per load balancer.");


                // PUT EXPECTATIONS
                must().adhereTo(new Verifier<LoadBalancer>() {
                    @Override
                    public VerifierResult verify(LoadBalancer obj) {
                        return new VerifierResult(obj.getName() != null || obj.getAlgorithm() != null || obj.getPort() != null
                                || obj.getProtocol() != null || obj.getConnectionLogging() != null);
                    }
                }).forContext(PUT).withMessage("The load balancer must have at least one of the following to update: name, algorithm, protocol, port.");
                result(validationTarget().getNodes()).must().beEmptyOrNull().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/nodes to configure nodes.");
                result(validationTarget().getVirtualIps()).must().beEmptyOrNull().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/virtualips/{virtual ip id} to configure a virtual ip.");
                result(validationTarget().getSessionPersistence()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/sessionpersistence to configure session persistence.");
                result(validationTarget().getHealthMonitor()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/healthmonitor to configure your health monitor.");
                result(validationTarget().getConnectionThrottle()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/throttles to configure connection throttling.");
                result(validationTarget().getAccessList()).must().beEmptyOrNull().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/accesslist to configure access lists.");
                result(validationTarget().getConnectionLogging()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/connecitonlogging to configure connection logging.");

            }
        });
    }

    @Override
    public ValidatorResult validate(LoadBalancer lb, Object httpRequestType) {
        ValidatorResult result = validator.validate(lb, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<LoadBalancer> getValidator() {
        return validator;
    }

}
