<html>
  <head>
  <title>Jirb Web App</title>
  <style type="text/css">
  	body { background:white; color:black; }
  	.description { float:right; border:0; width:35em; text-align:left; }
  	.code_line { color:lime; }
  	.console_output { color:white; }
  	.returned_value { color:aqua; }
  	.error { color:red; }
  	.ruby, #line { padding:0px; margin:0px; font-family:Courier New; font-size:1em; background:black; color:lime; }
  	.console_window { border:2px ridge gray; width:50em; height:35em; overflow:auto; }
  	#line { display:inline; border:0; overflow:hidden; min-height:1.2em; vertical-align: text-top;  }
  </style>
  <script type="text/javascript">
	function lineCount(str) {
		var count=1;
		for(var i=0;i<str.length;i++) {
			if(str[i]=='\n')
				count++;
		}
		return count;
	}

  	function handleEnter(e, form, textEle){
		var noOfLines=lineCount(textEle.value);

		var isEnterPressed = (e.keyCode == 13) || (e.keyCode == 10);

		if (isEnterPressed && (e.ctrlKey != true)) {
			form.submit();
			return false;
		}
		else if (isEnterPressed) {
			textEle.value = textEle.value + "\n ";
			textEle.style.height=(noOfLines+1)+"em";
			textEle.blur();
			textEle.focus();
			return false;

		}
		return true;
	}

	function focusOnLine() {
		document.getElementById('line').focus();
	}

	function clearHistory() {
		document.forms[0].action = 'jirb!clearHistory.action';
		document.forms[0].submit();
	}

</script>
  </head>
  <body onload="focusOnLine();" onclick="focusOnLine()">
  	<h2>Jirb</h2>
  	<form action="jirb.action" method="post">
	  	<div class="description">
		  	Enter JRuby code here (<tt>require 'java'</tt> and <tt>include Java</tt> are not needed)<br>
		  	<br>
		  	(For a block of code, use <tt>Ctrl + Enter</tt> to separate lines)<br>
	  	</div>
	  	<div onfocus="focusOnLine()" class="ruby console_window">

		  	<#if history??>
				<#list history as historyLine>
					${historyLine}<br>
				</#list>
			</#if>

		  	&gt;&gt;&nbsp;<textarea rows="1" cols="70" onkeypress="return handleEnter(event, this.form, this);" name="line" id="line"></textarea>
	  	</div>
	  	<input type="button" value="Clear" onclick="clearHistory()">
  	</form>

	<p><small>Developed by <a href="http://www.wikyblog.com/AmanKing">Aman King</a> from <a href="http://www.thoughtworks.com">ThoughtWorks</a></small></p>
  </body>
</html>
