package com.javacodegeeks.docker;

import com.google.common.collect.ImmutableMap;
import de.gesellix.docker.client.DockerClient;
import de.gesellix.docker.client.DockerClientImpl;
import de.gesellix.docker.client.builder.BuildContextBuilder;
import de.gesellix.docker.engine.EngineResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImagesApiApp {

  public static void main(String[] args) throws Exception {
    new ImagesApiApp().run();
  }

  private void run() throws Exception {
    final DockerClient client = new DockerClientImpl();

    final URL dockerfile = getClass().getResource("/Dockerfile");

    // Build image
    final String imageId = (String) client.build(newBuildContext(new File(dockerfile.toURI()).getParentFile()),
                                                 ImmutableMap.of("rm", true,
                                                                 "t", "base:openjdk-131-jdk"));

    // Inspect image
    final EngineResponse imageInfo = client.inspectImage(imageId);
    System.out.println("\n=== client.inspectImage");
    System.out.println(imageInfo.getContent());

    // List all images
    final EngineResponse allImages = client.images();
    System.out.println("\n=== client.listImages");
    System.out.println(allImages.getContent());

    // Tag the image
    client.tag(imageId, "openjdk");

    // Check image history
    final EngineResponse history = client.history(imageId);
    System.out.println("\n=== client.history");
    System.out.println(history.getContent());

    // Search images in the Docker Hub
    final EngineResponse jdkImages = (EngineResponse) client.search("jdk");
    System.out.println("\n=== client.searchImages");
    System.out.println(jdkImages.getContent());

    // Remove image
    final EngineResponse removedImages = client.rmi("base:openjdk-131-jdk");
    System.out.println("\n=== client.removeImage");
    System.out.println(removedImages.getContent());
  }

  private InputStream newBuildContext(File baseDirectory) {
    try {
      File buildContext = File.createTempFile("buildContext", ".tar");
      buildContext.deleteOnExit();
      BuildContextBuilder.archiveTarFilesRecursively(baseDirectory, buildContext);
      return new FileInputStream(buildContext);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
