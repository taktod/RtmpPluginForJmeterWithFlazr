package com.ttProject.jmeter.rtmp.sampler;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

/**
 * Rtmpのデータ参照の部分のGUI設定
 * @author taktod
 */
public class RtmpAbstractSamplerBeanInfo extends BeanInfoSupport {
	public RtmpAbstractSamplerBeanInfo() {
		super(RtmpAbstractSampler.class);

		/** variableNameだけ独立させておく。 */
		createPropertyGroup("BaseSetting", new String[]{"variableName"});

		PropertyDescriptor p;
		// デフォルト値等の設定
		p = property("variableName");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "rtmp");
	}
}
