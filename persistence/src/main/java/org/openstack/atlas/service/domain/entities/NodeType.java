package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum NodeType implements Serializable {
    PRIMARY, FAIL_OVER;

    private final static long serialVersionUID = 532512316L;
}
