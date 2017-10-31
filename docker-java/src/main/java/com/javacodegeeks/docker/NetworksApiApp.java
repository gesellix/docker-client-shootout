package com.javacodegeeks.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.List;

public class NetworksApiApp {
  public static void main(String[] args) throws Exception {
    new NetworksApiApp().run();
  }

  private void run() throws Exception {
    final DockerClient client = DockerClientBuilder.getInstance().build();

    // Create the network
    final CreateNetworkResponse network = client.createNetworkCmd()
        .withName("test-network")
        .withDriver("bridge")
        .exec();
    System.out.println("\n=== client.createNetwork");
    System.out.println(network);

    // Inspect the network
    final Network info = client.inspectNetworkCmd()
        .withNetworkId(network.getId())
        .exec();
    System.out.println("\n=== client.inspectNetwork");
    System.out.println(info);

    // List all networks
    final List<Network> networks = client.listNetworksCmd().exec();
    System.out.println("\n=== client.listNetworks");
    System.out.println(networks);

    // Create container
    final CreateContainerResponse container = client.createContainerCmd("mysql:8.0.2")
        .withEnv("MYSQL_ROOT_PASSWORD=p$ssw0rd",
                 "MYSQL_DATABASE=my_app_db")
        .withExposedPorts(ExposedPort.tcp(3306))
        .exec();

    // Connect the container to network
    client.startContainerCmd(container.getId()).exec();
    client.connectToNetworkCmd()
        .withContainerId(container.getId())
        .withNetworkId(network.getId())
        .exec();

    // Disconnect the container from network
    client.disconnectFromNetworkCmd()
        .withContainerId(container.getId())
        .withNetworkId(network.getId())
        .exec();

    // Remove the container
    client.stopContainerCmd(container.getId()).exec();
    client.removeContainerCmd(container.getId()).exec();

    // Remove the network
    client.removeNetworkCmd(network.getId()).exec();

    client.close();
  }
}
