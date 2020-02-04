package com.amazonaws.kvs.streaming.connect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.Validate;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KVSByteBufferSubscriber implements Subscriber<ByteBuffer> {

	private static final Logger logger = LoggerFactory.getLogger(KVSByteBufferSubscriber.class);

	private KVSStreamTrackObject kvsStreamTrackObject;
	private CompletableFuture<Path> future;
	private Subscription subscription;

	public KVSByteBufferSubscriber(KVSStreamTrackObject kvsStreamTrackObject, CompletableFuture<Path> future) {
		this.kvsStreamTrackObject = Validate.notNull(kvsStreamTrackObject);
		this.future = Validate.notNull(future);
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = Validate.notNull(subscription);
		subscription.request(Long.MAX_VALUE);

	}

	@Override
	public void onNext(ByteBuffer audioBuffer) {
		try {
			if (this.future.isCancelled()) {
				this.cancel();
				this.close();
			} else {
				byte[] audioBytes = new byte[audioBuffer.remaining()];
				audioBuffer.get(audioBytes);
				kvsStreamTrackObject.getOutputStream().write(audioBytes);
			}
		} catch (IOException ex) {
			logger.error("Error ", ex);
		}

	}

	@Override
	public void onError(Throwable t) {
		this.future.completeExceptionally(t);
		this.cancel();
		this.close();
	}

	@Override
	public void onComplete() {
		this.future.complete(kvsStreamTrackObject.getSaveAudioFilePath());
		this.cancel();
		this.close();
	}

	public void cancel() {
		try {
			this.subscription.cancel();
		} catch (Exception ex) {
			logger.error("Error ", ex);
		}
	}

	public void close() {
		try {
			kvsStreamTrackObject.getInputStream().close();
			kvsStreamTrackObject.getOutputStream().close();
		} catch (Exception ex) {
			logger.error("Error ", ex);
		}
	}

}
