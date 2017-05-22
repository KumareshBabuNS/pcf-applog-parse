package io.pivotal.smartarchive.regexparser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
	
	private String regex = "";
	
	StringWriter stringWriter = new StringWriter();
	CSVPrinter printer; 
	
	@Autowired
	public RegexParserConfiguration() throws IOException {
		this.printer = new CSVPrinter(stringWriter, CSVFormat.RFC4180.withDelimiter(','));
	}
	
	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object parseMessage(Object payload) {
		log.info(payload.toString());
		
		Map<String, String> payloadAsMap = (Map<String, String>) payload;
		
		Pattern pattern = Pattern.compile(regex);
		
		Matcher matcher = pattern.matcher((String)payloadAsMap.get("message"));
		
		if (matcher.matches() == false) {
			return null;
		}
		
		Map<String, String> parsedRecord = new HashMap<String, String>();
		
		// First populate the top level fields from JSON object
		for(String k : payloadAsMap.keySet() ){
			if (k != "message") {
				parsedRecord.put(k, payloadAsMap.get(k));
			}
		}
		
		Integer numFields = matcher.groupCount();
		
		if (numFields == 24) {
			/* If we have a complete pattern match, add fields individually */
			for (Integer f=0; f < numFields; ++f ) {
				parsedRecord.put(f.toString(), matcher.group(f));
			}
		} else {
			/* If we can't parse the discrete fields, send the raw data anyway */
			parsedRecord.put("message", payloadAsMap.get("message"));
		}
		
		return parsedRecord;
	}
}
