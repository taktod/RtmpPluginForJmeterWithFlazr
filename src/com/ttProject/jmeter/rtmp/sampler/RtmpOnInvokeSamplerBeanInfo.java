package com.ttProject.jmeter.rtmp.sampler;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class RtmpOnInvokeSamplerBeanInfo extends BeanInfoSupport {
	public RtmpOnInvokeSamplerBeanInfo() {
		super(RtmpOnInvokeSampler.class);

		createPropertyGroup("Setting", new String[]{"timeOut",
				"methodName"});
		PropertyDescriptor p;
		p = property("methodName");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
	}
}
