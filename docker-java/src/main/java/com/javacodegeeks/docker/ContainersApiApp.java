package com.javacodegeeks.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.TopContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.UpdateContainerResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.util.concurrent.ForkJoinPool;

public class ContainersApiApp {

  public static void main(String[] args) throws Exception {
    new ContainersApiApp().run();
  }

  private void run() throws Exception {
    final DockerClient client = DockerClientBuilder.getInstance().build();

    // Pull the image first
    client.pullImageCmd("mysql:8.0.2").exec(new PullImageResultCallback());

    // Create container
    final CreateContainerResponse container = client.createContainerCmd("mysql:8.0.2")
        .withEnv("MYSQL_ROOT_PASSWORD=p$ssw0rd",
                 "MYSQL_DATABASE=my_app_db")
        .withExposedPorts(ExposedPort.tcp(3306))
        .withPortBindings(PortBinding.parse("0.0.0.0:3306:3306/tcp"))
        .exec();
    System.out.println("\n=== client.createContainer");
    System.out.println(container);

    // Start the container
    client.startContainerCmd(container.getId()).exec();

    // Inspect the container
    InspectContainerResponse info = client.inspectContainerCmd(container.getId()).exec();
    System.out.println("\n=== client.inspectContainer");
    System.out.println(info);

    // Get port mappings
    final Ports mappings = info
        .getHostConfig()
        .getPortBindings();
    System.out.println("\n=== port mappings");
    System.out.println(mappings);

    // Get all exposed ports
    final Ports ports = info
        .getNetworkSettings()
        .getPorts();
    System.out.println("\n=== ports");
    System.out.println(ports);

    // Attach to container
    ForkJoinPool.commonPool().submit(
        () -> {
          client
              .attachContainerCmd(container.getId())
              .withStdOut(true)
              .withStdErr(true);
          return null;
        }
    );

    // Pause the container
    client.pauseContainerCmd(container.getId()).exec();

    // Unpause the container
    client.unpauseContainerCmd(container.getId()).exec();

    // Update container
    final UpdateContainerResponse update = client.updateContainerCmd(container.getId())
        .withMemory(268435456L /* 256Mb */)
        .withMemorySwap(268435456L).exec();
    System.out.println("\n=== client.updateContainer");
    System.out.println(update);

    // Get processes in the container
    final TopContainerResponse top = client.topContainerCmd(container.getId()).exec();
    System.out.println("\n=== client.topContainer");
    System.out.println(top);

    // Get the container statistics
    final StatsCallback stats = client.statsCmd(container.getId()).exec(new StatsCallback());
    System.out.println("\n=== client.stats");
    System.out.println(stats);

    // Get the container logs
    client
        .logContainerCmd(container.getId())
        .withStdOut(true)
        .withStdErr(true)
        .withTail(10)
        .exec(new LogContainerResultCallback());

    // Start the container
    client.stopContainerCmd(container.getId()).exec();

    // Remove container
    client.removeContainerCmd(container.getId()).exec();

    client.close();
  }

  static class StatsCallback extends ResultCallbackTemplate<StatsCallback, Statistics> {

    @Override
    public void onNext(Statistics object) {

    }
  }
}
