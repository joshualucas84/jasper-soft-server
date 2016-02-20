package example.cdspro;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility class which helps with various tasks related to JSON datasource.
 */
public final class JsonDataSourceUtils {

    /**
     * "Special" string which we tag a "special" field with.
     */
    private static final String TAG = "tag";

    /**
     * Runs the query against the resource and returns fields in the query result.
     * @param fileName - the JSON resource
     * @param query - the query
     * @return row iterator produced by the query
     * @throws JRException - in case of any error
     */
    public static RowExtractor getRowExtractor(String fileName, String query) throws JRException {
        return new RowExtractor(fileName, query);
    }

    /**
     * Runs the query against the resource and returns fields in the query result.
     * @param is - the JSON input stream
     * @param query - the query
     * @return row iterator produced by the query
     * @throws JRException - in case of any error
     */
    public static RowExtractor getRowExtractor(InputStream is, String query) throws JRException {
        return new RowExtractor(is, query);
    }

    /**
     * Creates a special field with a recognizable name.
     */
    private static JRField createTaggedField() {
        JRDesignField taggedField = new JRDesignField();
        taggedField.setName(TAG);
        taggedField.setValueClassName(Object.class.getName());
        return taggedField;
    }

    /**
     * Creates a field from the map entry.
     * @param fieldEntry - the map entry
     */
    private static JRField createField(Map.Entry<String, JsonNode> fieldEntry) {
        JRField field;
        if (fieldEntry.getValue().isBoolean()) {
            field = createField(fieldEntry.getKey(), Boolean.class.getName());
        } else if (fieldEntry.getValue().isInt()) {
            field = createField(fieldEntry.getKey(), Integer.class.getName());
        } else if (fieldEntry.getValue().isLong()) {
            field = createField(fieldEntry.getKey(), Long.class.getName());
        } else if (fieldEntry.getValue().isDouble()) {
            field = createField(fieldEntry.getKey(), Double.class.getName());
        } else if (fieldEntry.getValue().isBigDecimal()) {
            field = createField(fieldEntry.getKey(), BigDecimal.class.getName());
        } else if (fieldEntry.getValue().isBigInteger()) {
            field = createField(fieldEntry.getKey(), BigInteger.class.getName());
        } else if (fieldEntry.getValue().isTextual()) {
            field = createField(fieldEntry.getKey(), String.class.getName());
        } else {
            field = createField(fieldEntry.getKey(), Object.class.getName());
        }
        return field;
    }

    /**
     * Creates a field with given name and value class. The name is used as the field name as well as description.
     * @param fieldName - the field name
     * @param valueClassName - the field value class name
     */
    private static JRField createField(String fieldName, String valueClassName) {
        JRDesignField field = new JRDesignField();
        field.setName(fieldName);
        field.setValueClassName(valueClassName);
        field.setDescription(fieldName);
        return field;
    }

    private JsonDataSourceUtils() {
        // prohibit instantiation
    }

    /**
     * Helps extract fields from the result of the query execution.
     */
    public static final class RowExtractor extends JsonDataSource {

        public RowExtractor(String fileName, String selectExpression) throws JRException {
            super(fileName, selectExpression);
            // at this point the resource is fully read and can be released.
            close();
        }

        public RowExtractor(InputStream is, String selectExpression) throws JRException {
            super(is, selectExpression);
            // at this point the resource is fully read and can be released.
            close();
        }

        @Override
        protected JsonNode getJsonData(JsonNode rootNode, String jsonExpression) throws JRException {
            // This method is called once during the instantiation and once for each getFieldValue() call.
            // Here we let the instantiation call trough but intercept the call from inside getFieldValue().
            // We call getFieldValue() ourselves and pass a special field with a special name so here we can recognize it
            // and return back the root node.
            if (jsonExpression == TAG) {
                return rootNode;
            }
            return super.getJsonData(rootNode, jsonExpression);
        }

        private JsonNode getRow() throws JRException {
            // getting just the first row is enough
            return next() ? (JsonNode) getFieldValue(createTaggedField()) : null;
        }

        public List<JRField> getNextRowFields()  throws JRException {
            JsonNode row = getRow();
            if (row == null) return null;
            List<JRField> fields = new ArrayList<JRField>();
            Iterator<Map.Entry<String, JsonNode>> fieldIter = row.fields();
            while (fieldIter.hasNext()) {
                JRField field = createField(fieldIter.next());
                fields.add(field);
            }
            return fields;
        }
    }
}
