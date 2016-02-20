/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2009 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.dto.logcapture;

import javax.xml.bind.annotation.*;

/**
 * @author Yakiv Tymoshenko
 * @version $Id: ResourceAndSnapshotFilter.java 56967 2015-08-20 23:20:53Z esytnik $
 * @since 10.02.2015
 */
@XmlRootElement
@XmlType(propOrder = {"resourceUri", "includeDataSnapshots"})
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ResourceAndSnapshotFilter {
    private String resourceUri;
    private Boolean includeDataSnapshots;

    public ResourceAndSnapshotFilter() {
    }

    public ResourceAndSnapshotFilter(ResourceAndSnapshotFilter other) {
        this.resourceUri = other.getResourceUri();
        this.includeDataSnapshots = other.getIncludeDataSnapshots();
    }

    /*
        Don't name it "isExportEnabled" because it would look like a property in resulting JSON/XML.
     */
    public boolean exportEnabled() {
        return includeDataSnapshots != null
                && (includeDataSnapshots && resourceUri != null && resourceUri.length() > 0);
    }

    @XmlElement(name = "uri")
    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    @XmlElement(name = "includeDataSnapshot")
    public Boolean getIncludeDataSnapshots() {
        return includeDataSnapshots;
    }

    public void setIncludeDataSnapshots(Boolean includeDataSnapshots) {
        this.includeDataSnapshots = includeDataSnapshots;
    }
}
