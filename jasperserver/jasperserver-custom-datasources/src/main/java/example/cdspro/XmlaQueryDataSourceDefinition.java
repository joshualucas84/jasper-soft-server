package example.cdspro;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JRDesignField;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.AbstractTextDataSourceDefinition;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDomainMetaDataImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;

/**
 * A definition of an XMLA datasource.
 */
public class XmlaQueryDataSourceDefinition extends AbstractTextDataSourceDefinition {

    private static final long serialVersionUID = -9093395922486182432L;
    private static final String QUERY_LANGUAGE = "XmlaQuery";

    @Override
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customReportDataSource) throws Exception {
        Map<String, Object> properties = getDataSourceServicePropertyMap(customReportDataSource, new HashMap<String, Object>());

        XmlaQueryDataSourceHelper helper = new XmlaQueryDataSourceHelper(properties);
        List<JRField> fields = helper.getFields();

        setFieldTypes(helper.getDataSource(), fields);

        CustomDomainMetaDataImpl md = (CustomDomainMetaDataImpl) CustomDomainMetadataUtils.createCustomDomainMetaData(QUERY_LANGUAGE, fields);
        md.setQueryText((String) properties.get("query"));

        return md;
    }

    private void setFieldTypes(JRDataSource jrDataSource, List<JRField> fields) throws JRException {
        List<String> types = getFieldTypes(jrDataSource, fields);
        Iterator<String> typeIter = types.iterator();
        Iterator<JRField> fieldIter = fields.iterator();
        while (typeIter.hasNext() && fieldIter.hasNext()) {
            ((JRDesignField) fieldIter.next()).setValueClassName(typeIter.next());
        }
    }

    @Override
    public Map<String, Object> customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object> propertyValueMap) {
        return propertyValueMap;
    }

}
