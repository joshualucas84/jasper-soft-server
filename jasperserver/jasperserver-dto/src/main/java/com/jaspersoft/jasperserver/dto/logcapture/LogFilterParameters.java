/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * Licensed under commercial Jaspersoft Subscription License Agreement
 */
package com.jaspersoft.jasperserver.dto.logcapture;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yakiv Tymoshenko
 * @version $Id: Id $
 * @since 07.10.14
 */
@XmlRootElement
@XmlType(propOrder = {})
public class LogFilterParameters {
    private String userId;
    private String sessionId;
    private ResourceAndSnapshotFilter resourceAndSnapshotFilter;

    public LogFilterParameters() {
        resourceAndSnapshotFilter = new ResourceAndSnapshotFilter();
    }

    public LogFilterParameters(LogFilterParameters other) {
        this.userId = other.getUserId();
        this.sessionId = other.getSessionId();
        this.resourceAndSnapshotFilter = other.getResourceAndSnapshotFilter();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @XmlElement(name = "resource")
    public ResourceAndSnapshotFilter getResourceAndSnapshotFilter() {
        return resourceAndSnapshotFilter;
    }

    public void setResourceAndSnapshotFilter(ResourceAndSnapshotFilter resourceAndSnapshotFilter) {
        this.resourceAndSnapshotFilter = resourceAndSnapshotFilter;
    }
}
