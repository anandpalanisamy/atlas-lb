package org.openstack.atlas.service.domain.entity;

import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

import java.io.Serializable;

public enum IpVersion implements Serializable {
    IPV4(org.openstack.atlas.core.api.v1.IpVersion.IPV4),
    IPV6(org.openstack.atlas.core.api.v1.IpVersion.IPV6);

    private final static long serialVersionUID = 532512316L;
    private final org.openstack.atlas.core.api.v1.IpVersion myType;

    IpVersion(org.openstack.atlas.core.api.v1.IpVersion myType) {
        this.myType = myType;
    }

    public org.openstack.atlas.core.api.v1.IpVersion getDataType() {
        return myType;
    }

    public static IpVersion fromDataType(org.openstack.atlas.core.api.v1.IpVersion type) {
        for (IpVersion value : values()) {
            if (type == value.getDataType()) {
                return value;
            }
        }
        
        throw new NoMappableConstantException("Could not map constant: " + type.value() + " for type: " + type.name());
    }
}

