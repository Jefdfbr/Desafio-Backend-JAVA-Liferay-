<%@ taglib prefix="aui"          uri="http://liferay.com/tld/aui" %>
<%@ taglib prefix="portlet"      uri="http://java.sun.com/portlet_2_0" %>
<%@ taglib prefix="liferay-ui"   uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="liferay-theme" uri="http://liferay.com/tld/theme" %>
<%@ taglib prefix="c"            uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="com.liferay.document.library.kernel.service.DLAppLocalServiceUtil" %>
<%@ page import="com.liferay.portal.kernel.repository.model.FileEntry" %>
<%@ page import="com.liferay.portal.kernel.servlet.SessionErrors" %>
<%@ page import="com.liferay.portal.kernel.servlet.SessionMessages" %>
<%@ page import="com.liferay.portal.kernel.theme.ThemeDisplay" %>
<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>
<%@ page import="com.liferay.portal.kernel.util.WebKeys" %>
<%@ page import="com.seatecnologia.todo.model.SubTask" %>
<%@ page import="com.seatecnologia.todo.model.Task" %>
<%@ page import="com.seatecnologia.todo.service.TaskLocalService" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.List" %>

<portlet:defineObjects />
<liferay-theme:defineObjects />
