package com.ttProject.jmeter.rtmp.library;

import java.util.Map;

import com.flazr.rtmp.message.Command;

/**
 * Interface for Flazr
 * @author taktod
 */
public interface IRtmpClientEx {
	public void onConnect(Map<String, Object> obj);
	public void onDisconnect();
//	public void onCreateStream(Integer streamId);
	public void onInvoke(String funcName, Command command);
	public void onResult(String funcName, Command command);
}
