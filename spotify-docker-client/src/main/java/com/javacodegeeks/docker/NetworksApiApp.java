package com.javacodegeeks.docker;

import java.util.List;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkCreation;

public class NetworksApiApp {
	public static void main(String[] args) throws Exception {
		new NetworksApiApp().run();
	}

	private void run() throws Exception {
		final DockerClient client = DefaultDockerClient
			.fromEnv()
			.build();
			
		// Create the network
		final NetworkCreation network = client.createNetwork(
			NetworkConfig			
				.builder()
				.name("test-network")
				.driver("bridge")
				.build()
			);
		System.out.println("\n=== client.createNetwork");
		System.out.println(network);
		
		// Inspect the network
		final Network info = client.inspectNetwork(network.id());
		System.out.println("\n=== client.inspectNetwork");
		System.out.println(info);
		
		// List all networks
		final List<Network> networks = client.listNetworks();
		System.out.println("\n=== client.listNetworks");
		System.out.println(networks);
		
		// Create container
		final ContainerCreation container = client.createContainer(ContainerConfig
			.builder()
			.image("mysql:8.0.2")
			.env(
				"MYSQL_ROOT_PASSWORD=p$ssw0rd", 
				"MYSQL_DATABASE=my_app_db"
			)
			.exposedPorts("3306")
			.build()
		);

		// Connect the container to network
		client.startContainer(container.id());
		client.connectToNetwork(container.id(), network.id());
				
		// Disconnect the container from network
		client.disconnectFromNetwork(container.id(), network.id());
		
		// Remove the container
		client.stopContainer(container.id(), 5);
		client.removeContainer(container.id());
				
		// Remove the network
		client.removeNetwork(network.id());
		
		client.close();
	}
	
}
