package com.javacodegeeks.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImagesApiApp {

  public static void main(String[] args) throws Exception {
    new ImagesApiApp().run();
  }

  private void run() throws Exception {
    final DockerClient client = DockerClientBuilder.getInstance().build();

    final URL dockerfile = getClass().getResource("/Dockerfile");

    // Build image
    Set<String> tags = new HashSet<>();
    tags.add("base:openjdk-131-jdk");
    final String imageId = client.buildImageCmd()
        .withDockerfile(Paths.get(dockerfile.toURI()).toFile())
        .withTags(tags)
        .exec(new BuildImageResultCallback())
        .awaitImageId();

    // Inspect image
    final InspectImageResponse imageInfo = client.inspectImageCmd(imageId).exec();
    System.out.println("\n=== client.inspectImage");
    System.out.println(imageInfo);

    // List all images
    final List<Image> allImages = client.listImagesCmd().exec();
    System.out.println("\n=== client.listImages");
    allImages.forEach(System.out::println);

    // Tag the image
    client.tagImageCmd(imageId, "openjdk", "latest");

    // Check image history
    // TODO not available?
//		final List<ImageHistory> history = client.history(imageId);
//		System.out.println("\n=== client.history");
//		history.forEach(System.out::println);

    // Search images in the Docker Hub
    final List<SearchItem> jdkImages = client.searchImagesCmd("jdk").exec();
    System.out.println("\n=== client.searchImages");
    jdkImages.forEach(System.out::println);

    // Remove image
    client.removeImageCmd(imageId).exec();
//    final List<RemovedImage> removedImages = client.removeImageCmd(imageId).exec();
//		System.out.println("\n=== client.removeImage");
//		removedImages.forEach(System.out::println);

    client.close();
  }
}
