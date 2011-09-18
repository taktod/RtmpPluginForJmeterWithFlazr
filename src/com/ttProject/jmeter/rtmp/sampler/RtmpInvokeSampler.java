package com.ttProject.jmeter.rtmp.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;

import com.flazr.rtmp.message.Command;
import com.ttProject.jmeter.rtmp.data.InvokeParameterData;
import com.ttProject.jmeter.rtmp.library.IRtmpClientEx;
import com.ttProject.jmeter.rtmp.library.RtmpClientEx;
import com.ttProject.junit.annotation.Init;
import com.ttProject.junit.annotation.Junit;
import com.ttProject.junit.annotation.Test;
import com.ttProject.junit.library.MethodChecker;

/**
 * Rtmpサーバー宛にメッセージを実行する。
 * @author taktod
 *
 */
@SuppressWarnings("unused")
public class RtmpInvokeSampler extends RtmpTimeoutAbstractSampler implements TestBean {
	/** シリアル番号 */
	private static final long serialVersionUID = -7937880449517409758L;
	private String invokeResult = null;
	private List<InvokeParameterData> params;

	public RtmpInvokeSampler() {
	}
	@Override
//	@Junit({
//		@Test(value="null", assume="@dumppause"),
//	})
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.sampleStart();
		boolean success = false;
		if(!check(result)) {
			return result;
		}
		success = doInvoke();
		setupResult(result, invokeResult, success);
		return result;
	}
	private boolean doInvoke() {
		InvokeEvent event = new InvokeEvent(Thread.currentThread());
		RtmpClientEx rtmpClient = getRtmpData().getRtmpClient();
		List<String> params = new ArrayList<String>();
		if(this.params != null) {
			for(InvokeParameterData data : this.params) {
				params.add(data.getParam());
			}
		}
		rtmpClient.setListener(event);
		rtmpClient.invoke(getPropertyAsString("invokeFunc"), params.toArray());
		try {
			Thread.sleep(getTimeOutVal());
			invokeResult = "invoke Timeout";
			return false;
		}
		catch (InterruptedException e) {
			;
		}
		catch (Exception e) {
			invokeResult = e.getMessage();
			return false;
		}
		return true;
	}
	private class InvokeEvent implements IRtmpClientEx {
		private Thread t;
		public InvokeEvent(Thread currentThread) {
			t = currentThread;
		}
		@Override
		public void onConnect(Map<String, Object> obj) {
		}
		@Override
		public void onDisconnect() {
		}
		@Override
		public void onInvoke(String funcName, Command command) {
		}		
		@Override
		public void onResult(String funcName, Command command) {
			invokeResult = (String)command.getArg(0);
			t.interrupt();
		}
	}
	/**
	 * @return the invokeFunc
	 */
	public String getInvokeFunc() {
		return getPropertyAsString("invokeFunc");
	}
	/**
	 * @param invokeFunc the invokeFunc to set
	 */
	public void setInvokeFunc(String invokeFunc) {
		setProperty("invokeFunc", invokeFunc);
	}
	/**
	 * @return the test
	 */
	public List<InvokeParameterData> getParameters() {
		System.out.println("getParameters is called...");
		return params;
	}
	/**
	 * @param test the test to set
	 */
	public void setParameters(List<InvokeParameterData> params) {
		System.out.println("setParameters is called....");
		Exception e = new Exception("hogehoge");
		e.printStackTrace(System.out);
		this.params = params;
	}

	
	
	
	
	@Init({"rtmp", "4000", "testFunc"})
	private RtmpInvokeSampler(String variableName, String timeOut, String methodName) throws Exception {
		RtmpConnectSampler connect = (RtmpConnectSampler)new MethodChecker().getClassInstance(RtmpConnectSampler.class);
		setVariableName(variableName);
		setTimeOut(timeOut);
		setInvokeFunc(methodName);
		connect.sample(null);
	}
}
