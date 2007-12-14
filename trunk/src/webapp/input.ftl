<html>
  <head>
  <title>Jirb Web App</title>
  <style type="text/css">
  	body { background:white; color:black; }
  	.ruby { padding:0px; margin:0px; font-family:Courier New; font-size:1em; background:black; color:lime; }
  </style>
  <script language="javascript" type="text/javascript">
<!--
function ctrl_enter(e, form, textArea)
{
	var isEnterPressed = (e.keyCode == 13) || (e.keyCode == 10);
	if (isEnterPressed && (e.ctrlKey != true)) 
		form.submit();
	else if (isEnterPressed) {
		textArea.value = textArea.value + "\n ";
		textArea.blur();
		textArea.focus();
	}
}
//-->
</script>
  </head>
  <body onload="document.getElementById('line').focus();" onfocus="document.getElementById('line').focus();">
  	<h2>Jirb</h2>
  	<div style="float:right; border:0; width:35em;">
  	<p style="text-align:left;">
  	<form action="jirb.action" method="post">
  	Enter JRuby code here (<tt>require 'java'</tt> and <tt>include Java</tt> are not needed)<br>
  	<br>
  	(For a block of code, use <tt>Ctrl + Enter</tt> to separate lines)<br>
	</p>
  	</div>
  	<div onfocus="document.getElementById('line').focus();" style="border:2px ridge gray; width:50em; height:35em; overflow:auto; " class="ruby">
  	<#if !history.isEmpty()>
        <#list history as historyLine>
        	${historyLine}<br>
        </#list>
    </#if>
  	<div class="ruby" style="overflow:visible;"><span style="left:0; top:0; float:top;">&gt;&gt;&nbsp;</span>
  	<textarea style="border:0; width:40em; overflow:visible;" class="ruby" type="text" onkeypress="return ctrl_enter(event, this.form, this);" name="line" id="line"></textarea>
  	</div>
  	</div>
  	  	<input type="button" value="Clear" onclick="document.forms[0].action = 'jirb!clearHistory.action';document.forms[0].submit();">
	  	<input type="submit" value="Submit" style="display:none">

  	</form>
  	</p>

  </body>
</html>