<%--
 memiiso license
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<!DOCTYPE html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="../nifi/assets/jquery-ui-dist/jquery-ui.min.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/assets/slickgrid/slick.grid.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/css/slick-nifi-theme.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/js/jquery/modal/jquery.modal.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/js/jquery/combo/jquery.combo.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/assets/qtip2/dist/jquery.qtip.min.css" type="text/css"/>
    <link rel="stylesheet" href="js/CodeMirror/lib/codemirror.css" type="text/css"/>
    <link rel="stylesheet" href="js/CodeMirror/addon/lint/lint.css" type="text/css">
    <link rel="stylesheet" href="js/CodeMirror/addon/hint/show-hint.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/js/jquery/nfeditor/jquery.nfeditor.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/js/jquery/nfeditor/languages/nfel.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/fonts/flowfont/flowfont.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/assets/font-awesome/css/font-awesome.min.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/assets/reset.css/reset.css" type="text/css"/>
    <link rel="stylesheet" href="css/main.css" type="text/css"/>
    <link rel="stylesheet" href="../nifi/css/common-ui.css" type="text/css"/>

    <script type="text/javascript" src="../nifi/assets/jquery/dist/jquery.min.js"></script>
    <script type="text/javascript" src="../nifi/js/jquery/jquery.center.js"></script>
    <script type="text/javascript" src="../nifi/js/jquery/jquery.each.js"></script>
    <script type="text/javascript" src="../nifi/js/jquery/jquery.tab.js"></script>
    <script type="text/javascript" src="../nifi/js/jquery/modal/jquery.modal.js"></script>
    <script type="text/javascript" src="../nifi/js/jquery/combo/jquery.combo.js"></script>
    <script type="text/javascript" src="../nifi/js/jquery/jquery.ellipsis.js"></script>
    <script type="text/javascript" src="../nifi/assets/jquery-ui-dist/jquery-ui.min.js"></script>
    <script type="text/javascript" src="../nifi/assets/qtip2/dist/jquery.qtip.min.js"></script>
    <script type="text/javascript" src="../nifi/assets/JSON2/json2.js"></script>
    <script type="text/javascript" src="../nifi/assets/jsonlint/lib/jsonlint.js"></script>
    <script type="text/javascript" src="../nifi/js/codemirror/lib/codemirror-compressed.js"></script>
    <!--script type="text/javascript" src="js/CodeMirror/src/codemirror.js"></script-->
    <script type="text/javascript" src="js/CodeMirror/mode/sql/sql.js"></script>
    <script type="text/javascript" src="js/CodeMirror/addon/lint/lint.js"></script>
    <script type="text/javascript" src="js/CodeMirror/addon/lint/json-lint.js"></script>
    <script type="text/javascript" src="js/CodeMirror/addon/hint/show-hint.js"></script>
    <script type="text/javascript" src="js/CodeMirror/addon/hint/sql-hint.js"></script>

    <script type="text/javascript" src="../nifi/js/nf/nf-namespace.js"></script>
    <script type="text/javascript" src="../nifi/js/nf/nf-storage.js"></script>
    <script type="text/javascript" src="../nifi/js/nf/nf-ajax-setup.js"></script>
    <script type="text/javascript" src="../nifi/js/nf/nf-universal-capture.js"></script>
    <script type="text/javascript" src="js/application.js"></script>

    <title>melt CTAS</title>
</head>

<body>
<div id="melt-processor-id" class="hidden">
    <%=request.getParameter("id") == null ? "" : org.apache.nifi.util.EscapeUtils.escapeHtml(request.getParameter("id"))%>
</div>
<div id="melt-client-id" class="hidden">
    <%=request.getParameter("clientId") == null ? "" : org.apache.nifi.util.EscapeUtils.escapeHtml(request.getParameter("clientId"))%>
</div>
<div id="melt-revision" class="hidden">
    <%=request.getParameter("revision") == null ? "" : org.apache.nifi.util.EscapeUtils.escapeHtml(request.getParameter("revision"))%>
</div>
<div id="melt-editable" class="hidden">
    <%=request.getParameter("editable") == null ? "" : org.apache.nifi.util.EscapeUtils.escapeHtml(request.getParameter("editable"))%>
</div><div id="melt-ctas-content">
              <div id="ctas-database-panel">
                  <div id="flowfile-policy-container">
                      <span id="selected-flowfile-policy" class="hidden"></span>
                      <div id="flowfile-policy-label" class="large-label">Schemas</div>
                      <div class="info fa fa-question-circle" title="Defines the behavior when multiple rules match."></div>
                      <div id="flowfile-policy"></div>
                      <div class="clear"></div>
                  </div>
                  <div id="rule-label-container">
                      <div id="rules-label" class="large-label">Tables</div>
                      <div class="info fa fa-question-circle" title="List of all the tables in the selected database"></div>
                      <div class="clear"></div>
                  </div>
                  <div id="table-list-container">
                      <ul id="rule-list"></ul>
                  </div>
                  <div id="no-rules" class="unset">No tables foundd.</div>
              </div>
              <div id="ctas-expression-panel">
                  <div id="expression-button-container">
                      <div id="expression-save" class="button button-normal">Save</div>
                      <div id="expression-validate" class="button button-normal">Validate</div>
                      <div class="clear"></div>
                  </div>
                  <div id="ctas-expression-container">
                      <div class="large-label-container">
                          <div id="conditions-label" class="large-label">New SQL-Expression</div>
                          <div class="info fa fa-question-circle" title="All conditions must be met for this rule to match."></div>
                          <div class="clear"></div>
                      </div>
                      <div id="ctas-expression-container">
                          <textarea id="sqlEditor" name="sqlEditor">
                          create table if not exists table1(
                            a  bigint(13) not null primary key,
                            b  char(4)    not null,
                            c  char(50)   not null,
                            d  int(9)     not null,
                          );

                          insert into table1 values (1234567890123, "b", "c", 0);

                          select from_unixtime(a/1000), b, c, min(d) as `using`
                            from table1 t1
                            left join table2 t2 using (a)
                            -- inner join table3 t3 on t3._a = t1.a
                            join (
                              select a, b, c
                              from data
                            ) as foo on foo.a = t1.a

                            where a &gt; 10
                            and b like '%foo'
                            or c = 3.14159
                            and d &lt; -15.7
                            order by 1 desc
                          ;

                          select @total := sum(d) from data;
                          </textarea>
                      </div>
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