package org.apache.mesos.mini;

import static com.jayway.awaitility.Awaitility.await;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import mesosphere.marathon.client.model.v2.App;

import org.apache.mesos.mini.marathon.MarathonContainer;
import org.apache.mesos.mini.mesos.MesosClusterConfig;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class MesosClusterTest {

	private static final MesosClusterConfig config = MesosClusterConfig.builder()
			.numberOfSlaves(3)
			.privateRegistryPort(16000) // Currently you have to choose an available port by yourself
			.proxyPort(8777)
			.slaveResources(new String[]{"ports(*):[9200-9200,9300-9300]", "ports(*):[9201-9201,9301-9301]", "ports(*):[9202-9202,9302-9302]"})
			.build();

	@ClassRule
	public static MesosCluster cluster = new MesosCluster(config);

	@Test
	public void mesosClusterCanBeStarted() throws Exception {
		JSONObject stateInfo = cluster.getStateInfoJSON();

		Assert.assertEquals(3, stateInfo.getInt("activated_slaves"));
	}

	@Test
	public void mesosClusterCanBeStarted2() throws Exception {
		JSONObject stateInfo = cluster.getStateInfoJSON();
		Assert.assertEquals(3, stateInfo.getInt("activated_slaves"));


		String mesosMasterUrl = cluster.getMesosContainer().getMesosMasterURL();
		Assert.assertTrue(mesosMasterUrl.contains(":5050"));
	}

	@Test
	public void startMarathon(){
		MarathonContainer marathonContainer = new MarathonContainer(config.dockerClient);
		marathonContainer.setMesosIp(cluster.getMesosContainer().getIpAddress());
		cluster.addAndStartContainer(marathonContainer);
		System.out.println("Marathon ip "+marathonContainer.getIpAddress());
		final Marathon marathon = MarathonClient.getInstance("http://"+marathonContainer.getIpAddress()+":8080");
		await().atMost(20, TimeUnit.SECONDS).pollDelay(1, TimeUnit.SECONDS).until(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try{
					marathon.getServerInfo();
				}catch(Throwable e){
					return false;
				}
				return true;
			}
		});
		startDockerContainers(marathon);
	}

	private void startDockerContainers(Marathon marathon)  {
		try{
			App app = new App();
			app.setId("containerapp");
			app.setCmd("sudo docker run -P " + HelloWorldContainer.HELLO_WORLD_IMAGE);
			app.setCpus(1.0);
			app.setMem(16.0);
			app.setInstances(5);
			marathon.createApp(app);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
