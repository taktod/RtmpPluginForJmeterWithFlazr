package com.ttProject.jmeter.rtmp.sampler;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class RtmpTimeoutAbstractSamplerBeanInfo extends BeanInfoSupport {
	public RtmpTimeoutAbstractSamplerBeanInfo() {
		super(RtmpTimeoutAbstractSampler.class);
		
		createPropertyGroup("Setting", new String[]{"timeOut"});
		PropertyDescriptor p;
		p = property("timeOut");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "1000");
	}
}
