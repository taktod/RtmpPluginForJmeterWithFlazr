package com.ttProject.jmeter.rtmp.sampler;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import com.ttProject.jmeter.rtmp.data.InvokeParameterData;
import com.ttProject.jmeter.rtmp.test.MyTableEditor;

public class RtmpInvokeSamplerBeanInfo extends BeanInfoSupport {
	public RtmpInvokeSamplerBeanInfo() {
		super(RtmpInvokeSampler.class);
		
		createPropertyGroup("Setting", new String[]{
				"timeOut",
				"invokeFunc",
				"parameters"});
		
		PropertyDescriptor p;
		p = property("invokeFunc");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");

		p = property("parameters");
		p.setPropertyEditorClass(MyTableEditor.class);
		p.setValue(MyTableEditor.CLASSNAME, InvokeParameterData.class.getName());
		p.setValue(MyTableEditor.HEADERS, new String[]{"parameter", "note"});
		p.setValue(MyTableEditor.OBJECT_PROPERTIES, new String[]{InvokeParameterData.param, InvokeParameterData.note});
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, new ArrayList<InvokeParameterData>());
	}
}
