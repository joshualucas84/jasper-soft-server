/*
 * Copyright (C) 2005 - 2014 TIBCO Software. All rights reserved.
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
package com.jaspersoft.jasperserver.jsp;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.util.JavaScriptUtils;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.jsp.JspContext;
import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author  dlitvak
 * @version $id$
 */
public class XSSEscapeXmlELResolver extends ELResolver {
	private static final Logger log = LogManager.getLogger(XSSEscapeXmlELResolver.class);

	public static final String XSSESCAPE_XML_ELRESOLVER_PROPERTIES = "com/jaspersoft/jasperserver/jsp/XSSEscapeXmlELResolver.properties";

	/** pageContext attribute name for flag to enable XML escaping */
	public static final String ESCAPE_XSS_SCRIPT = XSSEscapeXmlELResolver.class.getName() + ".escapeScript";

	public static final String UTF8_ESCAPE_XSS_SCRIPT = XSSEscapeXmlELResolver.class.getName() + ".javaScriptEscape";
	public static final String UNESCAPED_TILE_AND_EL_ATTRIBUTES_PROP = "unescapedTileandELAttributes";

	private static Set<String> unescapedTileandELAttributes;

	public XSSEscapeXmlELResolver() {
		if (unescapedTileandELAttributes == null) {
			Properties classProperies = new Properties();
			InputStream is = null;
			try {
				is = XSSEscapeXmlELResolver.class.getClassLoader().getResourceAsStream(XSSESCAPE_XML_ELRESOLVER_PROPERTIES);
				classProperies.load(is);
				unescapedTileandELAttributes = new HashSet<String>(Arrays.asList(classProperies.getProperty(UNESCAPED_TILE_AND_EL_ATTRIBUTES_PROP).split("\\s*,\\s*")));
			}
			catch (Exception e) {
				final String errMessage = "Failed to find " + UNESCAPED_TILE_AND_EL_ATTRIBUTES_PROP + " property in " + XSSESCAPE_XML_ELRESOLVER_PROPERTIES;
				log.error(errMessage, e);
				throw new RuntimeException(errMessage, e);
			}
			finally {
				try {
					if (is != null)
						is.close();
				}
				catch (IOException ioe) {
					log.error("Failed to close " + XSSESCAPE_XML_ELRESOLVER_PROPERTIES, ioe);
				}
			}
		}
	}

	private ThreadLocal<Boolean> excludeMe = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		JspContext pageContext = (JspContext) context.getContext(JspContext.class);
		Boolean escapeScript = (Boolean) pageContext.getAttribute(ESCAPE_XSS_SCRIPT);
		Boolean javaScriptEscape = (Boolean) pageContext.getAttribute(UTF8_ESCAPE_XSS_SCRIPT);

		if (escapeScript != null && !escapeScript) {
			return null;
		}

		try {
			if (excludeMe.get()) {
				return null;
			}

			if (property == null || unescapedTileandELAttributes.contains(property.toString())) {
				return null;
			}

			// This resolver is in the original resolver chain. To prevent
			// infinite recursion, set a flag to prevent this resolver from
			// invoking the original resolver chain again when its turn in the
			// chain comes around.
			excludeMe.set(Boolean.TRUE);
			Object obj = context.getELResolver().getValue(context, base, property);
			if (obj != null && isTypeSuitable(obj)) {
				String objStr = obj.toString();
				if (javaScriptEscape != null && javaScriptEscape) {
					obj = JavaScriptUtils.javaScriptEscape(objStr);
				}
				else
					obj = EscapeXssScript.escape(objStr);
			}
			return obj;
		} finally {
		  excludeMe.remove();
		}
	}
    
    private boolean isTypeSuitable(Object obj) {
        return obj instanceof String 
                || obj instanceof StringBuffer 
                || obj instanceof StringBuilder 
                || obj instanceof Locale 
                || obj instanceof JSONObject;         
    }

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return true;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
