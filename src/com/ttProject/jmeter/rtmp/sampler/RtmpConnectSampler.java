package com.ttProject.jmeter.rtmp.sampler;

import java.util.Map;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.ThreadListener;

import com.flazr.rtmp.message.Command;
import com.ttProject.jmeter.rtmp.library.IRtmpClientEx;
import com.ttProject.jmeter.rtmp.library.RtmpClientEx;
import com.ttProject.junit.annotation.Init;
import com.ttProject.junit.annotation.Junit;
import com.ttProject.junit.annotation.Test;

/**
 * Rtmpのサーバーへの接続をサンプリングする。
 * @author taktod
 */
@SuppressWarnings("unused")
public class RtmpConnectSampler extends RtmpTimeoutAbstractSampler implements TestBean, ThreadListener {
	/** シリアル番号 */
	private static final long serialVersionUID = -3395716901195949497L;
	/** スレッドごとの動作をするかフラグ */
	private boolean perThread;
	/** 接続後のサーバーからの応答コード */
	private String connectCode;
	/**
	 * 通常のコンストラクタ
	 */
	public RtmpConnectSampler() {
	}
	/**
	 * {@inheritDoc}
	 * @param entry
	 * @return
	 */
	@Override
//	@Junit({
//		@Test("null"),
//		@Test("null"), // 2度目の動作は前のスレッドの動作が存在してしまうので、すでにうごいている動作になってしまう。
//	})
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.sampleStart();
		boolean success = false;
		// 動作前確認
		if(!check(result)) {
			return result;
		}
		// 実験スタート
		success = doConnect();
		setupResult(result, connectCode, success);
		return result;
	}
	/**
	 * 実行前動作確認(エラー時は動作確認にかかった時間をレポートする。)
	 * @param result
	 * @return true:問題なし。 false:問題あり、サンプリング強制終了
	 */
	@Override
	protected boolean check(SampleResult result) {
		if(!checkConfig(result)) {
			return false;
		}
		// rtmpClientが接続しているか確認する。
		if(getRtmpConnectConfig().getRtmpData(isPerThread()).getRtmpClient() != null) {
			// すでに接続が存在する。
			setupResult(result, "rtmpConnection is already established", true);
			return false;
		}
		return true;
	}
	/**
	 * 接続動作
	 * @param result
	 */
	private boolean doConnect() {
		ConnectEvent event = new ConnectEvent(Thread.currentThread());
		RtmpClientEx rtmpClient = new RtmpClientEx(
				getRtmpConnectConfig().getServer(),
				getRtmpConnectConfig().getPort(),
				getRtmpConnectConfig().getApplication(),
				event);
		try {
			boolean result = rtmpClient.connect(null, getRtmpConnectConfig().getSwfUrl(), getRtmpConnectConfig().getPageUrl());
			if(result) {
				Thread.sleep(getTimeOutVal());
				connectCode = "Connection Timeout";
			}
			else {
				connectCode = "Connection failed";
			}
		}
		catch (InterruptedException e) {
			;
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			connectCode = e.getMessage();
		}
		// 接続成功時はスレッドとrtmpClientを関係つけておき、次のサンプラーで利用できるようにしておく。
		if("NetConnection.Connect.Success".equals(connectCode)) {
			getRtmpData().setRtmpClient(rtmpClient);
			return true;
		}
		else {
			rtmpClient.disconnect();
			return false;
		}
	}
	/**
	 * コネクト時のコールバック関数
	 * @author taktod
	 */
	private class ConnectEvent implements IRtmpClientEx {
		private Thread t;
		public ConnectEvent(Thread currentThread) {
			t = currentThread;
		}
		@Override
		public void onConnect(Map<String, Object> obj) {
			connectCode = (String)obj.get("code");
			t.interrupt();
		}
		@Override
		public void onDisconnect() {
		}
		@Override
		public void onInvoke(String funcName, Command command) {
		}
		@Override
		public void onResult(String funcName, Command command) {
		}
	}
	/**
	 * @return the perThread
	 */
	public boolean isPerThread() {
		return perThread;
	}
	/**
	 * @param perThread the perThread to set
	 */
	public void setPerThread(boolean perThread) {
		this.perThread = perThread;
	}
	@Override
	public void threadStarted() {
		System.out.println("start Thread....");
	}
	@Override
	public void threadFinished() {
		System.out.println("stop thread...");
	}





	/**
	 * Junitテスト用のデフォルトデータ入りコンストラクタ
	 * @param config
	 * @param variableName
	 * @param perThread
	 */
	@Init({"rtmp", "4000", "true"})
	private RtmpConnectSampler(String variableName, String timeOut, boolean perThread) {
		setVariableName(variableName);
		setTimeOut(timeOut);
		setPerThread(perThread);
	}
}
