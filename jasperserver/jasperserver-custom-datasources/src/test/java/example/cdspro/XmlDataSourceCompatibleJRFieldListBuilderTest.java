package example.cdspro;

import java.util.List;

import net.sf.jasperreports.engine.JRField;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link XmlDataSourceCompatibleJRFieldListBuilder}.
 */
public class XmlDataSourceCompatibleJRFieldListBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void badInput_null() {
        new XmlDataSourceCompatibleJRFieldListBuilder().field(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badInput_emptyString() {
        new XmlDataSourceCompatibleJRFieldListBuilder().field("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void badInput_blankString() {
        new XmlDataSourceCompatibleJRFieldListBuilder().field(" ");
    }

    @Test
    public void add2get2() {
        List<JRField> fields = new XmlDataSourceCompatibleJRFieldListBuilder().field("first").field("second").build();
        Assert.assertEquals("Unexpected number of fields", 2, fields.size());
    }

    /**
     * Test that insertion order is preserved.
     */
    @Test
    public void order() {
        List<JRField> fields = new XmlDataSourceCompatibleJRFieldListBuilder().field("first")
                .field("second")
                .field("third")
                .field("fourth")
                .build();
        String errorMessage = "Unexpected field order";
        Assert.assertEquals(errorMessage, "first", fields.get(0).getName());
        Assert.assertEquals(errorMessage, "second", fields.get(1).getName());
        Assert.assertEquals(errorMessage, "third", fields.get(2).getName());
        Assert.assertEquals(errorMessage, "fourth", fields.get(3).getName());
    }

    /**
     * Test that non-unique field names are handled according to the rules in the class javadoc.
     */
    @Test
    public void nonuniqueNames() {
        List<JRField> fields = new XmlDataSourceCompatibleJRFieldListBuilder().field("same").field("different").field("same").build();

        Assert.assertEquals("Unexpected number of fields", 3, fields.size());

        String nameErrorMsg = "Unexpected filed name";
        String descrErrorMsg = "Unexpected filed description";

        Assert.assertEquals(nameErrorMsg, "same_1", fields.get(0).getName());
        Assert.assertEquals(descrErrorMsg, "same[1]", fields.get(0).getDescription());

        Assert.assertEquals(nameErrorMsg, "same_2", fields.get(1).getName());
        Assert.assertEquals(descrErrorMsg, "same[2]", fields.get(1).getDescription());

        Assert.assertEquals(nameErrorMsg, "different", fields.get(2).getName());
        Assert.assertEquals(descrErrorMsg, "different", fields.get(2).getDescription());
    }

    /**
     * Tests that resulting list is not being cached.
     */
    @Test
    public void noCaching() {
        XmlDataSourceCompatibleJRFieldListBuilder builder = new XmlDataSourceCompatibleJRFieldListBuilder().field("first");
        Assert.assertNotSame("Unexpected instance", builder.build(), builder.build());
    }

    @Test
    public void emptyList() {
        List<JRField> fields = new XmlDataSourceCompatibleJRFieldListBuilder().build();
        Assert.assertEquals("Unexpected number of fields", 0, fields.size());
    }

}
