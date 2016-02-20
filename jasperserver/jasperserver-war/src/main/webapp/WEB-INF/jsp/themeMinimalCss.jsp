<%@ page contentType="text/css" %>

<%@ taglib uri="/spring" prefix="spring"%>

@import url("${pageContext.request.contextPath}/<spring:theme code="theme.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="pages.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="containers.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="dialog.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="buttons.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="lists.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="controls.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="dataDisplays.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="pageSpecific.css"/>") screen,print;
@import url("${pageContext.request.contextPath}/<spring:theme code="dialogSpecific.css"/>") screen,print;

@import url("${pageContext.request.contextPath}/<spring:theme code="forPrint.css"/>") print;
