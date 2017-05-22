package io.pivotal.smartarchive.regexparser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RegexParserApplicationTests {

	@Test
	public void contextLoads() {
	}

	private Integer ROUTER_OUTER_FIELDS = 8;

	/* Begin go-router Section */
	private Integer ROUTER_INNER_FIELDS = 21;

	/* TODO - convert all these to named capture groups */
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

	private String routerRegex = hostRegex + timestampRegex + requestRegex + statusBytesRecvBytesSentRegex
			+ referrerAndUserAgentRegex + sourceAddressAndPortRegex + destinationAddressAndPortRegex
			+ xforwardedforRegex + xforwardprotocolRegex + vcapreqRegex + resptimeRegex + app_idRegex + appindexRegex
			+ x_b3_traceidRegex + x_b3_spanidRegex + x_b3_parentspanidRegex;
	/* End go-router Section */

	/* Begin CAPI Section */
	private Integer CAPI_OUTER_FIELDS = 6; /*
											 * date, messsage, messageType,
											 * sourceType, sourceInstance, appId
											 */
	private Integer CAPI_INNER_FIELDS = 10;

	private String payloadBase = "([0-9A-z- ]+(?:[: A-z\"]+)?)[: {(]*";
	private String payloadFirstToken = "(?:\"?[A-z_:]+\"?\\=\\>\"?([A-z0-9- ]*)\"?[,)}',]*)? ?";
	private String payloadRemainingTokens = "(?:\"?[A-z_:]+\"?\\=\\>((?:\\d+)|\"?(?:[^\"]+)\"?)[,)}']*)? ?";

	private String capiPayloadPattern = payloadBase + payloadFirstToken + payloadRemainingTokens
			+ payloadRemainingTokens + payloadRemainingTokens + payloadRemainingTokens + payloadRemainingTokens
			+ payloadRemainingTokens + payloadRemainingTokens + payloadRemainingTokens;

	/* End CAPI Section */

	public Map<String, String> testParsing(String testCase, String testRegex, Integer expectedNumFields)
			throws JSONException {

		JSONObject jObject = new JSONObject(testCase);

		Map<String, String> payloadAsMap = new HashMap<String, String>();

		Iterator<?> keys = jObject.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = jObject.getString(key);
			payloadAsMap.put(key, value);

		}

		Pattern pattern = Pattern.compile(testRegex);

		Matcher matcher = pattern.matcher((String) payloadAsMap.get("message"));

		//System.out.println((String) payloadAsMap.get("message"));

		if (!matcher.find()) {
			//System.out.println("Not found");
			return null;
		}

		Map<String, String> parsedRecord = new HashMap<String, String>();

		// First populate the top level fields from JSON object
		for (String k : payloadAsMap.keySet()) {
			if (k != "message") {
				//System.out.println(payloadAsMap.get(k));
				parsedRecord.put(k, payloadAsMap.get(k));
			}
		}

		Integer numFields = matcher.groupCount();

		if (numFields == expectedNumFields) {
			/* If we have a complete pattern match, add fields individually */
			for (Integer f = 1; f <= numFields; ++f) {
				// System.out.println(matcher.group(f));
				System.out.println(matcher.group(f));
				parsedRecord.put(f.toString(), matcher.group(f));
			}

		}

		return parsedRecord;
	}

	@Test
	public void testCapiOne() throws JSONException {

		String apiTestOne = "{'date': 1493533391270, 'message': 'App instance exited with guid a395fdcb-4570-4914-895f-a43231ec924b payload: {\"instance\"=>\"0e00ccfd-7954-43df-5848-f77092a7c114\", \"index\"=>0, \"reason\"=>\"CRASHED\", \"exit_description\"=>\"2 error(s) occurred:\\n\\n* 1 error(s) occurred:\\n\\n* Exited with status 4\\n* 2 error(s) occurred:\\n\\n* cancelled\\n* process did not exit\", \"crash_count\"=>5, \"crash_timestamp\"=>1493533391162467512, \"version\"=>\"1e67e4ce-6b52-4daf-96b0-ed0b7132cc5b\"}', 'messageType': 'OUT', 'sourceType': 'API', 'sourceInstance': '6', 'appId': 'a395fdcb-4570-4914-895f-a43231ec924b'}";

		Map<String, String> parsedRecord = testParsing(apiTestOne, capiPayloadPattern, CAPI_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(CAPI_OUTER_FIELDS + CAPI_INNER_FIELDS, parsedRecord.size());

		// CAPI log timestamp
		Assert.assertEquals("1493533391270", parsedRecord.get("date"));

		// message text
		Assert.assertEquals("App instance exited with guid a395fdcb-4570-4914-895f-a43231ec924b payload: ",
				parsedRecord.get("1"));

		// message payload field 1
		Assert.assertEquals("0e00ccfd-7954-43df-5848-f77092a7c114", parsedRecord.get("2"));

		// message payload field 2
		Assert.assertEquals("0", parsedRecord.get("3"));

		// message payload field 3
		Assert.assertEquals("\"CRASHED\"", parsedRecord.get("4"));

		// message payload field 4
		Assert.assertEquals(
				"\"2 error(s) occurred:\n\n* 1 error(s) occurred:\n\n* Exited with status 4\n* 2 error(s) occurred:\n\n* cancelled\n* process did not exit\"",
				parsedRecord.get("5"));

		// message payload field 5
		Assert.assertEquals("5", parsedRecord.get("6"));

		// message payload field 6
		Assert.assertEquals("1493533391162467512", parsedRecord.get("7"));

		// message payload field 7
		Assert.assertEquals("\"1e67e4ce-6b52-4daf-96b0-ed0b7132cc5b\"", parsedRecord.get("8"));

		// message payload field 8
		Assert.assertEquals(null, parsedRecord.get("9"));

		// message payload field 9
		Assert.assertEquals(null, parsedRecord.get("10"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("API", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("6", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("a395fdcb-4570-4914-895f-a43231ec924b", parsedRecord.get("appId"));
	}

	@Test
	public void testCapiTwo() throws JSONException {

		String apiTestTwo = "{'date': 1494103282341, 'message': 'Process has crashed with type: \"web\"', 'messageType': 'OUT', 'sourceType': 'API', 'sourceInstance': '6', 'appId': '8568c5f4-358c-4a56-a793-8aba627d3bab'}";

		Map<String, String> parsedRecord = testParsing(apiTestTwo, capiPayloadPattern, CAPI_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(CAPI_OUTER_FIELDS + CAPI_INNER_FIELDS, parsedRecord.size());

		// CAPI log timestamp
		Assert.assertEquals("1494103282341", parsedRecord.get("date"));

		// message text
		Assert.assertEquals("Process has crashed with type: \"web\"", parsedRecord.get("1"));

		// message payload field 1
		Assert.assertEquals(null, parsedRecord.get("2"));

		// message payload field 2
		Assert.assertEquals(null, parsedRecord.get("3"));

		// message payload field 3
		Assert.assertEquals(null, parsedRecord.get("4"));

		// message payload field 4
		Assert.assertEquals(null, parsedRecord.get("5"));

		// message payload field 5
		Assert.assertEquals(null, parsedRecord.get("6"));

		// message payload field 6
		Assert.assertEquals(null, parsedRecord.get("7"));

		// message payload field 7
		Assert.assertEquals(null, parsedRecord.get("8"));

		// message payload field 8
		Assert.assertEquals(null, parsedRecord.get("9"));

		// message payload field 9
		Assert.assertEquals(null, parsedRecord.get("10"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("API", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("6", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("8568c5f4-358c-4a56-a793-8aba627d3bab", parsedRecord.get("appId"));
	}

	@Test
	public void testCapiThree() throws JSONException {

		String apiTestThree = "{'date': 1493989150689, 'message': 'Updated app with guid f2981826-e25e-464d-85cd-7c2a3de4e249 ({\"route\"=>\"51e1b791-eb32-42b8-8241-d4947c49257b\", :verb=>\"add\", :relation=>\"routes\", :related_guid=>\"51e1b791-eb32-42b8-8241-d4947c49257b\"})', 'messageType': 'OUT', 'sourceType': 'API', 'sourceInstance': '6', 'appId': 'f2981826-e25e-464d-85cd-7c2a3de4e249'}";

		System.out.println(capiPayloadPattern);

		Map<String, String> parsedRecord = testParsing(apiTestThree, capiPayloadPattern, CAPI_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(CAPI_OUTER_FIELDS + CAPI_INNER_FIELDS, parsedRecord.size());

		// CAPI log timestamp
		Assert.assertEquals("1493989150689", parsedRecord.get("date"));

		// message text
		Assert.assertEquals("Updated app with guid f2981826-e25e-464d-85cd-7c2a3de4e249 ", parsedRecord.get("1"));

		// message payload field 1
		Assert.assertEquals("51e1b791-eb32-42b8-8241-d4947c49257b", parsedRecord.get("2"));

		// message payload field 2
		Assert.assertEquals("\"add\"", parsedRecord.get("3"));

		// message payload field 3
		Assert.assertEquals("\"routes\"", parsedRecord.get("4"));

		// message payload field 4
		Assert.assertEquals("\"51e1b791-eb32-42b8-8241-d4947c49257b\"", parsedRecord.get("5"));

		// message payload field 5
		Assert.assertEquals(null, parsedRecord.get("6"));

		// message payload field 6
		Assert.assertEquals(null, parsedRecord.get("7"));

		// message payload field 7
		Assert.assertEquals(null, parsedRecord.get("8"));

		// message payload field 8
		Assert.assertEquals(null, parsedRecord.get("9"));

		// message payload field 9
		Assert.assertEquals(null, parsedRecord.get("10"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("API", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("6", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("f2981826-e25e-464d-85cd-7c2a3de4e249", parsedRecord.get("appId"));
	}

	@Test
	public void testCapiFour() throws JSONException {

		String apiTestFour = "{'date': 1493950739610, 'message': 'Updated app with guid bdeab700-7d73-4e3b-bae9-cdc028b3ba5e ({\"memory\"=>128, \"disk_quota\"=>256})', 'messageType': 'OUT', 'sourceType': 'API', 'sourceInstance': '3', 'appId': 'bdeab700-7d73-4e3b-bae9-cdc028b3ba5e'}";

		Map<String, String> parsedRecord = testParsing(apiTestFour, capiPayloadPattern, CAPI_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(CAPI_OUTER_FIELDS + CAPI_INNER_FIELDS, parsedRecord.size());

		// CAPI log timestamp
		Assert.assertEquals("1493950739610", parsedRecord.get("date"));

		// message text
		Assert.assertEquals("Updated app with guid bdeab700-7d73-4e3b-bae9-cdc028b3ba5e ", parsedRecord.get("1"));

		// message payload field 1
		Assert.assertEquals("128", parsedRecord.get("2"));

		// message payload field 2
		Assert.assertEquals("256", parsedRecord.get("3"));

		// message payload field 3
		Assert.assertEquals(null, parsedRecord.get("4"));

		// message payload field 4
		Assert.assertEquals(null, parsedRecord.get("5"));

		// message payload field 5
		Assert.assertEquals(null, parsedRecord.get("6"));

		// message payload field 6
		Assert.assertEquals(null, parsedRecord.get("7"));

		// message payload field 7
		Assert.assertEquals(null, parsedRecord.get("8"));

		// message payload field 8
		Assert.assertEquals(null, parsedRecord.get("9"));

		// message payload field 9
		Assert.assertEquals(null, parsedRecord.get("10"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("API", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("3", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("bdeab700-7d73-4e3b-bae9-cdc028b3ba5e", parsedRecord.get("appId"));
	}

	@Test
	public void testCapiFive() throws JSONException {

		String apiTestFive = "{'date': 1493305078965, 'message': 'App instance exited with guid 7d93d7b9-affa-498c-97ed-616da0795c7d payload: {\"instance\"=>\"\", \"index\"=>0, \"reason\"=>\"CRASHED\", \"exit_description\"=>\"2 error(s) occurred:\\n\\n* 2 error(s) occurred:\\n\\n* Exited with status 1\\n* cancelled\\n* cancelled\", \"crash_count\"=>68, \"crash_timestamp\"=>1493305078908738396, \"version\"=>\"8db7077a-a253-4078-9313-3d59c9db81b2\"}', 'messageType': 'OUT', 'sourceType': 'API', 'sourceInstance': '4', 'appId': '7d93d7b9-affa-498c-97ed-616da0795c7d'}";

		Map<String, String> parsedRecord = testParsing(apiTestFive, capiPayloadPattern, CAPI_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(CAPI_OUTER_FIELDS + CAPI_INNER_FIELDS, parsedRecord.size());

		// CAPI log timestamp
		Assert.assertEquals("1493305078965", parsedRecord.get("date"));

		// message text
		Assert.assertEquals("App instance exited with guid 7d93d7b9-affa-498c-97ed-616da0795c7d payload: ",
				parsedRecord.get("1"));

		// message payload field 1
		Assert.assertEquals("", parsedRecord.get("2"));

		// message payload field 2
		Assert.assertEquals("0", parsedRecord.get("3"));

		// message payload field 3
		Assert.assertEquals("\"CRASHED\"", parsedRecord.get("4"));

		// message payload field 4
		Assert.assertEquals(
				"\"2 error(s) occurred:\n\n* 2 error(s) occurred:\n\n* Exited with status 1\n* cancelled\n* cancelled\"",
				parsedRecord.get("5"));

		// message payload field 5
		Assert.assertEquals("68", parsedRecord.get("6"));

		// message payload field 6
		Assert.assertEquals("1493305078908738396", parsedRecord.get("7"));

		// message payload field 7
		Assert.assertEquals("\"8db7077a-a253-4078-9313-3d59c9db81b2\"", parsedRecord.get("8"));

		// message payload field 8
		Assert.assertEquals(null, parsedRecord.get("9"));

		// message payload field 9
		Assert.assertEquals(null, parsedRecord.get("10"));

		// message payload field 10
		Assert.assertEquals(null, parsedRecord.get("11"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("API", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("4", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("7d93d7b9-affa-498c-97ed-616da0795c7d", parsedRecord.get("appId"));
	}

	// @Test
	public void testCapiSix() throws JSONException {

		String apiTestSix = "{'date': 1494270451956, 'message': 'Updated app with guid 5fb7bdb2-5478-4670-935d-7eda86cdb2b6 ({\"name\"=>\"api-staging-agora\", \"command\"=>\"PRIVATE DATA HIDDEN\", \"memory\"=>512, \"buildpack\"=>\"ruby_buildpack\", \"environment_json\"=>\"PRIVATE DATA HIDDEN\"})', 'messageType': 'OUT', 'sourceType': 'API', 'sourceInstance': '0', 'appId': '5fb7bdb2-5478-4670-935d-7eda86cdb2b6'}";

		Map<String, String> parsedRecord = testParsing(apiTestSix, capiPayloadPattern, CAPI_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(CAPI_OUTER_FIELDS + CAPI_INNER_FIELDS, parsedRecord.size());

		// CAPI log timestamp
		Assert.assertEquals("1494270451956", parsedRecord.get("date"));

		// message text
		Assert.assertEquals("Updated app with guid 5fb7bdb2-5478-4670-935d-7eda86cdb2b6 ", parsedRecord.get("1"));

		// message payload field 1
		Assert.assertEquals("api-staging-agora", parsedRecord.get("2"));

		// message payload field 2
		Assert.assertEquals("\"PRIVATE DATA HIDDEN\"", parsedRecord.get("3"));

		// message payload field 3
		Assert.assertEquals("512", parsedRecord.get("4"));

		// message payload field 4
		Assert.assertEquals("\"ruby_buildpack\"", parsedRecord.get("5"));

		// message payload field 5
		Assert.assertEquals("\"PRIVATE DATA HIDDEN\"", parsedRecord.get("6"));

		// message payload field 6
		Assert.assertEquals(null, parsedRecord.get("7"));

		// message payload field 7
		Assert.assertEquals(null, parsedRecord.get("8"));

		// message payload field 8
		Assert.assertEquals(null, parsedRecord.get("9"));

		// message payload field 9
		Assert.assertEquals(null, parsedRecord.get("10"));

		// message payload field 10
		Assert.assertEquals(null, parsedRecord.get("11"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("API", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("0", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("5fb7bdb2-5478-4670-935d-7eda86cdb2b6", parsedRecord.get("appId"));
	}

	@Test
	public void testRouterParseOne() throws JSONException {
		String testCaseOne = "{'date': 1493536563339, 'message': 'api.europeana.eu - [2017-04-30T07:16:02.350+0000] \"GET /api/v2/thumbnail-by-url.json?size=w400&type=IMAGE&uri=https%3A%2F%2Fwww.rijksmuseum.nl%2Fassetimage2.jsp%3Fid%3DRP-F-2007-357-64 HTTP/1.1\" 200 0 107210 \"http://www.europeana.eu/portal/da/collections/art?f%5BREUSABILITY%5D%5B%5D=open&f%5BTYPE%5D%5B%5D=IMAGE&page=9&q=Finland\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36 OPR/44.0.2510.1449\" \"10.10.66.242:22920\" \"10.10.148.77:61015\" x_forwarded_for:\"5.103.56.83, 54.158.195.39\" x_forwarded_proto:\"http\" vcap_request_id:\"0e1e58b1-8f29-4372-7150-d1a90513629a\" response_time:0.989416602 app_id:\"aea3bb1a-4c84-4b3f-95c8-317621b7f962\" app_index:\"1\" x_b3_traceid:\"2b22064ddf4a1487\" x_b3_spanid:\"2b22064ddf4a1487\" x_b3_parentspanid:\"-\"\n', 'messageType': 'OUT', 'sourceType': 'RTR', 'sourceInstance': '5', 'appId': 'aea3bb1a-4c84-4b3f-95c8-317621b7f962', 'traceId': '2b22064ddf4a1487', 'spanId': '2b22064ddf4a1487'}";

		Map<String, String> parsedRecord = testParsing(testCaseOne, routerRegex, ROUTER_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(ROUTER_OUTER_FIELDS + ROUTER_INNER_FIELDS, parsedRecord.size());

		// Router log timestamp
		Assert.assertEquals("1493536563339", parsedRecord.get("date"));

		// Originating host
		Assert.assertEquals("api.europeana.eu", parsedRecord.get("1"));

		// Request start timestamp
		Assert.assertEquals("2017-04-30T07:16:02.350+0000", parsedRecord.get("2"));

		// Request type and URL
		Assert.assertEquals(
				"GET /api/v2/thumbnail-by-url.json?size=w400&type=IMAGE&uri=https%3A%2F%2Fwww.rijksmuseum.nl%2Fassetimage2.jsp%3Fid%3DRP-F-2007-357-64 HTTP/1.1",
				parsedRecord.get("3"));

		// Response code
		Assert.assertEquals("200", parsedRecord.get("4"));

		// Bytes received
		Assert.assertEquals("0", parsedRecord.get("5"));

		// Bytes sent
		Assert.assertEquals("107210", parsedRecord.get("6"));

		// Referring host
		Assert.assertEquals(
				"http://www.europeana.eu/portal/da/collections/art?f%5BREUSABILITY%5D%5B%5D=open&f%5BTYPE%5D%5B%5D=IMAGE&page=9&q=Finland",
				parsedRecord.get("7"));

		// User-agent string
		Assert.assertEquals(
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36 OPR/44.0.2510.1449",
				parsedRecord.get("8"));

		// Source IP
		Assert.assertEquals("10.10.66.242", parsedRecord.get("9"));

		// Source port
		Assert.assertEquals("22920", parsedRecord.get("10"));

		// Destination IP
		Assert.assertEquals("10.10.148.77", parsedRecord.get("11"));

		// Destination port
		Assert.assertEquals("61015", parsedRecord.get("12"));

		// x-forwarded-for
		Assert.assertEquals("5.103.56.83, 54.158.195.39", parsedRecord.get("13"));

		// x-forwarded-for-protocol
		Assert.assertEquals("http", parsedRecord.get("14"));

		// VCAP request id
		Assert.assertEquals("0e1e58b1-8f29-4372-7150-d1a90513629a", parsedRecord.get("15"));

		// Response time
		Assert.assertEquals("0.989416602", parsedRecord.get("16"));

		// Application id
		Assert.assertEquals("aea3bb1a-4c84-4b3f-95c8-317621b7f962", parsedRecord.get("17"));

		// Application index number
		Assert.assertEquals("1", parsedRecord.get("18"));

		// x_b3_trace_id
		Assert.assertEquals("2b22064ddf4a1487", parsedRecord.get("19"));

		// x_b3_span_id
		Assert.assertEquals("2b22064ddf4a1487", parsedRecord.get("20"));

		// x_b3_parentspan_id
		Assert.assertEquals("-", parsedRecord.get("21"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("RTR", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("5", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("aea3bb1a-4c84-4b3f-95c8-317621b7f962", parsedRecord.get("appId"));

		// Trace id
		Assert.assertEquals("2b22064ddf4a1487", parsedRecord.get("traceId"));

		// Span id
		Assert.assertEquals("2b22064ddf4a1487", parsedRecord.get("spanId"));
	}

	@Test
	public void testRouterParseTwo() throws JSONException {
		String testCaseTwo = "{'date': 1493576881806, 'message': 'iuhad89fgyphauihdfg9p8h.cfapps.io - [2017-04-30T18:28:01.803+0000] \"POST /events HTTP/1.1\" 200 584 2 \"https://iuhad89fgyphauihdfg9p8h.cfapps.io/events\" \"SendGrid Event API\" \"10.10.66.97:32150\" \"10.10.147.108:61106\" x_forwarded_for:\"167.89.125.250\" x_forwarded_proto:\"https\" vcap_request_id:\"c101367d-88ea-4d04-6fb3-02f064a33796\" response_time:0.00280887 app_id:\"f3634f33-65fd-4661-a221-99ce2eddea84\" app_index:\"1\" x_b3_traceid:\"ed27dbb0cbaa9401\" x_b3_spanid:\"ed27dbb0cbaa9401\" x_b3_parentspanid:\"-\"\n', 'messageType': 'OUT', 'sourceType': 'RTR', 'sourceInstance': '5', 'appId': 'f3634f33-65fd-4661-a221-99ce2eddea84', 'traceId': 'ed27dbb0cbaa9401', 'spanId': 'ed27dbb0cbaa9401'}";
		Map<String, String> parsedRecord = testParsing(testCaseTwo, routerRegex, ROUTER_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(ROUTER_OUTER_FIELDS + ROUTER_INNER_FIELDS, parsedRecord.size());

		// Router log timestamp
		Assert.assertEquals("1493576881806", parsedRecord.get("date"));

		// Originating host
		Assert.assertEquals("iuhad89fgyphauihdfg9p8h.cfapps.io", parsedRecord.get("1"));

		// Request start timestamp
		Assert.assertEquals("2017-04-30T18:28:01.803+0000", parsedRecord.get("2"));

		// Request type and URL
		Assert.assertEquals("POST /events HTTP/1.1", parsedRecord.get("3"));

		// Response code
		Assert.assertEquals("200", parsedRecord.get("4"));

		// Bytes received
		Assert.assertEquals("584", parsedRecord.get("5"));

		// Bytes sent
		Assert.assertEquals("2", parsedRecord.get("6"));

		// Referring host
		Assert.assertEquals("https://iuhad89fgyphauihdfg9p8h.cfapps.io/events", parsedRecord.get("7"));

		// User-agent string
		Assert.assertEquals("SendGrid Event API", parsedRecord.get("8"));

		// Source IP
		Assert.assertEquals("10.10.66.97", parsedRecord.get("9"));

		// Source port
		Assert.assertEquals("32150", parsedRecord.get("10"));

		// Destination IP
		Assert.assertEquals("10.10.147.108", parsedRecord.get("11"));

		// Destination port
		Assert.assertEquals("61106", parsedRecord.get("12"));

		// x-forwarded-for
		Assert.assertEquals("167.89.125.250", parsedRecord.get("13"));

		// x-forwarded-for-protocol
		Assert.assertEquals("https", parsedRecord.get("14"));

		// VCAP request id
		Assert.assertEquals("c101367d-88ea-4d04-6fb3-02f064a33796", parsedRecord.get("15"));

		// Response time
		Assert.assertEquals("0.00280887", parsedRecord.get("16"));

		// Application id
		Assert.assertEquals("f3634f33-65fd-4661-a221-99ce2eddea84", parsedRecord.get("17"));

		// Application index number
		Assert.assertEquals("1", parsedRecord.get("18"));

		// x_b3_trace_id
		Assert.assertEquals("ed27dbb0cbaa9401", parsedRecord.get("19"));

		// x_b3_span_id
		Assert.assertEquals("ed27dbb0cbaa9401", parsedRecord.get("20"));

		// x_b3_parentspan_id
		Assert.assertEquals("-", parsedRecord.get("21"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("RTR", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("5", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("f3634f33-65fd-4661-a221-99ce2eddea84", parsedRecord.get("appId"));

		// Trace id
		Assert.assertEquals("ed27dbb0cbaa9401", parsedRecord.get("traceId"));

		// Span id
		Assert.assertEquals("ed27dbb0cbaa9401", parsedRecord.get("spanId"));
	}

	@Test
	public void testRouterParseThree() throws JSONException {
		String testCaseThree = "{'date': 1494292420506, 'message': 'discovery-service-noninclinatory-distilery.cfapps.io - [2017-05-09T01:13:40.504+0000] \"PUT /eureka/apps/INVENTORY-SERVICE/6f030a2a-b6c5-4d8f-6f4a-f49b?status=UP&lastDirtyTimestamp=1494276865348 HTTP/1.1\" 200 0 0 \"-\" \"Java-EurekaClient/v1.4.6\" \"10.10.2.224:46667\" \"10.10.147.52:61075\" x_forwarded_for:\"54.166.89.110\" x_forwarded_proto:\"http\" vcap_request_id:\"da8e22bb-52a9-4df7-6df5-4f58d6aea45f\" response_time:0.002281495 app_id:\"8585c6d8-75aa-4141-af46-6aa6ea50ab83\" app_index:\"0\" x_b3_traceid:\"4a4f2642bd538c0c\" x_b3_spanid:\"4a4f2642bd538c0c\" x_b3_parentspanid:\"-\"\n', 'messageType': 'OUT', 'sourceType': 'RTR', 'sourceInstance': '2', 'appId': '8585c6d8-75aa-4141-af46-6aa6ea50ab83', 'traceId': '4a4f2642bd538c0c', 'spanId': '4a4f2642bd538c0c'}";

		Map<String, String> parsedRecord = testParsing(testCaseThree, routerRegex, ROUTER_INNER_FIELDS);

		// Quick fail test
		Assert.assertEquals(ROUTER_OUTER_FIELDS + ROUTER_INNER_FIELDS, parsedRecord.size());

		// Router log timestamp
		Assert.assertEquals("1494292420506", parsedRecord.get("date"));

		// Originating host
		Assert.assertEquals("discovery-service-noninclinatory-distilery.cfapps.io", parsedRecord.get("1"));

		// Request start timestamp
		Assert.assertEquals("2017-05-09T01:13:40.504+0000", parsedRecord.get("2"));

		// Request type and URL
		Assert.assertEquals(
				"PUT /eureka/apps/INVENTORY-SERVICE/6f030a2a-b6c5-4d8f-6f4a-f49b?status=UP&lastDirtyTimestamp=1494276865348 HTTP/1.1",
				parsedRecord.get("3"));

		// Response code
		Assert.assertEquals("200", parsedRecord.get("4"));

		// Bytes received
		Assert.assertEquals("0", parsedRecord.get("5"));

		// Bytes sent
		Assert.assertEquals("0", parsedRecord.get("6"));

		// Referring host
		Assert.assertEquals("-", parsedRecord.get("7"));

		// User-agent string
		Assert.assertEquals("Java-EurekaClient/v1.4.6", parsedRecord.get("8"));

		// Source IP
		Assert.assertEquals("10.10.2.224", parsedRecord.get("9"));

		// Source port
		Assert.assertEquals("46667", parsedRecord.get("10"));

		// Destination IP
		Assert.assertEquals("10.10.147.52", parsedRecord.get("11"));

		// Destination port
		Assert.assertEquals("61075", parsedRecord.get("12"));

		// x-forwarded-for
		Assert.assertEquals("54.166.89.110", parsedRecord.get("13"));

		// x-forwarded-for-protocol
		Assert.assertEquals("http", parsedRecord.get("14"));

		// VCAP request id
		Assert.assertEquals("da8e22bb-52a9-4df7-6df5-4f58d6aea45f", parsedRecord.get("15"));

		// Response time
		Assert.assertEquals("0.002281495", parsedRecord.get("16"));

		// Application id
		Assert.assertEquals("8585c6d8-75aa-4141-af46-6aa6ea50ab83", parsedRecord.get("17"));

		// Application index number
		Assert.assertEquals("0", parsedRecord.get("18"));

		// x_b3_trace_id
		Assert.assertEquals("4a4f2642bd538c0c", parsedRecord.get("19"));

		// x_b3_span_id
		Assert.assertEquals("4a4f2642bd538c0c", parsedRecord.get("20"));

		// x_b3_parentspan_id
		Assert.assertEquals("-", parsedRecord.get("21"));

		// Message type (OUT/ERR)
		Assert.assertEquals("OUT", parsedRecord.get("messageType"));

		// Source type
		Assert.assertEquals("RTR", parsedRecord.get("sourceType"));

		// Source instance
		Assert.assertEquals("2", parsedRecord.get("sourceInstance"));

		// Application ID
		Assert.assertEquals("8585c6d8-75aa-4141-af46-6aa6ea50ab83", parsedRecord.get("appId"));

		// Trace id
		Assert.assertEquals("4a4f2642bd538c0c", parsedRecord.get("traceId"));

		// Span id
		Assert.assertEquals("4a4f2642bd538c0c", parsedRecord.get("spanId"));
	}
}
