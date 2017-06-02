package io.pivotal.smartarchive.regexparser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;

@EnableBinding(Processor.class)
@EnableConfigurationProperties(RegexParserProperties.class)
public class RegexParserConfiguration {

	private static final Logger log = LoggerFactory.getLogger(RegexParserConfiguration.class);

	@Autowired
	private RegexParserProperties properties;

	/* Begin go-router Section */
	public Integer ROUTER_OUTER_FIELDS = 7;
	public Integer ROUTER_INNER_FIELDS = 21;

	/*
	 * TODO - convert all these to named capture groups or maintain a lookup
	 * table to map the group index to a corresponding field name
	 */
	private String hostRegex = "([^ ]+)[ -]+";
	private String timestampRegex = ".([0-9\\:\\-T\\+\\.]+).";
	private String requestRegex = " \"([^\"]+)\" ";
	private String statusBytesRecvBytesSentRegex = "\"?([0-9\\-]+)\"? ([0-9\\-]+) ([0-9\\-]+) ";
	private String referrerAndUserAgentRegex = "\"([^\"]+)\" \"([^\"]+)\" ";
	private String sourceAddressAndPortRegex = "\"([0-9.]+):(\\d+)\" ";
	private String destinationAddressAndPortRegex = "\"([0-9.]+):(\\d+)\" ";

	private String genericKeyValueRegex = "[a-z_0-9]+:\"([^\"]+)\"[ ]?";
	private String xforwardedforRegex = genericKeyValueRegex;
	private String xforwardprotocolRegex = genericKeyValueRegex;
	private String vcapreqRegex = genericKeyValueRegex;
	private String resptimeRegex = "[a-z_]+:([0-9.]+) ";
	private String app_idRegex = genericKeyValueRegex;
	private String appindexRegex = genericKeyValueRegex;

	private String x_b3_traceidRegex = genericKeyValueRegex;
	private String x_b3_spanidRegex = genericKeyValueRegex;
	private String x_b3_parentspanidRegex = genericKeyValueRegex;

	public String routerRegex = hostRegex + timestampRegex + requestRegex + statusBytesRecvBytesSentRegex
			+ referrerAndUserAgentRegex + sourceAddressAndPortRegex + destinationAddressAndPortRegex
			+ xforwardedforRegex + xforwardprotocolRegex + vcapreqRegex + resptimeRegex + app_idRegex + appindexRegex
			+ x_b3_traceidRegex + x_b3_spanidRegex + x_b3_parentspanidRegex;
	/* End go-router Section */

	/* Begin CAPI Section */
	public Integer CAPI_OUTER_FIELDS = 5;
	public Integer CAPI_INNER_FIELDS = 10;

	private String payloadBase = "([0-9A-z- ]+(?:[: A-z\"]+)?)[: {(]*";
	private String payloadFirstToken = "(?:\"?[A-z_:]+\"?\\=\\>\"?([A-z0-9- ]*)\"?[,)}',]*)? ?";
	private String payloadRemainingTokens = "(?:\"?[A-z_:]+\"?\\=\\>((?:\\d+)|\"?(?:[^\"]+)\"?)[,)}']*)? ?";

	public String capiRegex = payloadBase + payloadFirstToken + payloadRemainingTokens + payloadRemainingTokens
			+ payloadRemainingTokens + payloadRemainingTokens + payloadRemainingTokens + payloadRemainingTokens
			+ payloadRemainingTokens + payloadRemainingTokens;

	/* End CAPI Section */

	StringWriter stringWriter = new StringWriter();
	CSVPrinter printer;

	@Autowired
	public RegexParserConfiguration() throws IOException {
		this.printer = new CSVPrinter(stringWriter, CSVFormat.RFC4180.withDelimiter(','));
	}

	public Map<String, String> stringToMapRegex(String testCase, String testRegex, Integer expectedNumFields)
			throws JSONException {
		JSONObject jObject = new JSONObject(testCase);

		return jsonToMapRegex(jObject, testRegex, expectedNumFields);
	}

	public Map<String, String> jsonToMapRegex(JSONObject jObject, String testRegex, Integer expectedNumFields)
			throws JSONException {

		Map<String, String> payloadAsMap = new HashMap<String, String>();

		Iterator<?> keys = jObject.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = jObject.getString(key);
			payloadAsMap.put(key, value);

		}

		Pattern pattern = Pattern.compile(testRegex);

		Matcher matcher = pattern.matcher(payloadAsMap.get("message").toString());

		log.debug((String) payloadAsMap.get("message"));

		if (!matcher.find()) {
			log.debug("Pattern didn't match payload!");
			return null;
		}

		Map<String, String> parsedRecord = new HashMap<String, String>();

		// First populate the top level fields from JSON object
		for (String k : payloadAsMap.keySet()) {
			if (!k.equals("message")) {
				log.info("key: " + k + " value:  ", payloadAsMap.get(k));
				parsedRecord.put(k, payloadAsMap.get(k));
			}
		}

		Integer numFields = matcher.groupCount();

		if (numFields == expectedNumFields) {
			/*
			 * If there is a complete pattern match, add each field one at a
			 * time
			 */
			for (Integer f = 1; f <= numFields; ++f) {
				log.debug(matcher.group(f));
				System.out.println(matcher.group(f));
				parsedRecord.put("_" + f.toString(), matcher.group(f));
			}

		}

		return parsedRecord;
	}

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object parseMessage(Object payload) throws JSONException, IOException {
		log.info("Raw payload follows");
		log.info(payload.toString());

		// JSONObject payloadAsJson = (JSONObject)payload;
		// String delimitedRecord = "";

		Map<String, String> payloadAsMap = null;
		// if (payloadAsJson.get("sourceType") == "RTR") {
		payloadAsMap = stringToMapRegex(payload.toString(), routerRegex, ROUTER_INNER_FIELDS);
		// }
		// else if (payloadAsJson.get("sourceType") == "API") {
		// payloadAsMap = stringToMapRegex(payload, capiRegex,
		// CAPI_INNER_FIELDS);
		// }

		// log.info(payloadAsMap.toString());
		// do stuff

		payloadAsMap.put("id", UUID.randomUUID().toString());

		// printer.printRecord(payloadAsMap.values());

		// String delimitedRecord = stringWriter.toString();

		return payloadAsMap;
	}
}
