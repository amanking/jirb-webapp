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

        Ruby runtime = getRuntime();
        if (runtime == null) {
            final StringBuffer outputBuffer = new StringBuffer();

            runtime = prepareRuntime(outputBuffer);

            List<String> history = new ArrayList<String>();
            history.add("WELCOME TO jirb BY www.thoughtworks.com\n");

            populateSessionMap(outputBuffer, runtime, history);
        }

        clearRubyOutputBuffer();
    }

    private Ruby prepareRuntime(final StringBuffer output) {
        PrintStream out = new PrintStream(new OutputStream() {
            public void write(int b) throws IOException {
                output.append((char) b);
            }
        });

        Ruby runtime = Ruby.newInstance(System.in, out, out);
        runtime.evalScriptlet("require 'java'\n include Java");
        return runtime;
    }

    private void populateSessionMap(StringBuffer outputBuffer, Ruby runtime, List<String> history) {
        sessionMap.put(OUTPUT_SESSION_NAME, outputBuffer);
        sessionMap.put(RUNTIME_SESSION_NAME, runtime);
        sessionMap.put(HISTORY_SESSION_NAME, history);
    }

    private void clearRubyOutputBuffer() {
        StringBuffer outputBuffer = getRubyOutputBuffer();
        outputBuffer.replace(0, outputBuffer.length(), "");
    }

    public String execute() {
        if (line == null)
            return Action.SUCCESS;

        addCodeLineToHistory(line);

        try {
            IRubyObject rubyReturnValue = getRuntime().evalScriptlet(line);
            addConsoleOutputToHistory(stripLastNewLineChar(getRubyConsoleOutput()));

            String inspectOutput = inspectRubyReturnValue(rubyReturnValue);
            addReturnedValueToHistory(inspectOutput);
        }
        catch (RaiseException raiseException) {
            addErrorToHistory(raiseException);
        }

        return Action.SUCCESS;
    }

    private String stripLastNewLineChar(String string) {
        String outputString = string;
        if (outputString.length() > 0 && outputString.lastIndexOf('\n') == outputString.length() - 1)
            outputString = outputString.substring(0, outputString.length() - 1);
        return outputString;
    }

    private String inspectRubyReturnValue(IRubyObject rawRuby) {
        return "=> " + rawRuby.callMethod(getRuntime().getThreadService().getCurrentContext(), "inspect").toString();
    }

    private void addCodeLineToHistory(String string) {
        addToHistory(new DisplayLine.Code(string));
    }

    private void addConsoleOutputToHistory(String string) {
        if (string == null || string.trim().length() == 0)
            return;
        addToHistory(new DisplayLine.ConsoleOutput(string));
    }

    private void addReturnedValueToHistory(String string) {
        addToHistory(new DisplayLine.ReturnedValue(string));
    }

    private void addErrorToHistory(RaiseException exception) {
        String error = exception.getException().getType() + ": " + exception.getException();
        addToHistory(new DisplayLine.Error(error));
    }

    private void addToHistory(DisplayLine line) {
        getHistory().add(line);
    }

    public String clearHistory() {
        getHistory().clear();
        return Action.SUCCESS;
    }

    private Ruby getRuntime() {
        return (Ruby) sessionMap.get(RUNTIME_SESSION_NAME);
    }

    public List<Object> getHistory() {
        return (List<Object>) sessionMap.get(HISTORY_SESSION_NAME);
    }

    private String getRubyConsoleOutput() {
        return getRubyOutputBuffer().toString();
    }

    private StringBuffer getRubyOutputBuffer() {
        return (StringBuffer) sessionMap.get(OUTPUT_SESSION_NAME);
    }

    public void setLine(String line) {
        this.line = line;
    }

    public void setSession(Map sessionMap) {
        this.sessionMap = sessionMap;
    }

    private static abstract class DisplayLine {
        private String line;
        private String cssClass;

        DisplayLine(String line, String cssClass) {
            this.line = line;
            this.cssClass = cssClass;
        }

        public String toString() {
            return "<span class='" + cssClass + "'>" + escapeString(line) + "</span>";
        }

        public String escapeString(String string) {
            return string.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\n", "<br>");
        }

        static class Code extends DisplayLine {
            public Code(String line) {
                super(">> " + line, "code_line");
            }

            public String escapeString(String string) {
                return super.escapeString(string).replaceAll("<br>", "<br>&nbsp;&nbsp;&nbsp;");
            }
        }

        static class ConsoleOutput extends DisplayLine {
            public ConsoleOutput(String line) {
                super(line, "console_output");
            }
        }

        static class ReturnedValue extends DisplayLine {
            public ReturnedValue(String line) {
                super(line, "returned_value");
            }
        }

        static class Error extends DisplayLine {
            public Error(String line) {
                super(line, "error");
            }
        }
    }
}
