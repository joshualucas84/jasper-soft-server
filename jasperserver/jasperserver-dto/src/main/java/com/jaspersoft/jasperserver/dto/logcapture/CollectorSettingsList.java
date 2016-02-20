/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * Licensed under commercial Jaspersoft Subscription License Agreement
 */
package com.jaspersoft.jasperserver.dto.logcapture;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author Yakiv Tymoshenko
 * @version $Id: Id $
 * @since 06.10.14
 */
@XmlRootElement
@SuppressWarnings("unused")
public class CollectorSettingsList {
    List<CollectorSettings> collectorSettingsList;

    public CollectorSettingsList() {
    }

    public CollectorSettingsList(List<CollectorSettings> collectorSettingsList) {
        this.collectorSettingsList = collectorSettingsList;
    }

    @XmlElementWrapper(name = "CollectorSettingsList")
    @XmlElement(name = "CollectorSettings")
    public List<CollectorSettings> getCollectorSettingsList() {
        return collectorSettingsList;
    }

    public void setCollectorSettingsList(List<CollectorSettings> collectorSettingsList) {
        this.collectorSettingsList = collectorSettingsList;
    }
}
