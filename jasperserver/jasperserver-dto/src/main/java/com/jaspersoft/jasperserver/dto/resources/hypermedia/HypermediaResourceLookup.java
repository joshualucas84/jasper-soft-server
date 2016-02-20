package com.jaspersoft.jasperserver.dto.resources.hypermedia;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by borys.kolesnykov on 9/22/2014.
 */
public class HypermediaResourceLookup extends ClientResourceLookup {

    private HypermediaResourceLinks links;
    private HypermediaEmbeddedContainer embedded;

    public HypermediaResourceLookup(ClientResourceLookup other) {
        super(other);
    }

    @XmlElement(name = "_embedded")
    public HypermediaEmbeddedContainer getEmbedded() {
        return embedded;
    }

    public void setEmbedded(HypermediaEmbeddedContainer embedded) {
        this.embedded = embedded;
    }

    @XmlElement(name = "_links")
    public HypermediaResourceLinks getLinks() {
        return links;
    }

    public void setLinks(HypermediaResourceLinks links) {
        this.links = links;
    }
}
