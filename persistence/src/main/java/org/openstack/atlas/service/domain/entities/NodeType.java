package org.openstack.atlas.service.domain.entities;

import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;

import java.io.Serializable;

public enum NodeType implements Serializable {
    PRIMARY, SECONDARY;

    private final static long serialVersionUID = 532512316L;
}
