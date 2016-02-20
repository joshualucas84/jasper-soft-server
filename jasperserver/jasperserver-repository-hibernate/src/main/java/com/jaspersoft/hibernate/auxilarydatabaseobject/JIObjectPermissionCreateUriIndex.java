package com.jaspersoft.hibernate.auxilarydatabaseobject;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.AbstractAuxiliaryDatabaseObject;

/**
 * Creates <i>uri_index</i> for <i>uri</i> column on JIObjectPermission table
 *
 * @author Vlad Zavadskii
 * @version $Id: JIObjectPermissionCreateUriIndex.java 58720 2015-10-20 14:27:28Z vzavadsk $
 */
public class JIObjectPermissionCreateUriIndex extends AbstractAuxiliaryDatabaseObject {
    @Override
    public String sqlCreateString(Dialect dialect, Mapping mapping, String s, String s1) throws HibernateException {
        if (dialect instanceof MySQLDialect) {
            // We need to explicitly set length for the uri_index index on MySQL database.
            // Its allow to increase size of uri to more then 255 symbols.
            // Otherwise we'll get "#1071 - Specified key was too long; max key length is 767 bytes" error message
            return "create index uri_index on JIObjectPermission (uri(255))";
        } else {
            return "create index uri_index on JIObjectPermission (uri)";
        }
    }

    @Override
    public String sqlDropString(Dialect dialect, String s, String s1) {
        return "DROP INDEX uri_index ON JIObjectPermission";
    }
}
