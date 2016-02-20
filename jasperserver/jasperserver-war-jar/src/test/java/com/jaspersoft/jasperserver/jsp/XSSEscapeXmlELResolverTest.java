package com.jaspersoft.jasperserver.jsp;

import junit.framework.Assert;
import org.apache.taglibs.standard.lang.jstl.test.PageContextImpl;
import org.apache.tiles.evaluator.el.ELContextImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.util.JavaScriptUtils;
import org.unitils.UnitilsJUnit4;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.MapELResolver;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dlitvak
 * @version $Id: XSSEscapeXmlELResolverTest.java 54868 2015-04-29 15:13:53Z dlitvak $
 */
public class XSSEscapeXmlELResolverTest extends UnitilsJUnit4 {

	public static final String EL_PROPERTY = "elProperty";
	public static final String EL_NON_ESCAPED_PROPERTY = "bodyContent";

	private PageContext pageContext;
	private ELResolver xssElResolver;
	private ELContext elContext;
	private Map<String, String> elBaseMap;

	@Before
	public void setUp() {
		pageContext = new PageContextImpl();
		xssElResolver = new XSSEscapeXmlELResolver();

		ELResolver baseElResolver = new MapELResolver();
		elContext = new ELContextImpl(baseElResolver);
		elContext.putContext(JspContext.class, pageContext);

		elBaseMap = new HashMap<String, String>();
	}

	/**
	 * Test that < and > are properly escaped as &lt; and &gt;
	 */
    @Test
    public void testGetValue() {
		elBaseMap.put(EL_PROPERTY, "<script>");
		Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_PROPERTY);
		String escVal = EscapeXssScript.escape(elBaseMap.get(EL_PROPERTY));

		Assert.assertNotNull("xssElResolver.getValue is null", jspVal);
		Assert.assertNotNull("EscapeXssScript.escape is null", escVal);
		Assert.assertEquals("Result returned by XSS resolver is not equal that from XSS Escaper.", jspVal.toString(), escVal);
    }

	/**
	 * Test that < and > are NOT escaped when the resolved EL attribute is part of the exception list
	 * in XSSEscapeXmlELResolver.properties.  Usually, it means that this attribute is part of JSP Tiles.
	 */
    @Test
    public void testGetValueNotEscaping() {
		elBaseMap.put(EL_NON_ESCAPED_PROPERTY, "<script>");
		Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_NON_ESCAPED_PROPERTY);

		Assert.assertNull("testGetValueNotEscaping: xssElResolver.getValue should be null", jspVal);
    }

	/**
	 * Test that < and > are NOT escaped when EL is inside <js:out escapeScript=false>${elProperty}</js:out>.
	 */
	@Test
	public void testGetValueWithEscapeScriptFalse() {
		pageContext.setAttribute(XSSEscapeXmlELResolver.ESCAPE_XSS_SCRIPT, false);

		elBaseMap.put(EL_PROPERTY, "<script>");
		Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_PROPERTY);

		Assert.assertNull("testGetValueWithEscapeScriptFalse: xssElResolver.getValue should be null", jspVal);
	}

	/**
	 * Test that < and > are UTF-8 escaped when EL is inside <js:out javaScriptEscape=true>${elProperty}</js:out>.
	 */
	@Test
	public void testGetValueScriptWithJavaScriptEscapeTrue() {
		pageContext.setAttribute(XSSEscapeXmlELResolver.UTF8_ESCAPE_XSS_SCRIPT, true);

		elBaseMap.put(EL_PROPERTY, "<script>");
		Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_PROPERTY);
		String escVal = JavaScriptUtils.javaScriptEscape(elBaseMap.get(EL_PROPERTY));

		Assert.assertNotNull("xssElResolver.getValue is null", jspVal);
		Assert.assertNotNull("JavaScriptUtils.javaScriptEscape is null", escVal);
		Assert.assertEquals("Result returned by XSS resolver should be equal that from JavaScriptUtils.javaScriptEscape.", jspVal.toString(), escVal);
	}
}
