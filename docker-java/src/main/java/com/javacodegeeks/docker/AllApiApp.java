package com.javacodegeeks.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AllApiApp {
  private static final Logger LOG = LoggerFactory.getLogger(AllApiApp.class);

  public static void main(String[] args) throws InterruptedException, IOException {
    final DockerClient client = DockerClientBuilder.getInstance().build();
    String host = "localhost";

    // Pull the image first
    client.pullImageCmd("mysql:8.0.2").exec(new PullImageResultCallback());

    // Create container
    final CreateContainerResponse container = client.createContainerCmd("mysql:8.0.2")
        .withEnv("MYSQL_ROOT_PASSWORD=p$ssw0rd",
                 "MYSQL_DATABASE=my_app_db")
        .withExposedPorts(ExposedPort.tcp(3306))
        .withPortBindings(PortBinding.parse("0.0.0.0:0:3306/tcp"))
        // TODO withHealthcheck()?
        .exec();

    // Start the container
    client.startContainerCmd(container.getId()).exec();

    // Inspect the container's health
    InspectContainerResponse info = client.inspectContainerCmd(container.getId()).exec();
    if (info.getState().getHealth() == null) {
      LOG.info("Couldn't determine container.state.health.* for id {}", container.getId());
      client.stopContainerCmd(container.getId()).exec();
      client.removeContainerCmd(container.getId()).exec();
      client.close();
      return;
    }
    LOG.info("The container {} is {} ...", container.getId(), info.getState().getHealth().getStatus());

    while (info.getState().getHealth().getStatus().equalsIgnoreCase("starting")) {
      // Await for container's health check to pass or fail
      Thread.sleep(1000);

      // Ask for container status
      info = client.inspectContainerCmd(container.getId()).exec();
      LOG.info("The container {} is {} ...", container.getId(), info.getState().getHealth().getStatus());

      // Along with health, better to check the container status as well
      if (info.getState().getStatus().equalsIgnoreCase("exited")) {
        LOG.info("The container {} has exited unexpectedly ...", container.getId());
        break;
      }
    }

    // Check if container is healthy
    if (info.getState().getHealth().getStatus().equalsIgnoreCase("healthy")) {
      final Ports.Binding binding = info
          .getNetworkSettings()
          .getPorts()
          .getBindings().get(ExposedPort.parse("3306/tcp"))[0];

      final int port = Integer.valueOf(binding.getHostPortSpec());
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
    client.stopContainerCmd(container.getId()).exec();
    client.removeContainerCmd(container.getId()).exec();

    client.close();
  }
}
