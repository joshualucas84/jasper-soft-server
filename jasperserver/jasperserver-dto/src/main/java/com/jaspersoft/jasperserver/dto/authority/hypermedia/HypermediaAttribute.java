/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License  as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero  General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.dto.authority.hypermedia;
import com.jaspersoft.jasperserver.dto.authority.ClientAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id: HypermediaAttribute.java 58870 2015-10-27 22:30:55Z esytnik $
 */
@XmlRootElement(name = "hypermediaAttribute")
public class HypermediaAttribute extends ClientAttribute {
    private HypermediaAttributeEmbeddedContainer embedded;
    private HypermediaAttributeLinks links;

    public HypermediaAttribute(ClientAttribute other) {
        super(other);
    }

    public HypermediaAttribute() {
    }

    @XmlElement(name = "_embedded")
    public HypermediaAttributeEmbeddedContainer getEmbedded() {
        return embedded;
    }

    public HypermediaAttribute setEmbedded(HypermediaAttributeEmbeddedContainer embedded) {
        this.embedded = embedded;
        return this;
    }

    @XmlElement(name = "_links")
    public HypermediaAttributeLinks getLinks() {
        return links;
    }

    public HypermediaAttribute setLinks(HypermediaAttributeLinks links) {
        this.links = links;
        return this;
    }

}
