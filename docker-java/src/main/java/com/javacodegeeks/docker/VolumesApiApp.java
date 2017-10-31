package com.javacodegeeks.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.ListVolumesResponse;
import com.github.dockerjava.core.DockerClientBuilder;

public class VolumesApiApp {
  public static void main(String[] args) throws Exception {
    new VolumesApiApp().run();
  }

  private void run() throws Exception {
    final DockerClient client = DockerClientBuilder.getInstance().build();

    // Create the volume
    final CreateVolumeResponse volume = client.createVolumeCmd().exec();
    System.out.println("\n=== client.createVolume");
    System.out.println(volume);

    // Inspect the volume
    final InspectVolumeResponse info = client.inspectVolumeCmd(volume.getName()).exec();
    System.out.println("\n=== client.inspectVolume");
    System.out.println(info);

    // List all volumes
    final ListVolumesResponse volumes = client.listVolumesCmd().exec();
    System.out.println("\n=== client.listVolumes");
    System.out.println(volumes);

    // Remove the volume
    client.removeVolumeCmd(volume.getName()).exec();

    client.close();
  }
}
