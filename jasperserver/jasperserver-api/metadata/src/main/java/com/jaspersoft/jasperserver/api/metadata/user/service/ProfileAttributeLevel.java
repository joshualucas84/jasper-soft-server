package com.jaspersoft.jasperserver.api.metadata.user.service;

/**
 * Profile attribute level relative to target level
 *
 * @author askorodumov
 * @version $Id: ProfileAttributeLevel.java 54590 2015-04-22 17:55:42Z vzavadsk $
 */
public enum ProfileAttributeLevel {
    /**
     * When concerned attribute is on upper level than target level
     */
    PARENT,

    /**
     * When concerned attribute is on the target level
     */
    TARGET_ASSIGNED,

    /**
     * When concerned attribute is on lower level than target level
     */
    CHILD
}
