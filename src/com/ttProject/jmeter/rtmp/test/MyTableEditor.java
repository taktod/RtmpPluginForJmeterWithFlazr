package com.ttProject.jmeter.rtmp.test;

import java.awt.event.FocusEvent;
import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.gui.TableEditor;

public class MyTableEditor extends TableEditor {
	@Override
	public Object getValue() {
		Object obj = super.getValue();
		System.out.println(obj);
		return obj;
	}
	@Override
	public void setDescriptor(PropertyDescriptor descriptor) {
		System.out.println("setDescriptor is called....");
		System.out.println(descriptor);
		super.setDescriptor(descriptor);
	}
	@Override
	public void focusLost(FocusEvent e) {
		// ここでなにかする？
		System.out.println("getComponent");
		System.out.println(e.getComponent());
		System.out.println("getOppositeComponent");
		System.out.println(e.getOppositeComponent());
		System.out.println("getSource");
		System.out.println(e.getSource());
		super.focusLost(e);
	}
}
