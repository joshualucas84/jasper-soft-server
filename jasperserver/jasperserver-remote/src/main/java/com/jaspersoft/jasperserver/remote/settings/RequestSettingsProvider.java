/*
 * Copyright (C) 2005 - 2014 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 * Licensed under commercial Jaspersoft Subscription License Agreement
 */

package com.jaspersoft.jasperserver.remote.settings;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * Created by andriy on 26.09.14.
 */
public class RequestSettingsProvider implements SettingsProvider {

    private javax.servlet.http.HttpServletRequest getRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest();
    }

    @Override
    public Object getSettings() {
        HashMap<String, Object> settings = new HashMap<String, Object>();

        settings.put("maxInactiveInterval", getRequest().getSession().getMaxInactiveInterval());
        settings.put("contextPath", getRequest().getContextPath());
        return settings;
    }
}
