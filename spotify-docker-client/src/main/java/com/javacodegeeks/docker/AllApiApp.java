package com.javacodegeeks.docker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Healthcheck;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

public class AllApiApp {
	private static final Logger LOG = LoggerFactory.getLogger(AllApiApp.class);
	
	public static void main(String[] args) throws DockerCertificateException, DockerException, InterruptedException {
		final DockerClient client = DefaultDockerClient
				.fromEnv()
				.build();
				
		// Pull the image first
		client.pull("mysql:8.0.2");
		
		// Create container
		final ContainerCreation container = client.createContainer(ContainerConfig
			.builder()
			.image("mysql:8.0.2")
			.env(
				"MYSQL_ROOT_PASSWORD=p$ssw0rd", 
				"MYSQL_DATABASE=my_app_db"
			)
			.exposedPorts("3306")
			.healthcheck(
				Healthcheck
					.create(
					    // MySQL Docker image doesn't have `nc` available
						Arrays.asList("CMD-SHELL", "ss -ltn src :3306 | grep 3306"), 
						5000000000L, /* 5 secs, in nanoseconds */ 
						3000000000L, /* 3 secs, in nanoseconds */
						5
					)
			)
			.hostConfig(
				HostConfig 
					.builder()
					.portBindings(
						ImmutableMap.of(
							"3306", 
							ImmutableList.of(
								PortBinding.of("0.0.0.0", 0 /* use random port */)
							)
						)
					)
					.build()
			)
			.build()
		);
		
		// Start the container
		client.startContainer(container.id());
			
		// Inspect the container's health
		ContainerInfo info = client.inspectContainer(container.id());
		LOG.info("The container {} is {} ...", container.id(), info.state().health().status());
		
		while (info.state().health().status().equalsIgnoreCase("starting")) {
			// Await for container's health check to pass or fail
			Thread.sleep(1000);
			
			// Ask for container status
			info = client.inspectContainer(container.id());
			LOG.info("The container {} is {} ...", container.id(), info.state().health().status());
			
			// Along with health, better to check the container status as well
			if (info.state().status().equalsIgnoreCase("exited")) {
				LOG.info("The container {} has exited unexpectedly ...", container.id());
				break;
			}
		}

		// Check if container is healthy
		if (info.state().health().status().equalsIgnoreCase("healthy")) {
			final PortBinding binding = info
				.networkSettings()
				.ports()
				.get("3306/tcp")
				.get(0);
			
			final int port = Integer.valueOf(binding.hostPort());
			LOG.info("The MySQL host is IP is {}, port is {} ...", client.getHost(), port);
			
			final String url = String.format(
				"jdbc:mysql://%s:%d/my_app_db?user=root&password=p$ssw0rd&verifyServerCertificate=false", 
					client.getHost(), port);

			try (Connection connection = DriverManager.getConnection(url)) {
				try(ResultSet results = connection.getMetaData().getCatalogs()) {
					while (results.next()) {
						LOG.info("Found catalog '{}'", results.getString(1));
					}
				}
			} catch (SQLException ex) {
				LOG.error("MySQL connection problem", ex);
			}
		}
		
		// Stop the container
		client.stopContainer(container.id(), 5);
		client.removeContainer(container.id());
		
		client.close();
	}
}
