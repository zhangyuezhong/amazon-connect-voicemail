package com.amazonaws.kvs.streaming.connect;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.kinesisvideo.parser.ebml.InputStreamParserByteSource;
import com.amazonaws.kinesisvideo.parser.mkv.StreamingMkvReader;
import com.amazonaws.kinesisvideo.parser.utilities.FragmentMetadataVisitor;
import com.amazonaws.regions.Regions;

public class KVSAmazonConnectStreaming {

	private static final String START_SELECTOR_TYPE = "FRAGMENT_NUMBER";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSSZ");

	private AWSCredentialsProvider awsCredentialsProvider;

	public KVSAmazonConnectStreaming() {

	}

	/**
	 * 
	 * @param region
	 * @param streamARN
	 * @param startFragmentNum
	 * @param contactId
	 * @param track
	 * @return
	 * @throws Exception
	 */
	public CompletableFuture<Path> getStartStreamingFuture(Regions region, String streamARN, String startFragmentNum,
			String contactId, KVSUtils.TrackName track) throws Exception {

		String streamName = streamARN.substring(streamARN.indexOf("/") + 1, streamARN.lastIndexOf("/"));

		KVSStreamTrackObject kvsStreamTrackObject = getKVSStreamTrackObject(region, streamName, startFragmentNum,
				track.getName(), contactId);

		return this.getStartStreamingFuture(kvsStreamTrackObject, contactId);

	}

	private CompletableFuture<Path> getStartStreamingFuture(KVSStreamTrackObject kvsStreamTrackObject,
			String contactId) {
		CompletableFuture<Path> completableFuture = new CompletableFuture<>();
		KVSAudioStreamPublisher publisher = this.getKVSAudioStreamPublisher(kvsStreamTrackObject, contactId);
		publisher.subscribe(new KVSByteBufferSubscriber(kvsStreamTrackObject, completableFuture));
		return completableFuture;
	}

	/**
	 * Create all objects necessary for KVS streaming from each track
	 *
	 * @param streamName
	 * @param startFragmentNum
	 * @param trackName
	 * @param contactId
	 * @return
	 * @throws FileNotFoundException
	 */
	private KVSStreamTrackObject getKVSStreamTrackObject(Regions region, String streamName, String startFragmentNum,
			String trackName, String contactId) throws FileNotFoundException {

		InputStream kvsInputStream = KVSUtils.getInputStreamFromKVS(streamName, region, startFragmentNum,
				getAwsCredentialsProvider(), START_SELECTOR_TYPE);
		StreamingMkvReader streamingMkvReader = StreamingMkvReader
				.createDefault(new InputStreamParserByteSource(kvsInputStream));

		System.out.println(kvsInputStream.getClass().getCanonicalName());
		FragmentMetadataVisitor.BasicMkvTagProcessor tagProcessor = new FragmentMetadataVisitor.BasicMkvTagProcessor();
		FragmentMetadataVisitor fragmentVisitor = FragmentMetadataVisitor.create(Optional.of(tagProcessor));

		String fileName = String.format("%s_%s_%s.raw", contactId, DATE_FORMAT.format(new Date()), trackName);
		Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir"));
		Path saveAudioFilePath = tmpdir.resolve(fileName);
		FileOutputStream fileOutputStream = new FileOutputStream(saveAudioFilePath.toString());

		return new KVSStreamTrackObject(kvsInputStream, streamingMkvReader, tagProcessor, fragmentVisitor,
				saveAudioFilePath, fileOutputStream, trackName);
	}

	public KVSAudioStreamPublisher getKVSAudioStreamPublisher(KVSStreamTrackObject kvsStreamTrackObject,
			String contactId) {
		return new KVSAudioStreamPublisher(kvsStreamTrackObject.getStreamingMkvReader(), contactId,
				kvsStreamTrackObject.getOutputStream(), kvsStreamTrackObject.getTagProcessor(),
				kvsStreamTrackObject.getFragmentVisitor(), kvsStreamTrackObject.getTrackName());
	}

	/**
	 * KVSAudioStreamPublisher implements audio stream publisher. It emits audio
	 * events from a KVS stream asynchronously in a separate thread
	 */
	private static class KVSAudioStreamPublisher implements Publisher<ByteBuffer> {
		private final StreamingMkvReader streamingMkvReader;
		private String contactId;
		private OutputStream outputStream;
		private FragmentMetadataVisitor.BasicMkvTagProcessor tagProcessor;
		private FragmentMetadataVisitor fragmentVisitor;
		private String track;

		private KVSAudioStreamPublisher(StreamingMkvReader streamingMkvReader, String contactId,
				OutputStream outputStream, FragmentMetadataVisitor.BasicMkvTagProcessor tagProcessor,
				FragmentMetadataVisitor fragmentVisitor, String track) {
			this.streamingMkvReader = streamingMkvReader;
			this.contactId = contactId;
			this.outputStream = outputStream;
			this.tagProcessor = tagProcessor;
			this.fragmentVisitor = fragmentVisitor;
			this.track = track;
		}

		@Override
		public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
			subscriber.onSubscribe(new KVSByteBufferSubscription(subscriber, streamingMkvReader, contactId,
					outputStream, tagProcessor, fragmentVisitor, track));
		}
	}

	/**
	 * Closes the FileOutputStream and uploads the Raw audio file to S3
	 *
	 * @param kvsStreamTrackObject
	 * @param saveCallRecording    should the call recording be uploaded to S3?
	 * @throws IOException
	 */
	public void close(KVSStreamTrackObject kvsStreamTrackObject) throws IOException {
		kvsStreamTrackObject.getInputStream().close();
		kvsStreamTrackObject.getOutputStream().close();
	}

	public AWSCredentialsProvider getAwsCredentialsProvider() {
		if (this.awsCredentialsProvider != null)
			return awsCredentialsProvider;
		else {
			return DefaultAWSCredentialsProviderChain.getInstance();
		}
	}

	public void setAwsCredentialsProvider(AWSCredentialsProvider awsCredentialsProvider) {
		this.awsCredentialsProvider = awsCredentialsProvider;
	}

}
