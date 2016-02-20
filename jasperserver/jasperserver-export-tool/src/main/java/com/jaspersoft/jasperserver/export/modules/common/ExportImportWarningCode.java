package com.jaspersoft.jasperserver.export.modules.common;

/**
 * @author Vasyl Spachynskyi
 * @version $Id: $
 * @since 24.09.2015
 */
public enum ExportImportWarningCode {
    EXPORT_BROKEN_DEPENDENCY("export.broken.dependency"),
    IMPORT_RESOURCE_NOT_FOUND("import.resource.not.found"),
    IMPORT_RESOURCE_DIFFERENT_TYPE("import.resource.different.type.already.exist"),
    IMPORT_RESOURCE_ATTACHED_NOT_EXIST_ORG("import.resource.attached.not.exist.org"),
    IMPORT_RESOURCE_DATA_MISSING("import.resource.data.missing"),
    IMPORT_RESOURCE_URI_TOO_LONG("import.resource.uri.too.long"),
    IMPORT_REFERENCE_RESOURCE_NOT_FOUND("import.reference.resource.not.found"),
    IMPORT_ACCESS_DENIED("import.access.denied");

    private String warningCode;

    ExportImportWarningCode(String warningCode) {
        this.warningCode = warningCode;
    }

    @Override
    public String toString() {
        return warningCode;
    }
}
