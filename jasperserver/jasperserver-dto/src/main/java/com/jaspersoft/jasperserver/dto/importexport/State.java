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
package com.jaspersoft.jasperserver.dto.importexport;

import com.jaspersoft.jasperserver.dto.common.WarningDescriptor;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author: Zakhar.Tomchenco
 */
@XmlRootElement(name = "state")
public class State {

    String id;
    String message, phase;

    List<WarningDescriptor> warnings;

    ErrorDescriptor error;

    public State() {
        super();
    }

    @XmlElement(name = "id")
    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "phase")
    public synchronized String getPhase() {
        return phase;
    }

    public synchronized void setPhase(String phase) {
        this.phase = phase;
    }

    @XmlElement(name = "message")
    public synchronized String getMessage() {
        return message;
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public synchronized void setWarnings(List<WarningDescriptor> warnings) {
        this.warnings = warnings;
    }

    @XmlElement(name = "warnings")
    public synchronized List<WarningDescriptor> getWarnings() {
        if (warnings != null && warnings.size() > 0) {
            return warnings;
        }
        return null;
    }

    @XmlElement(name = "error")
    public synchronized ErrorDescriptor getError() {
        if (error == null) {
            return null;
        } else {
            ErrorDescriptor errorDescriptor = new ErrorDescriptor();
            errorDescriptor.setMessage(error.getMessage());
            errorDescriptor.setErrorCode(error.getErrorCode());
            errorDescriptor.setParameters(error.getParameters());

            return errorDescriptor;
        }
    }

    public synchronized void setError(ErrorDescriptor error) {
        if (error == null) {
            this.error = null;
        } else {
            ErrorDescriptor errorDescriptor = new ErrorDescriptor();
            errorDescriptor.setMessage(error.getMessage());
            errorDescriptor.setErrorCode(error.getErrorCode());
            errorDescriptor.setParameters(error.getParameters());

            this.error = error;
        }
    }
}
