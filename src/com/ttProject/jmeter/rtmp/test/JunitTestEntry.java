package com.ttProject.jmeter.rtmp.test;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.ttProject.jmeter.rtmp.config.RtmpConnectConfig;
import com.ttProject.junit.TestEntry;
import com.ttProject.junit.library.MethodChecker;

/**
 * Junitテストを実行するための下準備
 * @author taktod
 */
public class JunitTestEntry extends TestEntry {
	/**
	 * 初期化処理
	 */
	@Override
	public void setUp() throws Exception {
		// log4j対策
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.DEBUG);
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));

		// 対象パッケージを指定
		setPackagePath("com.ttProject.jmeter.rtmp");

		// (先にconnectionConfigをセットしておく。)
		MethodChecker mc = new MethodChecker();
		JMeterVariables variables = new JMeterVariables();
		JMeterContextService.getContext().setVariables(variables);
		variables.putObject("rtmp", mc.getClassInstance(RtmpConnectConfig.class));
		
		setData("sampleResult", new SampleResult());

		super.setUp();
	}
	/**
	 * dump出力の部分でSampleResultだけ中身がわかるようにしておく。
	 */
	@Override
	public boolean dump(Object obj) {
		if(obj instanceof SampleResult) {
			SampleResult result = (SampleResult) obj;
			System.out.println("[SampleResult]");
			System.out.println("success? : " + result.isSuccessful());
			System.out.println(result.getResponseDataAsString());
			return false;
		}
		return true;
	}
	/**
	 * assume=@customでSampleResultの内容確認するようにした。
	 */
	@Override
	public boolean customCheck(String assume, Object ret) {
		if(ret instanceof SampleResult) {
			String target = assume.replace("@custom", "").replace("[", "").replace("]", "");
			SampleResult result = (SampleResult)ret;
			System.out.println(result.getResponseDataAsString());
			if("".equals(target)) {
				if(result.isSuccessful()) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				if(result.getResponseDataAsString().indexOf(target) != -1) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return super.customCheck(assume, ret);
	}
}
