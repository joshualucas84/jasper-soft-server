package com.jaspersoft.jasperserver.api.metadata.user.service;

/**
 * Used to escape special characters in profile attribute value
 *
 * @author Vlad Zavadskii
 * @version $Id: ProfileAttributeEscapeStrategy.java 54590 2015-04-22 17:55:42Z vzavadsk $
 */
public interface ProfileAttributeEscapeStrategy {
    String escape(String profileAttributeValue);
}
