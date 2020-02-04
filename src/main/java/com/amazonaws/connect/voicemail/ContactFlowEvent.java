package com.amazonaws.connect.voicemail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class ContactFlowEvent {
	private DocumentContext doc;

	public ContactFlowEvent(String json) {
		this.doc = this.parse(json);
	}

	public ContactFlowEvent(InputStream input) throws IOException {
		this.doc = this.parse(input);
	}

	public String getContactId() {
		return this.read(doc, "$.Details.ContactData.ContactId", String.class).orElse(null);
	}

	public String getStartFragmentNumber() {
		return this.read(doc, "$.Details.ContactData.MediaStreams.Customer.Audio.StartFragmentNumber", String.class)
				.orElse(null);
	}

	public String getStreamARN() {
		return this.read(doc, "$.Details.ContactData.MediaStreams.Customer.Audio.StreamARN", String.class).orElse(null);
	}

	public String getCustomerEndpointAddress() {
		return this.read(doc, "$.Details.ContactData.CustomerEndpoint.Address", String.class).orElse(null);
	}

	public String getParameter(String key) {
		return this.read(doc, "$.Details.Parameters." + key, String.class).orElse(null);
	}

	private <T> Optional<T> read(DocumentContext doc, String path, Class<T> type) {
		T value = type.cast(doc.read(path, new com.jayway.jsonpath.Predicate[0]));
		return Optional.ofNullable(value);
	}

	private DocumentContext parse(String json) {
		return JsonPath
				.using(Configuration.defaultConfiguration().setOptions(new Option[] { Option.SUPPRESS_EXCEPTIONS }))
				.parse(json);
	}

	private DocumentContext parse(InputStream input) throws IOException {
		String request = IOUtils.toString(input, StandardCharsets.UTF_8);
		return JsonPath
				.using(Configuration.defaultConfiguration().setOptions(new Option[] { Option.SUPPRESS_EXCEPTIONS }))
				.parse(request);
	}

}
