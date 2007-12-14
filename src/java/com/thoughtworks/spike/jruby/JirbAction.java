package com.thoughtworks.spike.jruby;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.jruby.Ruby;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;

// Understands how to implement jirb via web

public class JirbAction extends ActionSupport implements SessionAware, Preparable {
	private static final String OUTPUT_SESSION_NAME = "output";

	private static final String HISTORY_SESSION_NAME = "HISTORY";

	private static final String RUNTIME_SESSION_NAME = "runtime";
	
	private String line;
	private Ruby runtime;
	private StringBuffer output;
	private ThreadContext threadContext;
	private Map sessionMap;

	private List<String> history;
	private PrintStream out;

	public void prepare() {
		
		runtime=(Ruby) sessionMap.get(RUNTIME_SESSION_NAME);
		if(runtime==null) {
			output = new StringBuffer();
			sessionMap.put(OUTPUT_SESSION_NAME, output);
			
			StringWriter stringWriter = new StringWriter();
			out=new PrintStream(new OutputStream() {
				public void write(int b) throws IOException {
					output.append((char)b);
				}			
			});
			
			runtime = Ruby.newInstance(Ruby.getDefaultInstance().getIn(), out, out);
			
	        runtime.evalScript("require 'java'\n include Java");
	        
			sessionMap.put(RUNTIME_SESSION_NAME, runtime);
			
			history = new ArrayList<String>();
			sessionMap.put(HISTORY_SESSION_NAME, history);
			
		}
		
		history = (List<String>) sessionMap.get(HISTORY_SESSION_NAME);
        threadContext = runtime.getThreadService().getCurrentContext();
        output=(StringBuffer) sessionMap.get(OUTPUT_SESSION_NAME);
        output.replace(0, output.length(), "");
	}


	public String execute() {
		if(line==null)
			return Action.INPUT;
		
		System.out.println("line = "+line);
		addToHistory(">> " + line, true);
		
		try {
			IRubyObject rawRuby = runtime.evalScript(line);
			addToHistory(stripLastNewLineChar(output.toString()));

			String inspectOutput="=> "+rawRuby.callMethod(threadContext, "inspect").toString();
			addToHistory(inspectOutput);
		}
		catch(Exception e) {
			e.printStackTrace();
			addActionError("Your code has error(s)!");
			addToHistory("---- compilation error");
		}
		
        return Action.SUCCESS; 
    }


	private String stripLastNewLineChar(String string) {
		String outputString=string;
		if(outputString.length()>0 && outputString.lastIndexOf('\n')==outputString.length()-1)
			outputString=outputString.substring(0,outputString.length()-1);
		return outputString;
	}
	
	public String clearHistory() {
		System.out.println("clearing history...");
		history.clear();
		return Action.SUCCESS;
	}

	public void setSession(Map sessionMap) {
		this.sessionMap=sessionMap;
	}
	
	public void setLine(String line) {
		this.line = line;
	}

	public String escapeString(String string, boolean addNoBreakSpaces) {
		return string.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\n", "<br>"+(addNoBreakSpaces?"&nbsp;&nbsp;&nbsp;&nbsp;":""));
	}
	
	public List<String> getHistory() {
		return history;
	}
	
	private void addToHistory(String string, boolean addNoBreakSpaces) {
		String lastOutput=escapeString(string, addNoBreakSpaces);
		if(lastOutput.length()!=0) {
			history.add(lastOutput);
		}
	}
	private void addToHistory(String string) {
		addToHistory(string, false);
	}
}
