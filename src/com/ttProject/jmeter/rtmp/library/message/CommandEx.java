package com.ttProject.jmeter.rtmp.library.message;

import org.jboss.netty.buffer.ChannelBuffer;

import com.flazr.amf.Amf0Object;
import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;

public abstract class CommandEx extends Command {
	public CommandEx(RtmpHeader header, ChannelBuffer in) {
		super(header, in);
	}

	public CommandEx(int transactionId, String name, Amf0Object object, Object ... args) {
		super(transactionId, name, object, args);
	}

	public CommandEx(String name, Amf0Object object, Object ... args) {
		this(0, name, object, args);
	}
	
	public static Command connect(ClientOptions options) {
		Amf0Object object = object(
			pair("app", options.getAppName()),
			pair("flashVer", "WIN 9,0,124,2"),
			pair("tcUrl", options.getTcUrl()),
			pair("fpad", false),
			pair("audioCodecs", 1639.0),
			pair("videoCodecs", 252.0),
        	pair("objectEncoding", 0.0),
        	pair("capabilities", 15.0),
        	pair("videoFunction", 1.0),
        	pair("swfUrl", "http://livechat.fc2.com/hogehoge.swf"),
        	pair("pageUrl", "http://livechat.fc2.com/hogehoge"));
   		if(options.getParams() != null) {
   			object.putAll(options.getParams());
   		}
   		return new CommandAmf0("connect", object, options.getArgs());
	}
	
	public static Command invoke(String funcName, Object... params) {
		return new CommandAmf0(funcName, null, params);
	}
}
