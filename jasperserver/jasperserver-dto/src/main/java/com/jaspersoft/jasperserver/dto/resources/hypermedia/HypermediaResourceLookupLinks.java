package com.jaspersoft.jasperserver.dto.resources.hypermedia;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by borys.kolesnykov on 9/22/2014.
 */
@XmlRootElement(name = "_links")
public class HypermediaResourceLookupLinks {

    private String self;
    private String next;
    private String prev;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }
}
