package com.thoughtworks.spike.jruby;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import org.jruby.Ruby;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.Preparable;

// Understands how to implement jirb via web

public class JirbAction implements SessionAware, Preparable {
	private static final String OUTPUT_SESSION_NAME = "output";
	private static final String HISTORY_SESSION_NAME = "HISTORY";
	private static final String RUNTIME_SESSION_NAME = "runtime";
	
	private String line;
	private Map sessionMap;

	public void prepare() {
		
		Ruby runtime=getRuntime();
		if(runtime==null) {
			final StringBuffer outputBuffer = new StringBuffer();
			
			runtime=prepareRuntime(outputBuffer);
	        
			List<String> history = new ArrayList<String>();
	        populateSessionMap(outputBuffer, runtime, history);
		}
		
        clearRubyOutputBuffer();
	}

	private Ruby getRuntime() {
		return (Ruby) sessionMap.get(RUNTIME_SESSION_NAME);
	}

	public List<String> getHistory() {
		return (List<String>) sessionMap.get(HISTORY_SESSION_NAME);
	}

	private void populateSessionMap(final StringBuffer outputBuffer, Ruby runtime, List<String> history) {
		sessionMap.put(OUTPUT_SESSION_NAME, outputBuffer);
		sessionMap.put(RUNTIME_SESSION_NAME, runtime);
		sessionMap.put(HISTORY_SESSION_NAME, history);
	}

	private Ruby prepareRuntime(final StringBuffer output) {
		PrintStream out=new PrintStream(new OutputStream() {
			public void write(int b) throws IOException {
				output.append((char)b);
			}			
		});
		
		Ruby runtime = Ruby.newInstance(Ruby.getDefaultInstance().getIn(), out, out);
		runtime.evalScript("require 'java'\n include Java");
		return runtime;
	}
	
	private String getRubyOutput(){
		return getRubyOutputBuffer().toString();
	}

	private StringBuffer getRubyOutputBuffer() {
		return (StringBuffer) sessionMap.get(OUTPUT_SESSION_NAME);
	}

	private void clearRubyOutputBuffer() {
		StringBuffer outputBuffer=getRubyOutputBuffer();
		outputBuffer.replace(0, outputBuffer.length(), "");
	}

	public String execute() {
		if(line==null)
			return Action.INPUT;
		
		addLineToHistory(">> " + line);
		
		try {			
			IRubyObject rubyReturnValue = getRuntime().evalScript(line);
			addOutputToHistory(stripLastNewLineChar(getRubyOutput()));

			String inspectOutput=inspectRubyReturnValue(rubyReturnValue);
			addOutputToHistory(inspectOutput);
		}
		catch(Exception e) {
			RaiseException raiseException=(RaiseException) e;
			addOutputToHistory("Error: "+raiseException.getException());
		}
		
        return Action.SUCCESS; 
    }

	private String inspectRubyReturnValue(IRubyObject rawRuby) {
		return "=> "+rawRuby.callMethod(getRuntime().getThreadService().getCurrentContext(), "inspect").toString();
	}

	private String stripLastNewLineChar(String string) {
		String outputString=string;
		if(outputString.length()>0 && outputString.lastIndexOf('\n')==outputString.length()-1)
			outputString=outputString.substring(0,outputString.length()-1);
		return outputString;
	}
	
	public String clearHistory() {
		getHistory().clear();
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
	
	private void addToHistory(String string, boolean addNoBreakSpaces) {
		String escapedString=escapeString(string, addNoBreakSpaces);
		if(escapedString.length()!=0) {
			getHistory().add(escapedString);
		}
	}
	private void addOutputToHistory(String string) {
		addToHistory(string, false);
	}
	private void addLineToHistory(String string) {
		addToHistory(string, true);
	}

}
