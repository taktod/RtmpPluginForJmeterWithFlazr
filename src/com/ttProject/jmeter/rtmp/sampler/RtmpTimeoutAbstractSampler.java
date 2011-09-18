package com.ttProject.jmeter.rtmp.sampler;

/**
 * 共通部分を抜き出したクラスタイムアウトの部分
 * @author taktod
 */
public abstract class RtmpTimeoutAbstractSampler extends RtmpAbstractSampler {
	/** シリアル番号 */
	private static final long serialVersionUID = 15383998085000665L;
	/** タイムアウト設定値 */
	private String timeOut = null;
	/** タイムアウト実利用値 */
	private Long timeOutVal;
	/** タイムアウトのデータがない場合のデフォルトの値 */
	private final long defaultTimeOutVal = 1000L;
	/**
	 * コンストラクタ
	 */
	public RtmpTimeoutAbstractSampler() {
	}
	/**
	 * @return the timeout
	 */
	public String getTimeOut() {
		return timeOut;
	}
	/**
	 * タイムアウトの値
	 * @return
	 */
	public Long getTimeOutVal() {
		if(timeOutVal == null) {
			return defaultTimeOutVal;
		}
		return timeOutVal;
	}
	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeOut(String timeOut) {
		try {
			this.timeOutVal = Long.parseLong(timeOut);
			this.timeOut = timeOut;
		}
		catch (Exception e) {
			this.timeOut = "";
			timeOutVal = null;
		}
	}
}
