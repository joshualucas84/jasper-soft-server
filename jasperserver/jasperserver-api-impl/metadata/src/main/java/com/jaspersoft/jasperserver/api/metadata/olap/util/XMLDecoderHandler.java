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
package com.jaspersoft.jasperserver.api.metadata.olap.util;

import com.jaspersoft.jasperserver.api.JSException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.reflect.Method;
import java.util.Stack;

/**
 * Handler for Olap properties, used to avoid complications with WebSphere and Xerces
 *
 * @author Zakhar Tomchenco
 * @version $Id: $
 */
public class XMLDecoderHandler extends DefaultHandler {
    private Class lastClass;
    private String lastValuePart;
    private Stack<String> lastMethodName = new Stack<String>();
    private Stack<Object> objects = new Stack<Object>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (attributes.getValue("", "method") != null) {
                lastMethodName.push(attributes.getValue("", "method"));
            }

            if (qName.equals("object")) {
                Object object = Class.forName(attributes.getValue("", "class")).newInstance();
                objects.push(object);
            }

            if (qName.equals("string")) {
                lastClass = String.class;
            }

            if (qName.equals("int")) {
                lastClass = Integer.class;
            }

            if (qName.equals("double")) {
                lastClass = Double.class;
            }

            if (qName.equals("boolean")) {
                lastClass = Boolean.class;
            }

            if (qName.equals("null")) {
                lastClass = Void.class;
            }

        } catch (Exception e) {
            throw new JSException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (lastValuePart != null){
            this.characters(lastValuePart.toCharArray(), 0, 0);
        }

        if (qName.equals("void")) {
            Object arg2 = objects.pop(), arg1 = objects.pop(), owner = objects.pop();

            arg1 = arg1 == this ? null : arg1;
            arg2 = arg2 == this ? null : arg2;

            try {
                Method setter = owner.getClass().getMethod(lastMethodName.pop(), Object.class, Object.class);

                setter.invoke(owner, arg1, arg2);

                objects.push(owner);
            } catch (Exception e) {
                throw new JSException("Not expected method: " + lastMethodName);
            }
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String value;

        if (ch.length == start + length){
            lastValuePart = new String(ch, start, length);
        } else {
            if (lastValuePart != null) {
                value = lastValuePart + new String(ch, start, length);
                lastValuePart = null;
            } else {
                value = new String(ch, start, length);
            }

            if (lastClass != null) {
                if (lastClass.equals(String.class)) {
                    objects.push(value);
                }

                if (lastClass.equals(Void.class)) {
                    objects.push(this);
                }

                if (lastClass.equals(Boolean.class)) {
                    if (value.toLowerCase().equals("true")) {
                        objects.push(Boolean.TRUE);
                    } else if (value.toLowerCase().equals("false")) {
                        objects.push(Boolean.FALSE);
                    } else {
                        throw new JSException("Unknown boolean value: " + value);
                    }
                }

                if (lastClass.equals(Integer.class)) {
                    objects.push(Integer.parseInt(value));
                }

                if (lastClass.equals(Double.class)) {
                    objects.push(Double.parseDouble(value));
                }

                lastClass = null;
            }
        }
    }

    public Object getResult(){
        return objects.pop();
    }
}
