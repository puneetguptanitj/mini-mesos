package org.apache.mesos.mini.marathon;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;

import org.apache.mesos.mini.container.AbstractContainer;

import java.security.SecureRandom;

/**
 * Test container
 */
public class MarathonContainer extends AbstractContainer {

    public static final String MESOS_CONTAINER_IMAGE = "mesosphere/marathon";
    private String mesosIp;

    public MarathonContainer(DockerClient dockerClient) {
        super(dockerClient);
    }

    @Override
    protected void pullImage() {
        pullImage(MESOS_CONTAINER_IMAGE, "latest");
    }

    public void setMesosIp(String ip){
    	mesosIp = ip;
    }
    @Override
    protected CreateContainerCmd dockerCommand() {
    	CreateContainerCmd cmd = dockerClient
        		.createContainerCmd(MESOS_CONTAINER_IMAGE).withPublishAllPorts(true)
        		.withEnv("MARATHON_MASTER=zk://"+mesosIp+":2181/mesos" , "MARATHON_ZK=zk://"+mesosIp+":2181/marathon")
        		.withName("marathon_" + new SecureRandom().nextInt());
    	System.out.println(cmd.getCmd());
    	return cmd;
    }
}
