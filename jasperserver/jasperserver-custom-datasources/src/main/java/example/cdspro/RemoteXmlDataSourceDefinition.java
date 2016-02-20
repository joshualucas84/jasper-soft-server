package example.cdspro;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.data.DataAdapter;
import net.sf.jasperreports.data.DataAdapterService;
import net.sf.jasperreports.data.xml.RemoteXmlDataAdapter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.data.JaxenXmlDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.AbstractTextDataSourceDefinition;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;

/**
 * A definition of an XML datasource which accesses a remote XML file.
 */
public class RemoteXmlDataSourceDefinition extends AbstractTextDataSourceDefinition {

    private static final long serialVersionUID = -8892282755896154869L;

    @Override
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customReportDataSource) throws Exception {
        Map<String, Object> properties = getDataSourceServicePropertyMap(customReportDataSource, new HashMap<String, Object>());

        String uri = (String) properties.get("fileName");
        String query = (String) properties.get("selectExpression");

        JaxenXmlDataSource jrDataSource = new JaxenXmlDataSource(uri, query);
        List<JRField> fields = createStringFields(jrDataSource);
        setFieldTypes(jrDataSource, fields);

        return CustomDomainMetadataUtils.createCustomDomainMetaData("RemoteXml", fields);
    }

    private void setFieldTypes(JaxenXmlDataSource jrDataSource, List<JRField> fields) throws JRException {
        jrDataSource.moveFirst();
        List<String> types = getFieldTypes(jrDataSource, fields);
        Iterator<String> typeIter = types.iterator();
        Iterator<JRField> fieldIter = fields.iterator();
        while (typeIter.hasNext() && fieldIter.hasNext()) {
            ((JRDesignField) fieldIter.next()).setValueClassName(typeIter.next());
        }
    }

    @Override
    public DataAdapterService getDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter) {
        return new DataSourceContributingRemoteXmlDataAdapterService(jasperReportsContext, (RemoteXmlDataAdapter) dataAdapter);
    }

    private List<JRField> createStringFields(JaxenXmlDataSource jrDataSource) throws Exception {
        jrDataSource.moveFirst();
        XmlDataSourceCompatibleJRFieldListBuilder fieldListBuilder = new XmlDataSourceCompatibleJRFieldListBuilder();
        if (jrDataSource.next()) {
            NodeList xmlFields = jrDataSource.getCurrentNode().getChildNodes();
            int fieldCount = xmlFields.getLength();
            for (int i = 0; i < fieldCount; i++) {
                Node xmlField = xmlFields.item(i);
                if (xmlField.getNodeType() == Node.ELEMENT_NODE) {
                    String fieldName = xmlField.getNodeName();
                    if (fieldName.indexOf(':') < 0) { // don't add if namespace prefix present. Fix later (Bug 41535).
                        fieldListBuilder.field(fieldName);
                    }
                }
            }
        }

        return fieldListBuilder.build();
    }

    @Override
    public Map<String, Object> customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object> propertyValueMap) {
        return propertyValueMap;
    }

}
