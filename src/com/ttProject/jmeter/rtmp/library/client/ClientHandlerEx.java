package com.ttProject.jmeter.rtmp.library.client;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.io.flv.FlvWriter;
import com.flazr.rtmp.LoopedReader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.RtmpPublisher;
import com.flazr.rtmp.RtmpReader;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.BytesRead;
import com.flazr.rtmp.message.ChunkSize;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.Control;
import com.flazr.rtmp.message.Metadata;
import com.flazr.rtmp.message.SetPeerBw;
import com.flazr.rtmp.message.WindowAckSize;
import com.flazr.util.ChannelUtils;
import com.flazr.util.Utils;
import com.ttProject.jmeter.rtmp.library.RtmpClientEx;
import com.ttProject.jmeter.rtmp.library.message.CommandEx;

@ChannelPipelineCoverage("one")
public class ClientHandlerEx extends SimpleChannelUpstreamHandler {
	private static final Logger logger = LoggerFactory.getLogger(ClientHandlerEx.class);
	
	private final RtmpClientEx rtmpClient;

	private int transactionId = 1;
	private Map<Integer, String> transactionToCommandMap;
	private ClientOptions options;
	private byte[] swfvBytes;

	private FlvWriter writer;

	private int bytesReadWindow = 2500000;
	private long bytesRead;
	private long bytesReadLastSent;    
	private int bytesWrittenWindow = 2500000;

	private RtmpPublisher publisher;
	private int streamId;    

	public void setSwfvBytes(byte[] swfvBytes) {
		this.swfvBytes = swfvBytes;        
		logger.info("set swf verification bytes: {}", Utils.toHex(swfvBytes));        
	}

	public ClientHandlerEx(ClientOptions options, RtmpClientEx rtmpClient) {
		this.options = options;
		this.rtmpClient = rtmpClient;
		rtmpClient.setClientHandlerEx(this);
		transactionToCommandMap = new HashMap<Integer, String>();        
	}

	private void writeCommandExpectingResult(Channel channel, Command command) {
		final int id = transactionId++;
		command.setTransactionId(id);
		transactionToCommandMap.put(id, command.getName());
		logger.info("sending command (expecting result): {}", command);
		channel.write(command);
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.info("channel opened: {}", e);
		super.channelOpen(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.info("handshake complete, sending 'connect'");
		writeCommandExpectingResult(e.getChannel(), CommandEx.connect(options));
	}
	
	public void channelInvoke(Channel channel, Command command) {
		writeCommandExpectingResult(channel, command);
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.info("channel closed: {}", e);
		if(writer != null) {
			writer.close();
		}
		if(publisher != null) {
			publisher.close();
		}
		super.channelClosed(ctx, e);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent me) {
		if(publisher != null && publisher.handle(me)) {
			return;
		}
		final Channel channel = me.getChannel();
		final RtmpMessage message = (RtmpMessage) me.getMessage();
		switch(message.getHeader().getMessageType()) {
		case CHUNK_SIZE: // handled by decoder
			break;
		case CONTROL:
			Control control = (Control) message;
			logger.debug("control: {}", control);
			switch(control.getType()) {
			case PING_REQUEST:
				final int time = control.getTime();
				logger.debug("server ping: {}", time);
				Control pong = Control.pingResponse(time);
				logger.debug("sending ping response: {}", pong);
				channel.write(pong);
				break;
			case SWFV_REQUEST:
				if(swfvBytes == null) {
					logger.warn("swf verification not initialized!" 
							+ " not sending response, server likely to stop responding / disconnect");
				} else {
					Control swfv = Control.swfvResponse(swfvBytes);
					logger.info("sending swf verification response: {}", swfv);
					channel.write(swfv);
				}
				break;
			case STREAM_BEGIN:
				if(publisher != null && !publisher.isStarted()) {
					publisher.start(channel, options.getStart(),
							options.getLength(), new ChunkSize(4096));
					return;
				}
				if(streamId !=0) {
					channel.write(Control.setBuffer(streamId, options.getBuffer()));
				}
				break;
			default:
				logger.debug("ignoring control message: {}", control);
			}
			break;
		case METADATA_AMF0:
		case METADATA_AMF3:
			Metadata metadata = (Metadata) message;
			if(metadata.getName().equals("onMetaData")) {
				logger.debug("writing 'onMetaData': {}", metadata);
				writer.write(message);
			} else {
				logger.debug("ignoring metadata: {}", metadata);
			}
			break;
		case AUDIO:
		case VIDEO:
		case AGGREGATE:                
			writer.write(message);
			bytesRead += message.getHeader().getSize();
			if((bytesRead - bytesReadLastSent) > bytesReadWindow) {
				logger.debug("sending bytes read ack {}", bytesRead);
				bytesReadLastSent = bytesRead;
				channel.write(new BytesRead(bytesRead));
			}
			break;
		case COMMAND_AMF0:
		case COMMAND_AMF3:
			Command command = (Command) message;                
			String name = command.getName();
			logger.debug("server command: {}", name);
			if(name.equals("_result")) {
				String resultFor = transactionToCommandMap.get(command.getTransactionId());
				logger.info("result for method call: {}", resultFor);
				if(resultFor.equals("connect")) {
					// ここでCreateStreamをしないで別の命令を発行するとかすればよさそう。
//                   writeCommandExpectingResult(channel, Command.createStream());
//                      writeCommandExpectingResult(channel, CommandEx.testFunc());
					Object connectInfo = command.getArg(0);
					if(connectInfo instanceof Map<?, ?>) {
						rtmpClient.getListener().onConnect((Map)connectInfo);
					}
					else {
						rtmpClient.getListener().onConnect(null);
					}
//					me.getChannel().close();
				} else if(resultFor.equals("createStream")) {
					streamId = ((Double) command.getArg(0)).intValue();
					logger.debug("streamId to use: {}", streamId);
					if(options.getPublishType() != null) { // TODO append, record                            
						RtmpReader reader;
//						if(options.getFileToPublish() != null) {
							reader = RtmpPublisher.getReader(options.getFileToPublish());
//						} else {
//							reader = options.getReaderToPublish();
//						}
						if(options.getLoop() > 1) {
							reader = new LoopedReader(reader, options.getLoop());
						}
						publisher = new RtmpPublisher(reader, streamId, options.getBuffer(), false) {
							@Override protected RtmpMessage[] getStopMessages(long timePosition) {
								return new RtmpMessage[]{Command.unpublish(streamId)};
							}
						};                            
						channel.write(Command.publish(streamId, options));
						return;
					} else {
//						writer = options.getWriterToSave();
//						if(writer == null) {
							writer = new FlvWriter(options.getStart(), options.getSaveAs());
//						}
						channel.write(Command.play(streamId, options));
						channel.write(Control.setBuffer(streamId, 0));
					}
				} else {
					// この動作はonResultで動作させるべき(resultForが一致しないとなんともいえないけどね。)
					rtmpClient.getListener().onResult(resultFor, command);
					logger.warn("un-handled server result for: {}", resultFor);
				}
			} else if(name.equals("onStatus")) {
				final Map<String, Object> temp = (Map) command.getArg(0);
				final String code = (String) temp.get("code");
				logger.info("onStatus code: {}", code);
				if (code.equals("NetStream.Failed") // TODO cleanup
						|| code.equals("NetStream.Play.Failed")
						|| code.equals("NetStream.Play.Stop")
						|| code.equals("NetStream.Play.StreamNotFound")) {
					logger.info("disconnecting, code: {}, bytes read: {}", code, bytesRead);
					channel.close();
					return;
				}
				if(code.equals("NetStream.Publish.Start")
						&& publisher != null && !publisher.isStarted()) {
					publisher.start(channel, options.getStart(),
							options.getLength(), new ChunkSize(4096));
					return;
				}
				if (publisher != null && code.equals("NetStream.Unpublish.Success")) {
					logger.info("unpublish success, closing channel");
					ChannelFuture future = channel.write(Command.closeStream(streamId));
					future.addListener(ChannelFutureListener.CLOSE);
					return;
				}
			} else if(name.equals("close")) {
				logger.info("server called close, closing channel");
				channel.close();
				return;
			} else if(name.equals("_error")) {
				logger.error("closing channel, server resonded with error: {}", command);
				channel.close();
				return;
			} else {
				rtmpClient.setOnInvokeData(name, command);
				rtmpClient.getListener().onInvoke(name, command);
				logger.warn("ignoring server command: {}", command);
			}
			break;
		case BYTES_READ:
			logger.info("ack from server: {}", message);
			break;
		case WINDOW_ACK_SIZE:
			WindowAckSize was = (WindowAckSize) message;                
			if(was.getValue() != bytesReadWindow) {
				channel.write(SetPeerBw.dynamic(bytesReadWindow));
			}                
			break;
		case SET_PEER_BW:
			SetPeerBw spb = (SetPeerBw) message;                
			if(spb.getValue() != bytesWrittenWindow) {
				channel.write(new WindowAckSize(bytesWrittenWindow));
			}
			break;
		default:
			logger.info("ignoring rtmp message: {}", message);
		}
		if(publisher != null && publisher.isStarted()) { // TODO better state machine
//			publisher.fireNext(channel, 0);
			publisher.write(channel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		ChannelUtils.exceptionCaught(e);
	}
}
