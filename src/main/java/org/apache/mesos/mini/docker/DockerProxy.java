package org.apache.mesos.mini.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import org.apache.mesos.mini.container.AbstractContainer;

import java.security.SecureRandom;

public class DockerProxy extends AbstractContainer {

    public static final String PROXY_IMAGE = "paintedfox/tinyproxy";
    public static final String CONTAINER_NAME = "tinyproxy";
    public static final String TAG = "latest";
    public final int proxyPort;

    public DockerProxy(DockerClient dockerClient, int port) {
        super(dockerClient);
        proxyPort = port;
    }

    @Override
    protected void pullImage() {
        pullImage(PROXY_IMAGE, TAG);
    }

    @Override
    protected CreateContainerCmd dockerCommand() {
        return dockerClient
                .createContainerCmd(PROXY_IMAGE + ":" + TAG)
                .withName(generateRegistryContainerName())
                .withExposedPorts(ExposedPort.parse("" + proxyPort))
                .withPortBindings(PortBinding.parse("0.0.0.0:" + proxyPort + ":8888"));
    }

    String generateRegistryContainerName() {
        return CONTAINER_NAME + "_" + new SecureRandom().nextInt();
    }

}