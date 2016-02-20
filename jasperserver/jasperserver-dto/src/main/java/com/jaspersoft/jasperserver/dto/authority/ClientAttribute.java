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
package com.jaspersoft.jasperserver.dto.authority;

import com.jaspersoft.jasperserver.dto.authority.hypermedia.HypermediaAttribute;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @author Volodya Sabadosh
 * @author Vlad Zavadskii
 * @version $Id: Id$
 */
@XmlRootElement(name = "attribute")
@XmlSeeAlso({HypermediaAttribute.class})
public class ClientAttribute {
    private String name;
    private String value;
    private Boolean secure = null;
    private Boolean inherited = null;
    private String description;
    private Integer permissionMask;
    private String holder;

    public ClientAttribute(ClientAttribute other) {
        this.name = other.getName();
        this.value = other.getValue();
        this.secure = other.isSecure();
        this.description = other.getDescription();
        this.permissionMask = other.getPermissionMask();
        this.inherited = other.isInherited();
        this.holder = other.getHolder();
    }

    public ClientAttribute() {
    }

    public String getName() {
        return name;
    }

    public ClientAttribute setName(String name) {
        this.name = name;
        return this;
    }

    public String getHolder() {
        return holder;
    }

    public ClientAttribute setHolder(String holder) {
        this.holder = holder;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ClientAttribute setValue(String value) {
        this.value = value;
        return this;
    }

    public ClientAttribute setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    @XmlElement(name = "secure")
    public Boolean isSecure() {
        return secure;
    }

    @XmlElement(name = "inherited")
    public Boolean isInherited() {
        return inherited;
    }

    public ClientAttribute setInherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ClientAttribute setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getPermissionMask() {
        return permissionMask;
    }

    public ClientAttribute setPermissionMask(Integer permissionMask) {
        this.permissionMask = permissionMask;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientAttribute that = (ClientAttribute) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (holder != null ? !holder.equals(that.holder) : that.holder != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (holder != null ? holder.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientAttribute{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", secure='" + secure + '\'' +
                ", inherited='" + inherited + '\'' +
                ", description='" + description + '\'' +
                ", permissionMask='" + permissionMask + '\'' +
                ", holder='" + holder + '\'' +
                '}';
    }
}
