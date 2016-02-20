package example.cdspro;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jasperreports.engine.JRField;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDomainMetaDataImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;

/**
 * A collection of methods for working with custom domain metadata.
 */
public final class CustomDomainMetadataUtils {

    /**
     * Creates custom domain metadata with given query language and fields. Straight field mapping is used (my_field:my_field).
     * 
     * @param queryLanguage - the query language
     * @param fields - the list of fields
     * @return a new instance of custom domain metadata
     */
    public static CustomDomainMetaData createCustomDomainMetaData(String queryLanguage, List<JRField> fields) {
        CustomDomainMetaDataImpl sourceMetadata = new CustomDomainMetaDataImpl();
        sourceMetadata.setQueryLanguage(queryLanguage);
        sourceMetadata.setFieldMapping(new HashMap<String, String>());
        sourceMetadata.setFieldNames(new ArrayList<String>(fields.size()));
        sourceMetadata.setFieldTypes(new ArrayList<String>(fields.size()));
        sourceMetadata.setFieldDescriptions(new ArrayList<String>(fields.size()));
        for (JRField field : fields) {
            String fieldName = getSanitizedFieldName(field);
            sourceMetadata.getFieldMapping().put(fieldName, fieldName);
            sourceMetadata.getFieldNames().add(fieldName);
            sourceMetadata.getFieldTypes().add(getFieldValueClassName(field));
            sourceMetadata.getFieldDescriptions().add(field.getDescription());
        }

        return sourceMetadata;
    }

    /**
     * Returns a field name with all dots (.) replaced with underscore (_).
     */
    private static String getSanitizedFieldName(JRField field) {
        return field.getName().replace(".", "_");
    }

    /**
     * Returns a java class name of the value for the given field if the value type is supported.
     * If the value type is not supported then <code>java.lang.String</code> is used.
     * 
     * @see #supportedTypes
     */
    private static String getFieldValueClassName(JRField field) {
        return supportedTypes.contains(field.getValueClassName()) ? field.getValueClassName() : String.class.getName();
    }

    /**
     * Java class names of supported value types.
     */
    private static Set<String> supportedTypes;

    static {
        supportedTypes = new HashSet<String>();
        supportedTypes.add(String.class.getName());
        supportedTypes.add(Byte.class.getName());
        supportedTypes.add(Short.class.getName());
        supportedTypes.add(Integer.class.getName());
        supportedTypes.add(Long.class.getName());
        supportedTypes.add(Float.class.getName());
        supportedTypes.add(Double.class.getName());
        supportedTypes.add(Number.class.getName());
        supportedTypes.add(java.util.Date.class.getName());
        supportedTypes.add(java.sql.Date.class.getName());
        supportedTypes.add(Time.class.getName());
        supportedTypes.add(Timestamp.class.getName());
        supportedTypes.add(BigDecimal.class.getName());
        supportedTypes.add(BigInteger.class.getName());
        supportedTypes.add(Boolean.class.getName());
        supportedTypes.add(Object.class.getName());
    }

    private CustomDomainMetadataUtils() {
        // prohibit instantiation
    }
}
