package org.eclipse.kuksa.roverCloudDemo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.eclipse.hono.client.HonoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
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

	public CommandControl(@Value("${hono.tenant.id}") final String honoTenantID,
			@Value("${hono.device.id}") final String honoDeviceID) {
		this.honoTenantID = honoTenantID;
		this.honoDeviceID = honoDeviceID;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// Query the latest telemetry data from the according Rover
		System.out.println(influxDB.getLatestTelemetryData(honoDeviceID).toString());

		// Create a drive forward command as JSON and send it to the Rover
		hmap = new HashMap<String, String>();

		Scanner sc = new Scanner(System.in);
		System.out.println("Enter command (Single char)");
		String command = sc.nextLine();
		if (!command.isEmpty() && isChar(command)) {
			hmap.put("command", command);
			System.out.println("Enter speed (0-360)");
			String speed = sc.nextLine();
			if (!speed.isEmpty() && isInteger(speed)) {
				hmap.put("speed", speed);

				// Create CommandClient for sending Command to a specific device
				final Future<HonoClient> clientFuture = this.honoConnector.connectToHono();
				clientFuture.map(client -> {
					client.getOrCreateCommandClient(honoTenantID, honoDeviceID).map(commandClient -> {
						commandClient.sendCommand("RoverDriving", buildCommandPayload(hmap));
						return commandClient;
					});
					return client;
				});
			} else {
				System.out.println("Invalid speed value");
			}
		} else {
			System.out.println("Invalid command value");
		}
	}

	private Buffer buildCommandPayload(HashMap<String, String> hmap) {
		JsonObject jsonCmd = new JsonObject();
		Set set = hmap.entrySet();
		Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry) iterator.next();
			jsonCmd.put(mentry.getKey().toString(), mentry.getValue());
		}
		return Buffer.buffer(jsonCmd.encode());
	}

	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	private static boolean isChar(String str) {
		if (str.length() != 1)
			return false;
		char c = Character.toLowerCase(str.charAt(0));
		return c >= 'a' && c <= 'z';
	}

}
