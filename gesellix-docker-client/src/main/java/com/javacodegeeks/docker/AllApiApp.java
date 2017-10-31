package com.javacodegeeks.docker;

import com.google.common.collect.ImmutableMap;
import de.gesellix.docker.client.DockerClient;
import de.gesellix.docker.client.DockerClientImpl;
import de.gesellix.docker.engine.EngineResponse;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class AllApiApp {
  private static final Logger LOG = LoggerFactory.getLogger(AllApiApp.class);

  public static void main(String[] args) throws InterruptedException {
    final DockerClient client = new DockerClientImpl();
    String host = "localhost";

    // Pull the image first
//    client.pull("mysql", "8.0.2");
    client.create(ImmutableMap.of("fromImage", "mysql",
                                  "tag", "8.0.2"));

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
                "3306/tcp", singletonList(ImmutableMap.of(
                    "HostIp", "0.0.0.0",
                    "HostPort", "0")
                ))),
        "Healthcheck", ImmutableMap.of(
            "Test", Arrays.asList("CMD-SHELL", "ss -ltn src :3306 | grep 3306"),
            "Interval", 5000000000L,
            "Timeout", 3000000000L,
            "Retries", 5
        )
    ));
    String containerId = (String) ((Map) container.getContent()).get("Id");

    // Start the container
    client.startContainer(containerId);

    // Inspect the container's health
    Map info = (Map) client.inspectContainer(containerId).getContent();
    String healthStatus = getProperty(info, "State.Health.Status");
    LOG.info("The container {} is {} ...", containerId, healthStatus);

    while (healthStatus.equalsIgnoreCase("starting")) {
      // Await for container's health check to pass or fail
      Thread.sleep(1000);

      // Ask for container status
      info = (Map) client.inspectContainer(containerId).getContent();
      healthStatus = getProperty(info, "State.Health.Status");
      LOG.info("The container {} is {} ...", containerId, healthStatus);

      // Along with health, better to check the container status as well
      if (((String) getProperty(info, "State.Status")).equalsIgnoreCase("exited")) {
        LOG.info("The container {} has exited unexpectedly ...", containerId);
        break;
      }
    }

    // Check if container is healthy
    if (healthStatus.equalsIgnoreCase("healthy")) {
      Map binding = (Map) ((List) getProperty(info, "NetworkSettings.Ports.3306/tcp")).get(0);

      final int port = Integer.valueOf((String) binding.get("HostPort"));
      LOG.info("The MySQL host is IP is {}, port is {} ...", host, port);

      final String url = String.format(
          "jdbc:mysql://%s:%d/my_app_db?user=root&password=p$ssw0rd&verifyServerCertificate=false",
          host, port);

      try (Connection connection = DriverManager.getConnection(url)) {
        try (ResultSet results = connection.getMetaData().getCatalogs()) {
          while (results.next()) {
            LOG.info("Found catalog '{}'", results.getString(1));
          }
        }
      }
      catch (SQLException ex) {
        LOG.error("MySQL connection problem", ex);
      }
    }

    // Stop the container
    client.stop(containerId);
    client.rm(containerId);
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
