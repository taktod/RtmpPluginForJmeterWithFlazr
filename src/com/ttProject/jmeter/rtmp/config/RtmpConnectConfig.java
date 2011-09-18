package com.ttProject.jmeter.rtmp.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import com.ttProject.jmeter.rtmp.data.RtmpData;
import com.ttProject.junit.annotation.Init;

/**
 * コンフィグデータ
 * @author taktod
 */
public class RtmpConnectConfig extends AbstractTestElement 
	implements TestBean, ConfigElement, TestListener, NoThreadClone {
	/** シリアルバージョンID */
	private static final long serialVersionUID = -1L;

	/** 設定データ名 */
	private String variableName = null;
	/** rtmp接続先 */
	private String rtmpUrl = null;
	/** SWF設置のページのURL */
	private String pageUrl = null;
	/** SWFのURL */
	private String swfUrl = null;

	/** Rtmp接続データ(各スレッド用) */
	private Map<Thread, RtmpData> rtmpData = null;
	/** Rtmp接続データ全体共有用 */
	private RtmpData rtmpDat = null;
	/** 接続先サーバー */
	private String server = null;
	/** 接続ポート */
	private Integer port = null;
	/** 接続アプリケーション */
	private String application = null;

	/**
	 * コンストラクタ
	 */
	public RtmpConnectConfig() {
		rtmpData = new ConcurrentHashMap<Thread, RtmpData>();
	}
	/**
	 * 設定データが有効か確認
	 * @return true:有効 false:無効
	 */
	public boolean isValid() {
		return (server != null && port != null && application != null);
	}
	/**
	 * RtmpDataの設定を応答する。
	 * @return
	 */
	public RtmpData getRtmpData() {
		return getRtmpData(null);
	}
	/**
	 * RtmpDataの設定を応答する。
	 * @param perThread
	 * @return
	 */
	public RtmpData getRtmpData(Boolean perThread) {
		if(perThread == null) {
			// threadごとの指定がない場合はある方を応答する。
			if(rtmpDat != null) {
				return rtmpDat;
			}
			return rtmpData.get(Thread.currentThread());
		}
		if(!perThread) {
			if(rtmpDat == null) {
				rtmpDat = new RtmpData();
			}
			return rtmpDat;
		}
		RtmpData rtmpData = this.rtmpData.get(Thread.currentThread());
		if(rtmpData == null) {
			rtmpData = new RtmpData();
			this.rtmpData.put(Thread.currentThread(), rtmpData);
		}
		return rtmpData;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addConfigElement(ConfigElement paramConfigElement) {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean expectsModification() {
		return false;
	}
	/**
	 * {@inheritDoc}
	 * <pre>テストが完了してもスレッドがのこっている限りここまでこないっぽい。</pre>
	 */
	@Override
	public void testEnded() {
		// テストが終了したときによけいなコネクションがのこっている場合はすべて破棄する。
		for(Entry<Thread, RtmpData> entry : rtmpData.entrySet()) {
			try {
				entry.getValue().getRtmpClient().disconnect();
			}
			catch (Exception e) {
			}
		}
		try {
			rtmpDat.getRtmpClient().disconnect();
		}
		catch (Exception e) {
		}
		rtmpData.clear();
		
		System.out.println("test end...");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void testEnded(String arg0) {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void testIterationStart(LoopIterationEvent arg0) {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void testStarted() {
		System.out.println("test start...");
		JMeterVariables variables = JMeterContextService.getContext().getVariables();
		System.out.println(variableName);
		if(variableName != null && !variableName.equals("")) {
			variables.putObject(variableName, this);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void testStarted(String arg0) {
	}

	/**
	 * @return the variableName
	 */
	public String getVariableName() {
		return variableName;
	}
	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	/**
	 * @return the rtmpUrl
	 */
	public String getRtmpUrl() {
		return rtmpUrl;
	}
	/**
	 * @param rtmpUrl the rtmpUrl to set
	 */
	public void setRtmpUrl(String rtmpUrl) {
		this.rtmpUrl = rtmpUrl;
		try {
			// rtmp://server:port/room or else
			String[] paths = rtmpUrl.split("/", 4);
			String[] addresses = paths[2].split(":");
			server = addresses[0];
			if(addresses.length == 1) {
				port = 1935;
			}
			else {
				port = Integer.parseInt(addresses[1]);
			}
			application = paths[3];
		}
		catch (Exception e) {
			server = null;
			port = null;
			application = null;
		}
	}
	/**
	 * @return the pageUrl
	 */
	public String getPageUrl() {
		return pageUrl;
	}
	/**
	 * @param pageUrl the pageUrl to set
	 */
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	/**
	 * @return the swfUrl
	 */
	public String getSwfUrl() {
		return swfUrl;
	}
	/**
	 * @param swfUrl the swfUrl to set
	 */
	public void setSwfUrl(String swfUrl) {
		this.swfUrl = swfUrl;
	}
	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}
	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}
	/**
	 * @return the application
	 */
	public String getApplication() {
		return application;
	}





	/**
	 * コンストラクタJunitテスト用
	 * @param variableName
	 * @param rtmpUrl
	 * @param pageUrl
	 * @param swfUrl
	 */
	@SuppressWarnings("unused")
	@Init({"rtmp", "rtmp://49.212.39.17/avatarChat", 
		"http://localhost/index.html", "http://localhost/test.swf"})
	private RtmpConnectConfig(
			String variableName,
			String rtmpUrl,
			String pageUrl,
			String swfUrl) {
		this();
		setVariableName(variableName);
		setRtmpUrl(rtmpUrl);
		setPageUrl(pageUrl);
		setSwfUrl(swfUrl);
	}
}
