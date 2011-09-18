package com.ttProject.jmeter.rtmp.data;

import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * Invoke用のパラメーター設定用の格納データ
 * @author taktod
 */
public class InvokeParameterData extends AbstractTestElement {
	/** シリアルID */
	private static final long serialVersionUID = -1L;
	/** アクセスに利用するkey */
	public static final String param = "param";
	/** アクセスに利用するkey */
	public static final String note = "note";
	public boolean isParameter() {
		return false;
	}
	/**
	 * パラメーターの設定
	 * @param data
	 */
	public void setParam(String data) {
		Exception e = new Exception("setParam");
		e.printStackTrace(System.out);
		setProperty(param, data);
	}
	/**
	 * パラメーターの参照
	 * @return
	 */
	public String getParam() {
		return getProperty(param).getStringValue();
	}
	/**
	 * パラメーターに追加しておくメモデータ設定
	 * @param data
	 */
	public void setNote(String data) {
		setProperty(note, data);
	}
	/**
	 * パラメーターに追加しておくメモデータ参照
	 * @return
	 */
	public String getNote() {
		return getProperty(note).getStringValue();
	}
}
