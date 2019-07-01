package org.eclipse.kuksa.roverCloudDemo;

import org.eclipse.hono.client.ApplicationClientFactory;
import org.eclipse.hono.client.CommandClient;
import org.eclipse.hono.client.HonoConnection;
import org.eclipse.hono.config.ClientConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@Component
public class HonoConnector {

	/* standard logger for information and error output */
	private static final Logger LOGGER = LoggerFactory.getLogger(HonoConnector.class);

	/* vertx instance opened to connect to Hono Messaging, needs to be closed */
	private final Vertx vertx = Vertx.vertx();
	
	/*
	 * client used to connect to the Hono Messaging Service
	 */
	private final ApplicationClientFactory clientFactory;
	@Autowired
	private ApplicationContext appContext;
	private String honoTenantID;
	private String honoDeviceID;

	/**
	 * Creates a new client to connect to Hono Messaging and forward the received
	 * messages to a message handler of choice.
	 *
	 * @param qpidRouterHost       url of the dispatch router to connect to
	 * @param qpidRouterPort       port of the dispatch router to use
	 * @param honoUser             user to authorize with Hono Messaging
	 * @param honoPassword         password to authorize with Hono Messaging
	 * @param honoTrustedStorePath path to the certificate file used to connect to
	 *                             Hono Messaging
	 */
	public HonoConnector(@Value("${qpid.router.host}") final String qpidRouterHost,
			@Value("${qpid.router.port}") final int qpidRouterPort, @Value("${hono.user}") final String honoUser,
			@Value("${hono.password}") final String honoPassword,
			@Value("${hono.trustedStorePath}") final String honoTrustedStorePath,
			@Value("${hono.reconnectAttempts}") final int reconnectAttempts,
			@Value("${hono.tenant.id}") final String honoTenantID,
			@Value("${hono.device.id}") final String honoDeviceID) {

		this.honoTenantID = honoTenantID;
		this.honoDeviceID = honoDeviceID;

		final ClientConfigProperties messagingProps = new ClientConfigProperties();
		messagingProps.setHost(qpidRouterHost);
		messagingProps.setPort(qpidRouterPort);
		messagingProps.setUsername(honoUser);
		messagingProps.setPassword(honoPassword);
		messagingProps.setTrustStorePath("target/classes/" + honoTrustedStorePath);
		messagingProps.setHostnameVerificationRequired(false);

		clientFactory = ApplicationClientFactory.create(HonoConnection.newConnection(vertx, messagingProps));
	}

	/**
	 * Shuts down the connector and calls Spring Boot to terminate.
	 */
	private void shutdown() {
		vertx.close();
		LOGGER.info("Shutting connector down...");

		SpringApplication.exit(appContext, () -> 0);
	}

	public Future<CommandClient> createCommandClient() {
		//TBD
		return null;
	}

}
