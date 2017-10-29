package com.javacodegeeks.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.VolumeList;

public class VolumesApiApp {
	public static void main(String[] args) throws Exception {
		new VolumesApiApp().run();
	}

	private void run() throws Exception {
		final DockerClient client = DefaultDockerClient
			.fromEnv()
			.build();
			
		// Create the volume
		final Volume volume = client.createVolume();
		System.out.println("\n=== client.createVolume");
		System.out.println(volume);
		
		// Inspect the volume
		final Volume info = client.inspectVolume(volume.name());
		System.out.println("\n=== client.inspectVolume");
		System.out.println(info);
		
		// List all volumes
		final VolumeList volumes = client.listVolumes();
		System.out.println("\n=== client.listVolumes");
		System.out.println(volumes);
		
		// Remove the volume
		client.removeVolume(volume.name());
		
		client.close();
	}
	
}
