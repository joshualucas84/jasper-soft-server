package com.jaspersoft.jasperserver.jsp;

import junit.framework.Assert;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;

/**
 *
 * @author dlitvak
 * @version $Id: EscapeXssScriptTest.java 55139 2015-05-05 22:41:57Z dlitvak $
 */
public class EscapeXssScriptTest extends UnitilsJUnit4 {

	/**
	 * Test that < and > are properly escaped as &lt; and &gt;
	 */
	@Test
    public void testEscape() {
		Assert.assertEquals("<script>alert; should be escaped as &lt;script&gt;alert&#059;", EscapeXssScript.escape("<script>alert;"), "&lt;script&gt;alert&#059;");
	}

	/**
	 * Test that ; is not incorrectly escaped in the unicode encoded chars such as &#1071;
	 */
	@Test
    public void testEscapeOfUnicodeChar() {
		Assert.assertEquals("<&#1071;> should be escaped as &lt;&#1071;&gt;", "&lt;&#1071;&gt;", EscapeXssScript.escape("<&#1071;>"));
	}

	/**
	 * Test that ; is not incorrectly escaped in the html encoded chars such as &amp;
	 */
	@Test
    public void testEscapeOfHTMLChar() {
		Assert.assertEquals("<&amp;&amp;&frac34;> should be escaped as &lt;&amp;&amp;&frac34;&gt;", "&lt;&amp;&amp;&frac34;&gt;", EscapeXssScript.escape("<&amp;&amp;&frac34;>"));
	}
}
