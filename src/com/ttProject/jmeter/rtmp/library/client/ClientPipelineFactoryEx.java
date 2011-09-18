package com.ttProject.jmeter.rtmp.library.client;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.jmeter.rtmp.library.RtmpClientEx;

public class ClientPipelineFactoryEx implements ChannelPipelineFactory {
	private final ClientOptions options;
	private final RtmpClientEx rtmpClient;
	public ClientPipelineFactoryEx(ClientOptions options, RtmpClientEx rtmpClient) {
		this.options = options;
		this.rtmpClient = rtmpClient;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("handshaker", new ClientHandshakeHandlerEx(options));
		pipeline.addLast("decoder", new RtmpDecoder());
		pipeline.addLast("encoder", new RtmpEncoder());

		pipeline.addLast("handler", new ClientHandlerEx(options, rtmpClient));
		return pipeline;
	}
}
