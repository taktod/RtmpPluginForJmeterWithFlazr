package com.ttProject.jmeter.rtmp.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

/**
 * 設定GUI定義
 * @author taktod
 */
public class RtmpConnectConfigBeanInfo extends BeanInfoSupport{
	/**
	 * コンストラクタ
	 */
	public RtmpConnectConfigBeanInfo() {
		super(RtmpConnectConfig.class);

		// 基本設定
		createPropertyGroup("BaseSetting", new String[]{
				"variableName"});

		// 通常設定
		createPropertyGroup("Setting", new String[]{
				"rtmpUrl",
				"pageUrl",
				"swfUrl"
		});
		
		// variableNameはデフォルトを設定しておく。
		PropertyDescriptor p;
		p = property("variableName");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "rtmp");
		
		// rtmpUrlもデフォルト値を設定しておく
		p = property("rtmpUrl");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
	}
}
