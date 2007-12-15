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
	
	public void testShouldReturnInputOnNullScript() {
		assertEquals(Action.INPUT, action.execute());
	}
	
	public void testShouldReturnSuccessWithOutputOnNonPuts() {
		action.setLine("java.lang.StringBuffer.new 'Aman'");
		assertEquals(Action.SUCCESS, action.execute());
		assertTrue("Should contain string value", action.getHistory().get(1).contains("Aman"));
	}

	public void testShouldReturnSuccessWithOutputOnPuts() {
		action.setLine("puts java.lang.StringBuffer.new 'Aman'");

		assertEquals(Action.SUCCESS, action.execute());
		assertTrue("Should contain string value", action.getHistory().get(1).contains("Aman"));
	}

	public void testShouldRetainContextOverSeparateInvocationsInASession() {
		action.setLine("str='Aman'");
		action.execute();
		
		JirbAction actionOnNextInvocation=createAction();
		actionOnNextInvocation.setLine("str");
		
		actionOnNextInvocation.execute();

		assertTrue("Should contain string value", actionOnNextInvocation.getHistory().get(3).contains("Aman"));
		
	}

	public void testShouldRetainContextOverPutsInSeparateInvocationsInASession() {
		action.setLine("str='Aman'");
		action.execute();
		
		JirbAction actionOnNextInvocation=createAction();
		actionOnNextInvocation.setLine("puts str");
		
		actionOnNextInvocation.execute();
		assertTrue("Should contain string value", actionOnNextInvocation.getHistory().get(3).contains("Aman"));		
	}

	public void testShouldNotThrowExceptionOnEvaluationError() {
		action.setLine("requireX 'java'");
		action.execute();
	}
	
	public void testShouldRetainInputAndOutputAsHistory() {
		action.setLine("str='Aman'");
		action.execute();
		
		JirbAction  nextAction=createAction();
		nextAction.setLine("puts str");
		nextAction.execute();
		
		assertEquals(5, nextAction.getHistory().size());
	}
		
}
