package com.ttProject.jmeter.rtmp.data;

import com.ttProject.jmeter.rtmp.library.RtmpClientEx;

/**
 * Rtmpの接続データ
 * @author taktod
 */
public class RtmpData {
	/** 接続RtmpClientデータ */
	private RtmpClientEx rtmpClient;
	/**
	 * @return the rtmpClient
	 */
	public RtmpClientEx getRtmpClient() {
		return rtmpClient;
	}
	/**
	 * @param rtmpClient the rtmpClient to set
	 */
	public void setRtmpClient(RtmpClientEx rtmpClient) {
		this.rtmpClient = rtmpClient;
	}
}
