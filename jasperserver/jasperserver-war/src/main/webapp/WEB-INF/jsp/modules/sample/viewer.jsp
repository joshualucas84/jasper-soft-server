  <%--
    ~ Copyright (C) 2005 - 2014 Jaspersoft Corporation. All rights reserved.
    ~ http://www.jaspersoft.com.
    ~
    ~ Unless you have purchased  a commercial license agreement from Jaspersoft,
    ~ the following license terms  apply:
    ~
    ~ This program is free software: you can redistribute it and/or  modify
    ~ it under the terms of the GNU Affero General Public License  as
    ~ published by the Free Software Foundation, either version 3 of  the
    ~ License, or (at your option) any later version.
    ~
    ~ This program is distributed in the hope that it will be useful,
    ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
    ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    ~ GNU Affero  General Public License for more details.
    ~
    ~ You should have received a copy of the GNU Affero General Public  License
    ~ along with this program. If not, see <http://www.gnu.org/licenses/>.
    --%>

    <%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
    <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


    <t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
      <t:putAttribute name="pageTitle" value="Report Viewer by Visualize"/>
      <t:putAttribute name="bodyID" value="reportViewer"/>
      <%--<t:putAttribute name="pageClass" value="test"/>--%>
      <t:putAttribute name="bodyClass" value="oneColumn"/>
    <%--<t:putAttribute name="moduleName" value="jr/require.config"/>--%>
    <t:putAttribute name="moduleName" value="samples/viewer"/>
      <t:putAttribute name="headerContent" >
        <%-- hack for export menu --%>
        <script type="text/javascript" id="toolbarText">
          { "toolbar_export" : [
            {"type": "simpleAction", "text": "As PDF", "action": "Report.exportReport", "actionArgs": ["pdf", "/jasperserver-pro/flow.html/flowFile/AllAccounts.pdf"]},
            {"type": "simpleAction", "text": "As Excel (Paginated)", "action": "Report.exportReport", "actionArgs": ["xls", "/jasperserver-pro/flow.html/flowFile/AllAccounts.xls"]},
            {"type": "simpleAction", "text": "As Excel", "action": "Report.exportReport", "actionArgs": ["xlsNoPag", "/jasperserver-pro/flow.html/flowFile/AllAccounts.xls"]},
            {"type": "simpleAction", "text": "As CSV", "action": "Report.exportReport", "actionArgs": ["csv", "/jasperserver-pro/flow.html/flowFile/AllAccounts.csv"]},
            {"type": "simpleAction", "text": "As DOCX", "action": "Report.exportReport", "actionArgs": ["docx", "/jasperserver-pro/flow.html/flowFile/AllAccounts.docx"]},
            {"type": "simpleAction", "text": "As RTF", "action": "Report.exportReport", "actionArgs": ["rtf", "/jasperserver-pro/flow.html/flowFile/AllAccounts.rtf"]},
            {"type": "simpleAction", "text": "As ODT", "action": "Report.exportReport", "actionArgs": ["odt", "/jasperserver-pro/flow.html/flowFile/AllAccounts.odt"]},
            {"type": "simpleAction", "text": "As ODS", "action": "Report.exportReport", "actionArgs": ["ods", "/jasperserver-pro/flow.html/flowFile/AllAccounts.ods"]},
            {"type": "simpleAction", "text": "As XLSX (Paginated)", "action": "Report.exportReport", "actionArgs": ["xlsx", "/jasperserver-pro/flow.html/flowFile/AllAccounts.xlsx"]},
            {"type": "simpleAction", "text": "As XLSX", "action": "Report.exportReport", "actionArgs": ["xlsxNoPag", "/jasperserver-pro/flow.html/flowFile/AllAccounts.xlsx"]}
          ]}

    </script>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="pageSpecific.css"/>" type="text/css" media="screen,print"/>
        <style type="text/css">

        </style>
      </t:putAttribute>

      <t:putAttribute name="bodyContent" >

      <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
        <t:putAttribute name="containerClass" value="column decorated primary showingToolBar"/>
        <t:putAttribute name="containerID" value="reportViewFrame"/>
        <t:putAttribute name="containerTitle">${reportUnitObject.label}</t:putAttribute>
    <!-- ========== TOOLBAR =========== -->
        <t:putAttribute name="headerContent">
            <script type="text/javascript">

            </script>
            <div id="dataTimestampMessage">??????</div>

            <ul class="list buttonSet">
              <li class="leaf">
                <button id="dataRefreshButton" type="submit" title="Refresh report with latest data." class="button capsule up" disabled="disabled">
                  <span class="wrap"><span class="icon"></span></span>
                </button>
              </li>
            </ul>

            <div id="viewerToolbar" class="toolbar">

              <!-- ========== PAGINATION =========== -->
              <div id="pagination" class="control paging">
                <button id="page_first" type="submit" title="First" class="button action square move toLeft up" disabled="disabled"><span class="wrap"><span class="icon"></span></span></button>
                <button id="page_prev" type="submit" title="Previous" class="button action square move left up" disabled="disabled"><span class="wrap"><span class="icon"></span></span></button>
                <label class="control input text inline" for="page_current" title="Current Page">
                  <span class="wrap">Page</span>
                  <input class="" id="page_current" type="text" name="currentPage" value="">
                  <span class="wrap" id="page_total"></span>
                </label>
                <button id="page_next" type="submit" title="Next" class="button action square move right up" disabled="disabled"><span class="wrap"><span class="icon"></span></span></button>
                <button id="page_last" type="submit" title="Last" class="button action square move toRight up" disabled="disabled"><span class="wrap"><span class="icon"></span></span></button>
              </div>

              <ul id="asyncIndicator" class="list buttonSet">
                <li class="leaf">
                  <button id="asyncCancel" class="button capsule text up">
                    <span class="wrap">Cancel Loading Data<span class="icon"></span></span>
                  </button>
                </li>
              </ul>


              <ul class="list buttonSet">
                <li class="node">
                  <ul class="list buttonSet">

                    <li class="leaf">
                      <button id="fileOptions" class="button capsule mutton up first" title="Save" disabled="disabled">
                        <span class="wrap"><span class="icon"></span><span class="indicator"></span></span>
                      </button>
                    </li>


                    <li class="leaf">
                      <button id="export" class="button capsule mutton up last" title="Export" disabled="disabled">
                        <span class="wrap"><span class="icon"></span><span class="indicator"></span></span>
                      </button>
                    </li>
                  </ul>
                </li>
                <li class="node">
                  <ul class="list buttonSet">
                    <li class="leaf">
                      <button id="undo" class="button capsule up first" disabled="disabled" title="Undo" disabled="disabled">
                        <span class="wrap"><span class="icon"></span></span>
                      </button>
                    </li>
                    <li class="leaf">
                      <button id="redo" class="button capsule up middle" disabled="disabled" title="Redo" disabled="disabled">
                        <span class="wrap"><span class="icon"></span></span>
                      </button>
                    </li>
                    <li class="leaf">
                      <button id="undoAll" class="button capsule up last" disabled="disabled" title="Undo All" disabled="disabled">
                        <span class="wrap"><span class="icon"></span></span>
                      </button>
                    </li>
                  </ul>
                </li>
                <li class="node">
                  <ul class="list buttonSet">
                    <li id="controls" class="leaf hidden">
                      <button id="ICDialog" class="button capsule up" title="Options" disabled="disabled">
                        <span class="wrap"><span class="icon"></span></span>
                      </button>
                    </li>
                  </ul>
                </li>
                <li class="node">
                  <ul class="list buttonSet">
                    <li class="leaf">
                      <button id="bookmarksDialog" style="" class="button capsule up" title="Bookmarks" disabled="disabled">
                        <span class="wrap"><span class="icon"></span></span>
                      </button>
                    </li>
                  </ul>
                </li>
              </ul>


              <ul class="list buttonSet">
                <li class="leaf">
                  <button id="back" class="button capsule text up" disabled="disabled">
                    <span class="wrap">Back<span class="icon"></span></span>
                  </button>
                </li>
              </ul>


              <div id="reportSearch" class="control search">
                <span class="divider first"></span>
                <label for="search_report" class="control input textPlus inline">
                  <input id="search_report" type="text" placeholder="search report" title="Search Report" name="search_report" disabled="disabled">
                  <button id="search_report_button" class="button search" title="Search Report" disabled="disabled">
                    <span class="icon"></span>
                  </button>
                  <button id="search_options" class="button disclosure" title="Search Options" disabled="disabled">
                    <span class="icon"></span>
                  </button>
                </label>
                <button id="search_previous" title="Search Previous" class="button action square move searchPrevious up" disabled="disabled" disabled="disabled">
                  <span class="wrap"><span class="icon"></span></span>
                </button>
                <button id="search_next" title="Search Next" class="button action square move searchNext up" disabled="disabled" disabled="disabled">
                  <span class="wrap"><span class="icon"></span></span>
                </button>
                <span class="divider last"></span>
              </div>
              <script type="application/json" id="reportSearchText">
              [
              {"key": "caseSensitive", "value": "Case Sensitive"},
              {"key": "wholeWordsOnly", "value": "Whole Words Only"}
              ]
              </script>

              <div id="reportZoom" class="control zoom">
                <span class="divider first"></span>
                <button id="zoom_out" title="Zoom Out" class="button action square move zoomOut up" disabled="disabled">
            <span class="wrap"><span class="icon"></span></span>
            </button>
                <button id="zoom_in" title="Zoom In" class="button action square move zoomIn up" disabled="disabled">
                  <span class="wrap"><span class="icon"></span></span>
                </button>
                <label for="zoom_value" class="control input textPlus inline">
                  <input id="zoom_value" type="text" value="100%" name="zoom_value" title="Zoom Options" disabled="disabled">
                  <button id="zoom_value_button" class="button disclosure" title="Zoom Options" disabled="disabled">
                  <span class="icon"></span>
                </button>
                </label>
              </div>
              <script type="application/json" id="reportZoomText">
              [
              {"key": "0.1", "value": "10%"}, {"key": "0.25", "value": "25%"}, {"key": "0.5", "value": "50%"}, {"key": "0.75",
              "value": "75%"},
              {"key": "1", "value": "100%"}, {"key": "1.25", "value": "125%"}, {"key": "2", "value": "200%"}, {"key": "4",
              "value": "400%"},
              {"key": "8", "value": "800%"}, {"key": "16", "value": "1600%"}, {"key": "24", "value": "2400%"}, {"key": "32",
              "value": "3200%"},
              {"key": "64", "value": "6400%"},
              {"key": "fit_actual", "value": "Actual Size"},
              {"key": "fit_width", "value": "Fit Width"},
              {"key": "fit_height", "value": "Fit Height"},
              {"key": "fit_page", "value": "Fit Page"}
              ]
              </script>
              </div>


            <div id="ajaxbuffer" style="display: none;"></div>


        </t:putAttribute>
        <t:putAttribute name="bodyContent">
          <div id="reportContainer" class="" style="position:relative;"></div>
        </t:putAttribute>
      </t:insertTemplate> <!-- container.jsp -->
    </t:putAttribute> <!-- bodyContent -->
    <t:putAttribute name="footerContent">
    <!-- custom content here; remove this comment -->
    </t:putAttribute>

    </t:insertTemplate> <!-- page.jsp -->
