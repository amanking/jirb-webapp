<html>
  <head>
  <title>Jirb Web App</title>
  <style type="text/css">
  	body { background:white; color:black; }
  	.ruby { padding:0px; margin:0px; font-family:Courier New; font-size:1em; background:black; color:lime; }
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
  	<div onfocus="document.getElementById('line').focus();" style="border:2px ridge gray; width:50em; height:35em; overflow:auto; " class="ruby" >
  	
  	<#if history??>
        <#list history as historyLine>
        	${historyLine}<br>
        </#list>
    </#if>
    
  	&gt;&gt;&nbsp;
  	<textarea style="border:0; overflow:visible; height:1em;" class="ruby" rows="1" cols="70" onkeypress="return handleEnter(event, this.form, this);" name="line" id="line"></textarea>
  	
  	</div>
  	  	<input type="button" value="Clear" onclick="document.forms[0].action = 'jirb!clearHistory.action';document.forms[0].submit();">
	  	<input type="submit" value="Submit" style="display:none">

  	</form>
  	</p>

  </body>
</html>