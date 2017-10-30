package com.javacodegeeks.docker;

import com.google.common.collect.ImmutableMap;
import de.gesellix.docker.client.DockerAsyncCallback;
import de.gesellix.docker.client.DockerClient;
import de.gesellix.docker.client.DockerClientImpl;
import de.gesellix.docker.engine.AttachConfig;
import de.gesellix.docker.engine.EngineResponse;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class ContainersApiApp {

  public static void main(String[] args) {
    new ContainersApiApp().run();
  }

  private void run() {
    final DockerClient client = new DockerClientImpl();

    // Pull the image first
    client.pull("mysql", "8.0.2");

    // Create container
    EngineResponse container = client.createContainer(ImmutableMap.of(
        "Image", "mysql:8.0.2",
        "Env", Arrays.asList(
            "MYSQL_ROOT_PASSWORD=p$ssw0rd",
            "MYSQL_DATABASE=my_app_db"
        ),
        "ExposedPorts", ImmutableMap.of("3306/tcp", Collections.emptyMap()),
        "HostConfig", ImmutableMap.of(
            "PortBindings", ImmutableMap.of(
                "3306/tcp", Arrays.asList(ImmutableMap.of(
                    "HostIp", "0.0.0.0",
                    "HostPort", "3306")
                )))
    ));
    String containerId = (String) ((Map) container.getContent()).get("Id");

    System.out.println("\n=== client.createContainer");
    System.out.println(container.getContent());

    // Start the container
    client.startContainer(containerId);

    // Inspect the container
    final EngineResponse info = (EngineResponse) client.inspectContainer(containerId);
    System.out.println("\n=== client.inspectContainer");
    System.out.println(info.getContent());

    // Get port mappings
    final Map mappings = getProperty((Map) info.getContent(), "HostConfig.PortBindings");
    System.out.println("\n=== port mappings");
    System.out.println(mappings);

    // Get all exposed ports
    final Map ports = getProperty((Map) info.getContent(), "NetworkSettings.Ports");
    System.out.println("\n=== ports");
    System.out.println(ports);

    // Attach to container
    AttachConfig attachConfig = new AttachConfig();
    AttachConfig.Streams streams = new AttachConfig.Streams();
    streams.setStdout(System.out);
    streams.setStderr(System.err);
    attachConfig.setStreams(streams);
    ForkJoinPool.commonPool().submit(
        () -> {
          client.attach(containerId,
                        ImmutableMap.of("Stream", 1,
                                        "Stdin", 1,
                                        "Stdout", 1,
                                        "Stderr", 1),
                        attachConfig);
          return null;
        }
    );

    // Pause the container
    client.pause(containerId);

    // Unpause the container
    client.unpause(containerId);

    // Update container
    Map updateConfig = ImmutableMap.of(
        "Memory", 314572800,
        "MemorySwap", 514288000);
    final EngineResponse update = (EngineResponse) client.updateContainer(containerId,
                                                                          updateConfig);
    System.out.println("\n=== client.updateContainer");
    System.out.println(update.getContent());

    // Get processes in the container
    final EngineResponse top = (EngineResponse) client.top(containerId);
    System.out.println("\n=== client.topContainer");
    System.out.println(top.getContent());

    // Get the container statistics
    final EngineResponse stats = (EngineResponse) client.stats(containerId);
    System.out.println("\n=== client.stats");
    System.out.println(stats.getContent());

    // Get the container logs
    Map<String, Object> logConfig = new HashMap<>();
    logConfig.put("stdout", true);
    logConfig.put("stderr", true);
    logConfig.put("tail", 10);
    EngineResponse logs = (EngineResponse) client.logs(containerId, logConfig, new DockerAsyncCallback() {
      @Override
      public Object onEvent(Object event) {
        System.out.println(event);
        return null;
      }

      @Override
      public Object onFinish() {
        return null;
      }
    });
//        .attach(System.out, System.err, false);
//    System.out.println(IOUtils.copy(new RawInputStream((InputStream) logs.getStream()), System.out));

    // Start the container
    client.stop(containerId);

    // Remove container
    client.rm(containerId);

    logs.getTaskFuture().cancel(true);
  }

  private static <R> R getProperty(Map map, String name) {
    try {
      return (R) PropertyUtils.getProperty(map, name);
    }
    catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
