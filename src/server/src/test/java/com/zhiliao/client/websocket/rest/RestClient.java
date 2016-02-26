package com.zhiliao.client.websocket.rest;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;


import com.zhiliao.client.websocket.MessageApi;
import com.zhiliao.client.websocket.MessageApi.OnMessageHandler;
import com.zhiliao.client.websocket.MessageApi.OnOpenHandler;
import com.zhiliao.message.client.*;
import com.zhiliao.message.server.*;
import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.model.rest.Resource;

public class RestClient {
	private MessageApi api;
	private long mark = 0;
	Map<Long, Entry<Request, Callback>> callbacks = new ConcurrentHashMap<>();

	public RestClient(MessageApi api) {
		this.api = api;
		List<OnOpenHandler> onOpenHandlers = api.getOnOpenHandlers();
		onOpenHandlers.add(new OnOpenHandler() {
			@Override
			public void handle(MessageApi api) {
				for (Entry<Request, Callback> entry : callbacks.values()) {
					api.send(entry.getKey());
				}
			}
		});
		Map<Class<? extends ServerMessage>, OnMessageHandler> handlers = api.getOnMessageHandlers();
		handlers.put(ErrorResponse.class, new MessageApi.OnMessageHandler<ErrorResponse>() {
			@Override
			public void handle(MessageApi api, ErrorResponse message) {
				Entry<Request, Callback> callback = callbacks.get(message.getMark());
				if (callback == null) {
					System.err.println("wild response : " + message);
					return;
				}
				callback.getValue().onError(message.getError());
				callbacks.remove(message.getMark());
			}
		});
		handlers.put(GetResponse.class, new MessageApi.OnMessageHandler<GetResponse>() {
			@Override
			public void handle(MessageApi api, GetResponse message) {
				Entry<Request, Callback> callback = callbacks.get(message.getMark());
				if (callback == null) {
					System.err.println("wild response : " + message);
					return;
				}

				((Callback.Get<Resource>)callback.getValue()).onResponse(message.getResource());
				callbacks.remove(message.getMark());
			}
		});
		handlers.put(PostResponse.class, new MessageApi.OnMessageHandler<PostResponse>() {
			@Override
			public void handle(MessageApi api, PostResponse message) {
				Entry<Request, Callback> callback = callbacks.get(message.getMark());
				if (callback == null) {
					System.err.println("wild response : " + message);
					return;
				}

				((Callback.Post)callback.getValue()).onResponse(message.getId());
				callbacks.remove(message.getMark());
			}
		});
		handlers.put(ListResponse.class, new MessageApi.OnMessageHandler<ListResponse>() {
			@Override
			public void handle(MessageApi api, ListResponse message) {
				Entry<Request, Callback> callback = callbacks.get(message.getMark());
				if (callback == null) {
					System.err.println("wild response : " + message);
					return;
				}

				((Callback.List)callback.getValue()).onResponse(message.getList());
				callbacks.remove(message.getMark());
			}
		});
		handlers.put(DeleteResponse.class, new MessageApi.OnMessageHandler<DeleteResponse>() {
			@Override
			public void handle(MessageApi api, DeleteResponse message) {
				Entry<Request, Callback> callback = callbacks.get(message.getMark());
				if (callback == null) {
					System.err.println("wild response : " + message);
					return;
				}

				((Callback.Delete)callback.getValue()).onResponse();
				callbacks.remove(message.getMark());
			}
		});
		handlers.put(IteratorInitResponse.class, new MessageApi.OnMessageHandler<IteratorInitResponse>() {

			@Override
			public void handle(MessageApi api, IteratorInitResponse message) {
				Entry<Request, Callback> callback = callbacks.get(message.getMark());
				if (callback == null) {
					System.err.println("wild response : " + message);
					return;
				}

				((Callback.IteratorInit)callback.getValue()).onResponse();
				callbacks.remove(message.getMark());
			}
		});
		handlers.put(IteratorNextResponse.class, new MessageApi.OnMessageHandler<IteratorNextResponse>() {

			@Override
			public void handle(MessageApi api, IteratorNextResponse message) {
				Entry<Request, Callback> callback = callbacks.get(message.getMark());
				if (callback == null) {
					System.err.println("wild response : " + message);
					return;
				}

				((Callback.IteratorNext)callback.getValue()).onResponse(message.getList());
				callbacks.remove(message.getMark());
			}
		});

	}

	public static interface Callback {
		public void onError(ErrorCode error);
		public static interface Get<T extends Resource> extends Callback {
			public void onResponse(T res);
		}
		public static interface Post<T extends Resource> extends Callback {
			public void onResponse(long id);
		}
		public static interface List<T extends Resource> extends Callback {
			public void onResponse(java.util.List<T> list);
		}
		public static interface Delete<T extends Resource> extends Callback {
			public void onResponse();
		}
		public static interface IteratorInit<T extends IteratorInitRequest.IteratorSpecification> extends Callback {
			public void onResponse();
		}
		public static interface IteratorNext<T extends IteratorInitRequest.IteratorSpecification> extends Callback {
			public void onResponse(java.util.List<Resource> list);
		}
	}

	public <T extends Resource> void get(T res, Callback.Get<T> callback) {
		GetRequest request = new GetRequest(mark++, res);
		sendRequest(request, callback);
	}
	
	public <T extends Resource> Future<T> get(T res) {
		final RestFuture<T> fut = new RestFuture<T>();
		get(res, new Callback.Get<T>() {
			@Override
			public void onError(ErrorCode error) {
				fut.set(error);
			}

			@Override
			public void onResponse(T res) {
				fut.set(res);
			}
		});
		return fut;
	}

	
	public <T extends Resource> void post(T res, Callback.Post<T> callback) {
		PostRequest request = new PostRequest(mark++, res);
		sendRequest(request, callback);
	}
	
	public <T extends Resource> Future<Long> post(T res) {
		final RestFuture<Long> fut = new RestFuture<Long>();
		post(res, new Callback.Post<T>() {
			@Override
			public void onError(ErrorCode error) {
				fut.set(error);
			}

			@Override
			public void onResponse(long id) {
				fut.set(id);
			}
		});
		return fut;
	}

	public <T extends Resource> void list(T res, Callback.List<T> callback) {
		ListRequest request = new ListRequest(mark++, res);
		sendRequest(request, callback);
	}
	

	public <T extends Resource> Future<List<T>> list(T res) {
		final RestFuture<List<T>> fut = new RestFuture<List<T>>();
		list(res, new Callback.List<T>() {
			@Override
			public void onError(ErrorCode error) {
				fut.set(error);
			}

			@Override
			public void onResponse(java.util.List<T> list) {
				fut.set(list);
			}
		});
		return fut;
	}

	public <T extends Resource> void delete(T res, Callback.Delete<T> callback) {
		DeleteRequest request = new DeleteRequest(mark++, res);
		sendRequest(request, callback);
	}
	
	public <T extends Resource> Future<Void> delete(T res) {
		final RestFuture<Void> fut = new RestFuture<Void>();
		delete(res, new Callback.Delete<T>() {
			@Override
			public void onError(ErrorCode error) {
				fut.set(error);
			}

			@Override
			public void onResponse() {
				fut.set((Void) null);
			}

		});
		return fut;
	}

	public <T extends IteratorInitRequest.IteratorSpecification> void iteratorInit(long id,
																				   T spec,
																				   Callback.IteratorInit<T> callback) {
		IteratorInitRequest request = new IteratorInitRequest(mark++, id, spec);
		sendRequest(request, callback);
	}

	public <T extends IteratorInitRequest.IteratorSpecification> Future<Void> iteratorInit(long id, T spec) {
		final RestFuture<Void> fut = new RestFuture<>();
		iteratorInit(id, spec, new Callback.IteratorInit<T>() {
			@Override
			public void onResponse() {
				fut.set((Void) null);
			}

			@Override
			public void onError(ErrorCode error) {
				fut.set(error);
			}
		});
		return fut;
	}

	public <T extends IteratorInitRequest.IteratorSpecification> void
	iteratorNext(long id, long count, boolean skip, Callback.IteratorNext<T> callback) {
		IteratorNextRequest request = new IteratorNextRequest(mark++, id, count, skip);
		sendRequest(request, callback);
	}

	public <T extends IteratorInitRequest.IteratorSpecification> Future<List<Resource>>
	iteratorNext(long id, long count, boolean skip) {
		final RestFuture<List<Resource>> fut = new RestFuture<>();
		iteratorNext(id, count, skip, new Callback.IteratorNext<IteratorInitRequest.IteratorSpecification>() {
			@Override
			public void onResponse(java.util.List<Resource> list) {
				fut.set(list);
			}

			@Override
			public void onError(ErrorCode error) {
				fut.set(error);
			}
		});
		return fut;
	}

	private void sendRequest(Request request, Callback callback) {
		callbacks.put(request.getMark(), new AbstractMap.SimpleEntry(request, callback));
		api.send(request);
	}
}
