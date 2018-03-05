<%--
 memiiso license
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet"
	href="../nifi/assets/jquery-ui-dist/jquery-ui.min.css" type="text/css" />
<link rel="stylesheet" href="../nifi/assets/slickgrid/slick.grid.css"
	type="text/css" />
<link rel="stylesheet" href="../nifi/css/slick-nifi-theme.css"
	type="text/css" />
<link rel="stylesheet" href="../nifi/js/jquery/modal/jquery.modal.css"
	type="text/css" />
<link rel="stylesheet" href="../nifi/js/jquery/combo/jquery.combo.css"
	type="text/css" />
<link rel="stylesheet"
	href="../nifi/assets/qtip2/dist/jquery.qtip.min.css" type="text/css" />
<link rel="stylesheet" href="../nifi/js/codemirror/lib/codemirror.css"
	type="text/css" />
<link rel="stylesheet"
	href="../nifi/js/codemirror/addon/hint/show-hint.css" type="text/css" />
<link rel="stylesheet"
	href="../nifi/js/jquery/nfeditor/jquery.nfeditor.css" type="text/css" />
<link rel="stylesheet"
	href="../nifi/js/jquery/nfeditor/languages/nfel.css" type="text/css" />
<link rel="stylesheet" href="../nifi/fonts/flowfont/flowfont.css"
	type="text/css" />
<link rel="stylesheet"
	href="../nifi/assets/font-awesome/css/font-awesome.min.css"
	type="text/css" />
<link rel="stylesheet" href="../nifi/assets/reset.css/reset.css"
	type="text/css" />
<link rel="stylesheet" href="css/main.css" type="text/css" />
<link rel="stylesheet" href="../nifi/css/common-ui.css" type="text/css" />
<script type="text/javascript"
	src="../nifi/assets/jquery/dist/jquery.min.js"></script>
<script type="text/javascript" src="../nifi/js/jquery/jquery.center.js"></script>
<script type="text/javascript" src="../nifi/js/jquery/jquery.each.js"></script>
<script type="text/javascript" src="../nifi/js/jquery/jquery.tab.js"></script>
<script type="text/javascript"
	src="../nifi/js/jquery/modal/jquery.modal.js"></script>
<script type="text/javascript"
	src="../nifi/js/jquery/combo/jquery.combo.js"></script>
<script type="text/javascript"
	src="../nifi/js/jquery/jquery.ellipsis.js"></script>
<script type="text/javascript"
	src="../nifi/assets/jquery-ui-dist/jquery-ui.min.js"></script>
<script type="text/javascript"
	src="../nifi/assets/qtip2/dist/jquery.qtip.min.js"></script>
<script type="text/javascript" src="../nifi/assets/JSON2/json2.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/lib/jquery.event.drag-2.3.0.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/plugins/slick.cellrangedecorator.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/plugins/slick.cellrangeselector.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/plugins/slick.cellselectionmodel.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/plugins/slick.rowselectionmodel.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/slick.formatters.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/slick.editors.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/slick.dataview.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/slick.core.js"></script>
<script type="text/javascript"
	src="../nifi/assets/slickgrid/slick.grid.js"></script>
<script type="text/javascript"
	src="../nifi/js/codemirror/lib/codemirror-compressed.js"></script>
<script type="text/javascript" src="../nifi/js/nf/nf-namespace.js"></script>
<script type="text/javascript" src="../nifi/js/nf/nf-storage.js"></script>
<script type="text/javascript" src="../nifi/js/nf/nf-ajax-setup.js"></script>
<script type="text/javascript"
	src="../nifi/js/nf/nf-universal-capture.js"></script>
<script type="text/javascript"
	src="../nifi/js/jquery/nfeditor/languages/nfel.js"></script>
<script type="text/javascript"
	src="../nifi/js/jquery/nfeditor/jquery.nfeditor.js"></script>
<script type="text/javascript" src="js/application.js"></script>
<title>melt CTAS</title>
</head>
<body>
	<div id="melt-processor-id" class="hidden"><%=request.getParameter("id") == null ? ""
					: org.apache.nifi.util.EscapeUtils.escapeHtml(request.getParameter("id"))%></div>
	<div id="melt-client-id" class="hidden"><%=request.getParameter("clientId") == null ? ""
					: org.apache.nifi.util.EscapeUtils.escapeHtml(request.getParameter("clientId"))%></div>
	<div id="melt-revision" class="hidden"><%=request.getParameter("revision") == null ? ""
					: org.apache.nifi.util.EscapeUtils.escapeHtml(request.getParameter("revision"))%></div>
	<div id="melt-editable" class="hidden"><%=request.getParameter("editable") == null ? ""
					: org.apache.nifi.util.EscapeUtils.escapeHtml(request.getParameter("editable"))%></div>
	<div id="melt-CTAS-content">
		<div id="melt-database-panel">
			<div id="melt-database-scheme-container">
				<span id="selected-flowfile-policy" class="hidden"></span>
				<div id="flowfile-policy-label" class="large-label">Database
					schemas</div>
				<div class="info fa fa-question-circle"
					title="Defines the behavior when multiple rules match. Use clone will ensure that each matching rule is executed with a copy of the original flowfile. Use original will execute all matching rules with the original flowfile in the order specified below."></div>
				<div id="flowfile-policy"></div>
				<div class="clear"></div>
			</div>
			<div id="melt-database-scheme-table-container">
				<div id="rules-label" class="large-label">Tables</div>
				<div class="info fa fa-question-circle"
					title="Click and drag to change the order that rules are evaluated."></div>
				<button id="new-rule" class="new-rule hidden fa fa-plus"></button>
				<div class="clear"></div>
			</div>
		</div>
		<div id="melt-CTAS-panel">
			<div id="selected-rule-name-container" class="selected-rule-detail">
				<div class="large-label">CTAS</div>
				<div id="selected-rule-id" class="hidden"></div>
				<input type="text" id="selected-rule-name" class=""></input>
			</div>
			<div class="clear"></div>
		</div>
		<div id="message-and-save-container">
			<div id="message"></div>
			<div id="selected-rule-save" class="button hidden">Save</div>
		</div>
		<div class="clear"></div>
		<div id="glass-pane"></div>
		<div id="ok-dialog" class="small-dialog">
			<div id="ok-dialog-content" class="dialog-content"></div>
		</div>
		<div id="yes-no-dialog" class="small-dialog">
			<div id="yes-no-dialog-content" class="dialog-content"></div>
		</div>
	</div>
</body>
</html>