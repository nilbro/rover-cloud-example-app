package org.eclipse.kuksa.roverCloudDemo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.hono.client.CommandClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.eclipse.hono.client.HonoConnection;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

@Component
public class CommandControl implements ApplicationRunner {

	@Autowired
	private HonoConnector honoConnector;

	@Autowired
	private InfluxDBClient influxDB;

	HashMap<String, String> hmap;
	
	private String honoTenantID;

	private String honoDeviceID;

	public CommandControl(@Value("${hono.tenant.id}") final String honoTenantID, @Value("${hono.device.id}") final String honoDeviceID) {
		this.honoTenantID = honoTenantID;
		this.honoDeviceID = honoDeviceID;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// Query the latest telemetry data from the according Rover
		System.out.println(influxDB.getLatestTelemetryData(honoDeviceID).toString());

		// Create a drive forward command as JSON and send it to the Rover
		hmap = new HashMap<String, String>();
		hmap.put("command", "W");
		hmap.put("speed", "360");

		//TBD
	}

	private Buffer buildCommandPayload(HashMap<String, String> hmap) {
		JsonObject jsonCmd = new JsonObject();
		Set set = hmap.entrySet();
		Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry) iterator.next();
			jsonCmd.put(mentry.getKey().toString(), mentry.getValue());
		}
		return Buffer.buffer(jsonCmd.encodePrettily());
	}

}
