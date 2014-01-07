package com.imgraph.tests.titan;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.kernel.Bootable;


public class TitanNode implements Bootable{

	private ActorSystem system;
	
	public TitanNode() {
				
		Config generalConfig = ConfigFactory.load().getConfig("titan");
		Config nodeConfig = ConfigFactory.parseString("hostname=\"127.0.0.1\"");
		
		Config combined = nodeConfig.withFallback(generalConfig);
		
		//system = ActorSystem.create("TitanNode", ConfigFactory.load(combined));
		system = ActorSystem.create("TitanNode", ConfigFactory.load().getConfig("titan"));

		
		/*
		ActorRef actor = system.actorOf(new Props(ManagerActor.class),
		        "managerActor");
		        */
	}
	
	
	public ActorSystem getActorSystem() {
		return system;
	}
	
	public void shutdown() {
		system.shutdown();	
	}

	public void startup() {
		system.actorOf(new Props(ManagerActor.class),
		        "managerActor");
		
	}


}
