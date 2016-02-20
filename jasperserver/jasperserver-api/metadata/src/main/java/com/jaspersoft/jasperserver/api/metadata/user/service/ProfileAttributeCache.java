package com.jaspersoft.jasperserver.api.metadata.user.service;

import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;

/**
 * Created with IntelliJ IDEA.
 * User: nthapa
 * Date: 3/12/15
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProfileAttributeCache {

    public void addItem(Object key, Object value);

    public Object getItem(Object key);

    public void removeItem(Object key);

    public void clearAll();


}
