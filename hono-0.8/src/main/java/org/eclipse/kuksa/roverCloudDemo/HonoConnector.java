/*
 * ******************************************************************************
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 *  Contributors:
 *      Johannes Kristan (Bosch Software Innovations GmbH) - initial API and functionality
 *      Leon Graser (Bosch Software Innovations GmbH)
 * *****************************************************************************
 */

package org.eclipse.kuksa.roverCloudDemo;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClientOptions;
import org.eclipse.hono.client.HonoClient;
import org.eclipse.hono.config.ClientConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;

@Component
class HonoConnector{

	/* standard logger for information and error output */
	private static final Logger LOGGER = LoggerFactory.getLogger(HonoConnector.class);

	/* connection options for the connection to the Hono Messaging Service */
	private final ProtonClientOptions options;

	/* vertx instance opened to connect to Hono Messaging, needs to be closed */
	private final Vertx vertx;

	/*
	 * client used to connect to the Hono Messaging Service to receive new messages
	 */
	private final HonoClient honoClient;

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
	 * @param tenantId             tenant id
	 */
	HonoConnector(@Value("${qpid.router.host}") final String qpidRouterHost,
			@Value("${qpid.router.port}") final int qpidRouterPort, @Value("${hono.user}") final String honoUser,
			@Value("${hono.password}") final String honoPassword,
			@Value("${hono.trustedStorePath}") final String honoTrustedStorePath,
			@Value("${hono.tenant.id}") final String tenantId, @Value("${influxdb.url}") final String influxURL,
			@Value("${influxdb.db.name}") final String dbName) throws MalformedURLException {
		vertx = Vertx.vertx();
		ClientConfigProperties config = new ClientConfigProperties();
		config.setHost(qpidRouterHost);
		config.setPort(qpidRouterPort);
		config.setUsername(honoUser);
		config.setPassword(honoPassword);
		config.setTrustStorePath("target/classes/" + honoTrustedStorePath);
		config.setHostnameVerificationRequired(false);

		honoClient = HonoClient.newClient(vertx, config);

		options = new ProtonClientOptions();
		options.setConnectTimeout(10000);
	}

	/**
	 * Connects to the Hono based on the options defined in {@link #options}. The
	 * {@link #connectionHandler} is used as a callback for the connection attempt.
	 * 
	 * @return
	 */
	public Future<HonoClient> connectToHono() {
		LOGGER.info("Started connection attempt to Hono.");
		return honoClient.connect(options);
	}
}
