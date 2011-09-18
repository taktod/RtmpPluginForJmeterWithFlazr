package com.ttProject.jmeter.rtmp.sampler;

import java.util.Map;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;

import com.flazr.rtmp.message.Command;
import com.ttProject.jmeter.rtmp.library.IRtmpClientEx;
import com.ttProject.jmeter.rtmp.library.RtmpClientEx;

/**
 * 切断操作のサンプリング
 * @author taktod
 */
public class RtmpDisconnectSampler extends RtmpTimeoutAbstractSampler implements TestBean {
	/** シリアルID */
	private static final long serialVersionUID = 4432585069468458531L;
	/** 切断状態 */
	private String disconnectCode;
	/**
	 * コンストラクタ
	 */
	public RtmpDisconnectSampler() {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.sampleStart();
		boolean success = false;
		if(!check(result)) {
			return result;
		}
		success = doDisconnect();
		setupResult(result, disconnectCode, success);
		return result;
	}
	/**
	 * 切断実行
	 */
	private boolean doDisconnect() {
		RtmpClientEx rtmpClient = getRtmpData().getRtmpClient();
		getRtmpData().setRtmpClient(null);
		rtmpClient.setListener(new DisconnectEvent(Thread.currentThread()));
		rtmpClient.disconnect();
		try {
			Thread.sleep(getTimeOutVal());
			disconnectCode = "Disconnect Timeout";
		}
		catch (InterruptedException e) {
			disconnectCode = "Disconnect ok";
			return true;
		}
		catch (Exception e) {
			disconnectCode = e.getMessage();
		}
		return false;
	}
	/**
	 * 切断イベント
	 * @author taktod
	 */
	private class DisconnectEvent implements IRtmpClientEx {
		private Thread t;
		public DisconnectEvent(Thread currentThread) {
			t = currentThread;
		}
		@Override
		public void onConnect(Map<String, Object> obj) {
		}
		@Override
		public void onDisconnect() {
			t.interrupt();
		}
		@Override
		public void onInvoke(String funcName, Command command) {
		}
		@Override
		public void onResult(String funcName, Command command) {
		}
	}

	@SuppressWarnings("unused")
	private RtmpDisconnectSampler(String variableName, String timeOut) {
		setVariableName(variableName);
		setTimeOut(timeOut);
	}
}
