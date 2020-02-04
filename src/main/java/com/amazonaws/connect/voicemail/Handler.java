package com.amazonaws.connect.voicemail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.amazonaws.kvs.streaming.connect.AudioUtils;
import com.amazonaws.kvs.streaming.connect.KVSAmazonConnectStreaming;
import com.amazonaws.kvs.streaming.connect.KVSUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import net.minidev.json.JSONObject;

public class Handler implements RequestStreamHandler {

	/**
	 * require parameters regionName sender recipient
	 * 
	 */
	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

		ContactFlowEvent event = new ContactFlowEvent(input);
		JSONObject responseBody = new JSONObject();
		try {
			this.sendMail(event, context);
			context.getLogger().log("email sent");
			responseBody.put("status", "sent");
		} catch (Exception ex) {
			context.getLogger().log(ex.getMessage());
			responseBody.put("status", "failed");
		}
		OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
		writer.write(responseBody.toString());
		writer.close();
	}

	public void sendMail(ContactFlowEvent event, Context context) throws Exception {
		try {
			AmazonSES ses = new AmazonSES();
			ses.setSender(System.getenv("EMAIL_SENDER"));
			ses.setRecipient(event.getParameter("recipient"));
			ses.setBodyHTML(
					"<html><head></head><body><h1>Hello!</h1><p>Please see the attached file</p></body></html>");
			ses.setBodyText("");
			ses.setSubject("Voicemail from " + event.getCustomerEndpointAddress());
			ses.setRegion(Regions.fromName(System.getenv("AWS_REGION")));
			ses.setAttachment(this.getWavFilePath(event, context));
			context.getLogger().log(ses.toString());
			ses.send();
		} catch (Exception ex) {
			throw ex;
		}
	}

	public String getWavFilePath(ContactFlowEvent event, Context context) throws Exception {
		try {
			Regions region = Regions.fromName(System.getenv("AWS_REGION"));
			String contactId = event.getContactId();
			String streamARN = event.getStreamARN();
			String startFragmentNumber = event.getStartFragmentNumber();

			context.getLogger().log("contactId=" + contactId);
			context.getLogger().log("startFragmentNumber=" + startFragmentNumber);
			context.getLogger().log("streamARN=" + streamARN);

			KVSAmazonConnectStreaming kacs = new KVSAmazonConnectStreaming();
			CompletableFuture<Path> completableFuture = kacs.getStartStreamingFuture(region, streamARN,
					startFragmentNumber, contactId, KVSUtils.TrackName.AUDIO_FROM_CUSTOMER);
			Path rawFile = completableFuture.get();
			File wavFile = AudioUtils.convertRawToWav(rawFile.toString());
			return wavFile.getAbsolutePath();

		} catch (Exception ex) {
			throw ex;
		}
	}

}
