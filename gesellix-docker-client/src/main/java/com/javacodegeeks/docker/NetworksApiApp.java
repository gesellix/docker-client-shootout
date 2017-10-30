package com.javacodegeeks.docker;

import com.google.common.collect.ImmutableMap;
import de.gesellix.docker.client.DockerClient;
import de.gesellix.docker.client.DockerClientImpl;
import de.gesellix.docker.engine.EngineResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NetworksApiApp {
  public static void main(String[] args) {
    new NetworksApiApp().run();
  }

  private void run() {
    final DockerClient client = new DockerClientImpl();

    // Create the network
    Map<String, Object> networkConfig = new HashMap<>();
    networkConfig.put("Driver", "bridge");
    final EngineResponse network = (EngineResponse) client.createNetwork("test-network", networkConfig);
    String networkId = (String) ((Map) network.getContent()).get("Id");
    System.out.println("\n=== client.createNetwork");
    System.out.println(network.getContent());

    // Inspect the network
    final EngineResponse info = client.inspectNetwork(networkId);
    System.out.println("\n=== client.inspectNetwork");
    System.out.println(info.getContent());

    // List all networks
    final EngineResponse networks = client.networks();
    System.out.println("\n=== client.listNetworks");
    System.out.println(networks.getContent());

    // Create container
    EngineResponse container = client.createContainer(ImmutableMap.of(
        "Image", "mysql:8.0.2",
        "Env", Arrays.asList(
            "MYSQL_ROOT_PASSWORD=p$ssw0rd",
            "MYSQL_DATABASE=my_app_db"
        ),
        "ExposedPorts", ImmutableMap.of("3306/tcp", Collections.emptyMap())
    ));
    String containerId = (String) ((Map) container.getContent()).get("Id");

    // Connect the container to network
    client.startContainer(containerId);
    client.connectNetwork(networkId, containerId);

    // Disconnect the container from network
    client.disconnectNetwork(networkId, containerId);

    // Remove the container
    client.stop(containerId);
    client.rm(containerId);

    // Remove the network
    client.rmNetwork(networkId);
  }
}
