package com.thoughtworks.spike.jruby;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.Action;

import jline.History;
import junit.framework.TestCase;

public class JirbActionTest extends TestCase {

	private JirbAction action;
	private Map sessionMap;

	public void setUp() {
		sessionMap=new HashMap();
		action=createAction();
	}

	private JirbAction createAction() {
		JirbAction jirbAction=new JirbAction();
		jirbAction.setSession(sessionMap);
		jirbAction.prepare();
		return jirbAction;
	}
	
	public void testShouldReturnSuccessWithOutputOnNonPuts() {
		action.setLine("java.lang.StringBuffer.new 'Aman'");

        assertEquals(Action.SUCCESS, action.execute());

        assertEquals("<span class='code_line'>&gt;&gt; java.lang.StringBuffer.new 'Aman'</span>", action.getHistory().get(1).toString());
        assertTrue("Should show object description in a span but got '"+action.getHistory().get(2).toString()+"'", action.getHistory().get(2).toString().matches("<span class='returned_value'>=&gt; #&lt;Java::JavaLang::StringBuffer:(.*?) @java_object=Aman&gt;</span>"));
    }

	public void testShouldReturnSuccessWithOutputOnMultilineNonPuts() {
		action.setLine("java.lang.StringBuffer.new 'Aman'\nstr='aman'");

        assertEquals(Action.SUCCESS, action.execute());

        assertEquals("<span class='code_line'>&gt;&gt; java.lang.StringBuffer.new 'Aman'<br>&nbsp;&nbsp;&nbsp;str='aman'</span>", action.getHistory().get(1).toString());
        assertEquals("<span class='returned_value'>=&gt; \"aman\"</span>", action.getHistory().get(2).toString());
    }

	public void testShouldReturnSuccessWithOutputOnPuts() {
		action.setLine("puts java.lang.StringBuffer.new('Aman')");

		assertEquals(Action.SUCCESS, action.execute());

        assertEquals("<span class='code_line'>&gt;&gt; puts java.lang.StringBuffer.new('Aman')</span>", action.getHistory().get(1).toString());
		assertEquals("<span class='console_output'>Aman</span>", action.getHistory().get(2).toString());
        assertEquals("<span class='returned_value'>=&gt; nil</span>", action.getHistory().get(3).toString());
    }

	public void testShouldRetainContextOverSeparateInvocationsInASession() {
		action.setLine("str='Aman'");
		action.execute();
		
		JirbAction actionOnNextInvocation=createAction();
		actionOnNextInvocation.setLine("str");
		
		actionOnNextInvocation.execute();

        assertEquals("<span class='code_line'>&gt;&gt; str='Aman'</span>", action.getHistory().get(1).toString());
        assertEquals("<span class='returned_value'>=&gt; \"Aman\"</span>", action.getHistory().get(2).toString());
        assertEquals("<span class='code_line'>&gt;&gt; str</span>", action.getHistory().get(3).toString());
        assertEquals("<span class='returned_value'>=&gt; \"Aman\"</span>", action.getHistory().get(4).toString());
	}

	public void testShouldRetainContextOverPutsInSeparateInvocationsInASession() {
		action.setLine("str='Aman'");
		action.execute();
		
		JirbAction actionOnNextInvocation=createAction();
		actionOnNextInvocation.setLine("puts str");
		
		actionOnNextInvocation.execute();

        assertEquals("<span class='code_line'>&gt;&gt; str='Aman'</span>", action.getHistory().get(1).toString());
        assertEquals("<span class='returned_value'>=&gt; \"Aman\"</span>", action.getHistory().get(2).toString());
        assertEquals("<span class='code_line'>&gt;&gt; puts str</span>", action.getHistory().get(3).toString());
        assertEquals("<span class='console_output'>Aman</span>", action.getHistory().get(4).toString());
        assertEquals("<span class='returned_value'>=&gt; nil</span>", action.getHistory().get(5).toString());
	}

	public void testShouldNotThrowExceptionOnEvaluationErrorButDisplayError() {
		action.setLine("requireX 'java'");
		action.execute();

        assertEquals("<span class='code_line'>&gt;&gt; requireX 'java'</span>", action.getHistory().get(1).toString());
        assertEquals("<span class='error'>NoMethodError: undefined method `requireX' for main:Object</span>", action.getHistory().get(2).toString());
    }
	
	public void testShouldRetainInputAndOutputAsHistory() {
		action.setLine("str='Aman'");
		action.execute();
		
		JirbAction  nextAction=createAction();
		nextAction.setLine("puts str");
		nextAction.execute();
		
		assertEquals(1+5, nextAction.getHistory().size());
	}
	
	public void testShouldNotReturnNullHistory() {
		assertNotNull(action.getHistory());
	}
}
