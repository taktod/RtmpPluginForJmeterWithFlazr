package com.ttProject.jmeter.rtmp.library;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;
import com.ttProject.jmeter.rtmp.library.client.ClientHandlerEx;
import com.ttProject.jmeter.rtmp.library.client.ClientPipelineFactoryEx;
import com.ttProject.jmeter.rtmp.library.message.CommandEx;
import com.ttProject.junit.annotation.Init;
import com.ttProject.junit.annotation.Junit;
import com.ttProject.junit.annotation.Test;

@SuppressWarnings("unused")
public class RtmpClientEx {
	private String server;
	private int port;
	private String application;
	private IRtmpClientEx listener; // ここでlistenerをいれておくべき、そして、すべてのClientのクラスからRtmpClientExのlistenerを取得して動作させることができるようにしておくべき。
	private Map<String, Command> invokeHistory = new ConcurrentHashMap<String, Command>();
	private ChannelFuture future = null;
	private WeakReference<ClientHandlerEx> clientHandlerEx = null;

	public RtmpClientEx() {
	}

	public RtmpClientEx(String server, int port, String application, IRtmpClientEx listener) {
		this();
		this.server = server;
		this.port = port;
		this.application = application;
		this.listener = listener;
	}
	
	@Init({"true"})
	private RtmpClientEx(boolean test) {
		this.server = "49.212.39.171";
		this.port = 1935;
		this.application = "avatarChat";
		this.listener = new IRtmpClientEx() {
			@Override
			public void onResult(String funcName, Command command) {
				System.out.println(funcName);
				System.out.println(command.getArg(0));
			}
			@Override
			public void onInvoke(String funcName, Command command) {
				System.out.println(funcName);
				System.out.println(command.getArg(0));
			}
			
			@Override
			public void onDisconnect() {
				System.out.println("停止完了");
			}
			
			@Override
			public void onConnect(Map<String, Object> obj) {
				System.out.println(obj);
				if(obj.get("code").equals("NetConnection.Connect.Success")) {
					// 接続成功！！！
					System.out.println("接続成功");
					// 適当になんかinvokeする。
					invoke("testFunc", new Object[]{"hogehoge", "sone"});
				}
				else {
					System.out.println("接続失敗");
				}
//				disconnect();
			}
		};
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
	public int getPort() {
		return port;
	}
	/**
	 * @return the application
	 */
	public String getApplication() {
		return application;
	}
	/**
	 * @return the listener
	 */
	public IRtmpClientEx getListener() {
		return listener;
	}
	/**
	 * @param listener the listener to set
	 */
	public void setListener(IRtmpClientEx listener) {
		this.listener = listener;
	}
	/**
	 * @param clientHandler
	 */
	public void setClientHandlerEx(ClientHandlerEx clientHandler) {
		this.clientHandlerEx = new WeakReference<ClientHandlerEx>(clientHandler);
	}
	
//	@Junit({
//		@Test(value={"null", "", ""}, assume="@dumppause"),
//	})
	public boolean connect(Object[] params, String swfUrl, String pageUrl) {
		// 動作がかえってこなくなるので、Threadを作成する必要がある。(そのスレッドで一連の処理を実施し、DisconnectしおわったらThreadを停止させる？)
		// とりあえず面倒なので、普通にやってみるか・・・
		ClientOptions options = new ClientOptions(server, port, application, "a", "a", false, null);
		Executor executor = Executors.newCachedThreadPool();
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(executor, executor));
		bootstrap.setPipelineFactory(new ClientPipelineFactoryEx(options, this));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		future = bootstrap.connect(new InetSocketAddress(server, port));
		future.awaitUninterruptibly();
		return future.isSuccess();
	}

	public void disconnect() {
		if(future == null) {
			return;
		}
		// 止める。
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				listener.onDisconnect();
			}
		});
		future.getChannel().close();
	}
	
	public void invoke(String funcName, Object[] params) {
		if(clientHandlerEx == null) {
			System.out.println("ClientHandlerのWeakReferenceがなくなっていました。");
			return; // エラーにすべき？
		}
		ClientHandlerEx handler = clientHandlerEx.get();
		if(handler == null) {
			System.out.println("ClientHandlerが改修されていました。");
			return; // エラーにすべき。例外？
		}
		handler.channelInvoke(future.getChannel(), CommandEx.invoke(funcName, params));
	}
	/**
	 * get the passed invoke event.
	 * @param methodName
	 * @return
	 */
	public Command getOnInvokeData(String methodName) {
		return invokeHistory.remove(methodName);
	}
	public void setOnInvokeData(String methodName, Command command) {
		invokeHistory.put(methodName, command);
	}
}
