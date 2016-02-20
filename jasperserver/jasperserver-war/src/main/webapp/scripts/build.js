({
  "dir": "build/optimized/",
  "mainConfigFile": "require.config.js",
  "optimizeCss": "none",
  "optimize": "uglify2",
  "skipDirOptimize": false,
  "removeCombined": false,
  "preserveLicenseComments": false,
  "paths": {
    "common": "bower_components/js-sdk/src/common",
    "jquery": "empty:",
    "prototype": "empty:",
    "report.global": "empty:",
    "csrf.guard": "empty:",
    "wcf.scroll": "empty:",
    "ReportRequireJsConfig": "empty:"
  },
  "shim": {
    "mustache": {
      "init": function () {
                    return Mustache;
                }
    }
  },
  "modules": [
    {
      "name": "jasper",
      "include": [
        "bower_components/jquery/dist/jquery",
        "jqueryNoConflict"
      ]
    },
    {
      "name": "commons.main"
    },
    {
      "name": "dataSource/addDataSourcePage"
    },
    {
      "name": "addDataType.page"
    },
    {
      "name": "addFileResource.page"
    },
    {
      "name": "addInputControl.page"
    },
    {
      "name": "addInputControlQueryInformation.page"
    },
    {
      "name": "addJasperReport.page"
    },
    {
      "name": "addJasperReportLocateControl.page"
    },
    {
      "name": "addJasperReportResourceNaming.page"
    },
    {
      "name": "addJasperReportResourcesAndControls.page"
    },
    {
      "name": "addListOfValues.page"
    },
    {
      "name": "addMondrianXML.page"
    },
    {
      "name": "addOLAPView.page"
    },
    {
      "name": "addQuery.page"
    },
    {
      "name": "addQueryWithResourceLocator.page"
    },
    {
      "name": "adminCustomAttributesPage"
    },
    {
      "name": "adminResetSettingsPage"
    },
    {
      "name": "admin.export.page"
    },
    {
      "name": "admin.import.page"
    },
    {
      "name": "admin.logging.page"
    },
    {
      "name": "admin.options.page"
    },
    {
      "name": "admin.roles.page"
    },
    {
      "name": "admin.users.page"
    },
    {
      "name": "connectionType.page"
    },
    {
      "name": "dataTypeLocate.page"
    },
    {
      "name": "listOfValuesLocate.page"
    },
    {
      "name": "locateDataSource.page"
    },
    {
      "name": "locateMondrianConnectionSource.page"
    },
    {
      "name": "locateQuery.page"
    },
    {
      "name": "locateXmlConnectionSource.page"
    },
    {
      "name": "login.page"
    },
    {
      "name": "olap.view.page"
    },
    {
      "name": "report.viewer.page"
    },
    {
      "name": "plain.report.viewer.page"
    },
    {
      "name": "results.page"
    },
    {
      "name": "messages/details/messageDetails.page"
    },
    {
      "name": "messages/list/messageList.page"
    },
    {
      "name": "scheduler/SchedulerController"
    },
    {
      "name": "encrypt.page"
    },
    {
      "name": "error.system"
    },
    {
      "name": "xdm.remote.page"
    }
  ],
  "fileExclusionRegExp": /(^\.|prototype.*patched\.js|Owasp\.CsrfGuard\.js)/
})