package com.javacodegeeks.docker;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.BuildParam;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageHistory;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.ImageSearchResult;
import com.spotify.docker.client.messages.RemovedImage;

public class ImagesApiApp {

	public static void main(String[] args) throws Exception {
		new ImagesApiApp().run();
	}

	private void run() throws Exception {
		final DockerClient client = DefaultDockerClient
			.fromEnv()
			.build();
			
		final URL dockerfile = getClass().getResource("/");
		
		// Build image
		final String imageId = client.build(Paths.get(dockerfile.toURI()), 
			BuildParam.name("base:openjdk-131-jdk"));
		
		// Inspect image
		final ImageInfo imageInfo = client.inspectImage(imageId);
		System.out.println("\n=== client.inspectImage");
		System.out.println(imageInfo);		
		
		// List all images
		final List<Image> allImages = client.listImages();
		System.out.println("\n=== client.listImages");
		allImages.forEach(System.out::println);
		
		// Tag the image
		client.tag(imageId, "openjdk");
		
		// Check image history
		final List<ImageHistory> history = client.history(imageId);
		System.out.println("\n=== client.history");
		history.forEach(System.out::println);
		
		// Search images in the Docker Hub
		final List<ImageSearchResult> jdkImages = client.searchImages("jdk");
		System.out.println("\n=== client.searchImages");
		jdkImages.forEach(System.out::println);
		
		// Remove image
		final List<RemovedImage> removedImages = client.removeImage(imageId, true, false);
		System.out.println("\n=== client.removeImage");
		removedImages.forEach(System.out::println);
		
		client.close();
	}
	
}
