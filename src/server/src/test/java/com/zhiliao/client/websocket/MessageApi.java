package com.zhiliao.client.websocket;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.CloseReason;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.zhiliao.client.Config;
import com.zhiliao.message.client.ClientMessage;
import com.zhiliao.message.server.ServerMessage;

public class MessageApi extends Endpoint {
	public Map<Class<? extends ServerMessage>, OnMessageHandler> getOnMessageHandlers() {
		return onMessageHandlers;
	}

	public void setOnMessageHandlers(
			Map<Class<? extends ServerMessage>, OnMessageHandler> onMessageHandlers) {
		this.onMessageHandlers = onMessageHandlers;
	}

	public List<OnOpenHandler> getOnOpenHandlers() {
		return onOpenHandlers;
	}

	public void setOnOpenHandlers(List<OnOpenHandler> onOpenHandlers) {
		this.onOpenHandlers = onOpenHandlers;
	}

	public List<OnCloseHandler> getOnCloseHandlers() {
		return onCloseHandlers;
	}

	public void setOnCloseHandlers(List<OnCloseHandler> onCloseHandlers) {
		this.onCloseHandlers = onCloseHandlers;
	}

	public List<OnErrorHandler> getOnErrorHandlers() {
		return onErrorHandlers;
	}

	public void setOnErrorHandlers(List<OnErrorHandler> onErrorHandlers) {
		this.onErrorHandlers = onErrorHandlers;
	}

	static public interface OnMessageHandler<T extends ServerMessage> {
		void handle(MessageApi api, T message);
	}
	static public interface OnOpenHandler {
		void handle(MessageApi api);
	}
	static public interface OnCloseHandler {
		void handle(MessageApi api, CloseReason reason);
	}
	static public interface OnErrorHandler {
		void handle(MessageApi api, Throwable thr);
	}

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
		
	private Map<Class<? extends ServerMessage>, OnMessageHandler> onMessageHandlers = 
			new HashMap<Class<? extends ServerMessage>, MessageApi.OnMessageHandler>();
	private List<OnOpenHandler> onOpenHandlers = new ArrayList<MessageApi.OnOpenHandler>();
	private List<OnCloseHandler> onCloseHandlers = new ArrayList<MessageApi.OnCloseHandler>();
	private List<OnErrorHandler> onErrorHandlers = new ArrayList<MessageApi.OnErrorHandler>();
	
	private Session session;
	
	public MessageApi() {
	}
	
	public void connect(final Configurator configurator) throws IOException {
		ClientManager client = ClientManager.createClient();
		ClientEndpointConfig config = new ClientEndpointConfig() {
			
			@Override
			public Map<String, Object> getUserProperties() {
				return new HashMap<String, Object>();
			}
			
			@Override
			public List<Class<? extends Encoder>> getEncoders() {
				List<Class<? extends Encoder>> list = new ArrayList<Class<? extends Encoder>>();
				list.add(MessageEncoder.class);
				return list;
			}
			
			@Override
			public List<Class<? extends Decoder>> getDecoders() {
				List<Class<? extends Decoder>> list = new ArrayList<Class<? extends Decoder>>();
				list.add(MessageDecoder.class);
				return list;
			}
			
			@Override
			public List<String> getPreferredSubprotocols() {
				return new ArrayList<String>();
			}
			
			@Override
			public List<Extension> getExtensions() {
				return new ArrayList<Extension>();
			}
			
			@Override
			public Configurator getConfigurator() {
				if (configurator == null)
					return new Configurator();
				else
					return configurator;
			}
		};
		URI path;
		try {
			path = new URI(Config.websocketURI);
			client.connectToServer(this, config, path);
		} catch (URISyntaxException | DeploymentException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		lock.writeLock().lock();
		
		this.session = session;
		final MessageApi api = this;
		session.addMessageHandler(new MessageHandler.Whole<ServerMessage>() {
			@Override
			public void onMessage(ServerMessage message) {
				for (Entry<Class<? extends ServerMessage>, OnMessageHandler> entry : onMessageHandlers.entrySet()) {
					Class<? extends ServerMessage> clazz = entry.getKey();
					if (clazz.isInstance(message)) {
						entry.getValue().handle(api, clazz.cast(message));
					}
				}
			}
		});

		for (OnOpenHandler handler : onOpenHandlers)
			handler.handle(api);
		lock.writeLock().unlock();
	}
	
	@Override
	public void onClose(Session session, CloseReason reason) {
		lock.writeLock().lock();
		this.session = null;
		for (OnCloseHandler handler : onCloseHandlers)
			handler.handle(this, reason);
		lock.writeLock().unlock();
	}
	
	@Override
	public void onError(Session session, Throwable thr) {
		for (OnErrorHandler handler : onErrorHandlers)
			handler.handle(this, thr);
	}
	
	public void send(ClientMessage message) {
		lock.readLock().lock();
		if (session == null) {
			lock.readLock().unlock();
			throw new IllegalStateException();
		} else
			session.getAsyncRemote().sendObject(message);
		lock.readLock().unlock();
	}
}
