package com.javacodegeeks.docker;

import de.gesellix.docker.client.DockerClient;
import de.gesellix.docker.client.DockerClientImpl;
import de.gesellix.docker.engine.EngineResponse;
import org.apache.commons.collections.map.SingletonMap;

import java.util.Map;

public class VolumesApiApp {
  public static void main(String[] args) {
    new VolumesApiApp().run();
  }

  private void run() {
    final DockerClient client = new DockerClientImpl();

    // Create the volume
    final EngineResponse volume = client.createVolume(new SingletonMap("Name", null));
    System.out.println("\n=== client.createVolume");
    System.out.println(volume.getContent());

    // Inspect the volume
    final EngineResponse info = client.inspectVolume(((Map) volume.getContent()).get("Name"));
    System.out.println("\n=== client.inspectVolume");
    System.out.println(info.getContent());

    // List all volumes
    final EngineResponse volumes = client.volumes();
    System.out.println("\n=== client.listVolumes");
    System.out.println(volumes.getContent());

    // Remove the volume
    client.rmVolume(((Map) volume.getContent()).get("Name"));
  }
}
