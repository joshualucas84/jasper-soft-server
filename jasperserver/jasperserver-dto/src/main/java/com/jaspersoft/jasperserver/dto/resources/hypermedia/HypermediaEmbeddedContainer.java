package com.jaspersoft.jasperserver.dto.resources.hypermedia;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by borys.kolesnykov on 9/22/2014.
 */
@XmlRootElement(name = "_embedded")
public class HypermediaEmbeddedContainer {

    private List<ClientResourceLookup> resourceLookup;

    public List<ClientResourceLookup> getResourceLookup() {
        return resourceLookup;
    }

    public void setResourceLookup(List<ClientResourceLookup> resourceLookup) {
        this.resourceLookup = resourceLookup;
    }
}
