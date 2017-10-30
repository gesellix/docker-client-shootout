package com.javacodegeeks.docker;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.AttachParameter;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.ContainerUpdate;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.TopResults;

public class ContainersApiApp {

	public static void main(String[] args) throws Exception {
		new ContainersApiApp().run();
	}

	private void run() throws Exception {
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
			.hostConfig(
				HostConfig
					.builder()
					.portBindings(
						ImmutableMap.of(
							"3306", 
							ImmutableList.of(
								PortBinding.of("0.0.0.0", 3306)
							)
						)
					)
					.build()
			)
			.build()
		);
		System.out.println("\n=== client.createContainer");
		System.out.println(container);

		// Start the container
		client.startContainer(container.id());
		
		// Inspect the container
		final ContainerInfo info = client.inspectContainer(container.id());
		System.out.println("\n=== client.inspectContainer");
		System.out.println(info);
		
		// Get port mappings
		final ImmutableMap<String, List<PortBinding>> mappings = info
			.hostConfig()
			.portBindings();
		System.out.println("\n=== port mappings");
		System.out.println(mappings);			

		// Get all exposed ports
		final ImmutableMap<String, List<PortBinding>> ports = info
			.networkSettings()
			.ports();
		System.out.println("\n=== ports");
		System.out.println(ports);
			
		// Attach to container
		ForkJoinPool.commonPool().submit(
			() -> {
				client
					.attachContainer(container.id(), AttachParameter.values())
					.attach(System.out, System.err, false);
				return null;
			}
		);

		// Pause the container
		client.pauseContainer(container.id());

		// Unpause the container
		client.unpauseContainer(container.id());
		
		// Update container
		final ContainerUpdate update = client.updateContainer(container.id(), 
			HostConfig
				.builder()
				.memory(268435456L /* 256Mb */)
				.memorySwap(268435456L)
				.build()
			);
		System.out.println("\n=== client.updateContainer");
		System.out.println(update);
		
		// Get processes in the container
		final TopResults top = client.topContainer(container.id());
		System.out.println("\n=== client.topContainer");
		System.out.println(top);
		
		// Get the container statistics
		final ContainerStats stats = client.stats(container.id());
		System.out.println("\n=== client.stats");
		System.out.println(stats);
		
		// Get the container logs
		client
			.logs(container.id(), LogsParam.stdout(), LogsParam.stderr(), LogsParam.tail(10))
			.attach(System.out, System.err, false);
		
		// Start the container
		client.stopContainer(container.id(), 5 /* wait 5 seconds before killing */);

		// Remove container
		client.removeContainer(container.id());
		
		client.close();
	}
	
}
