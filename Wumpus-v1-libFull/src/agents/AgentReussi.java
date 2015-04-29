package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import chemin.Dijkstra;
import chemin.Graphe;
import agents.AgentExplorer.ExplorerBehaviour;
import agents.AgentExplorer.FinalDestinationBehaviour;
import agents.AgentExplorer.ListenerBehaviour;
import agents.AgentExplorer.TalkBehaviour;
import agents.AgentExplorer.TalkCapacityTreasurBehaviour;
import agents.AgentExplorer.TalkTreasurBehaviour;
import env.Attribute;
import env.Environment;
import env.Environment.Couple;
import mas.abstractAgent;

public class AgentReussi extends abstractAgent{
	
	protected void setup(){
		super.setup();
		
		

		//QueryAgentsOnLocation queryLocation = new QueryAgentsOnLocation();
		////
		String port = "8888";
		String cied = "PPTI-14-407-11";
		String ciedName = " Hell";
		String prof = "ppti-14-407-14";
		String profName = "prof";
		String myName = "le mec a coté de la porte";
		
		ContainerID c = new ContainerID();
		c.setName("container-1");
		c.setAddress(prof);
		c.setPort(port);
		
		
//			queryLocation.setLocation((Location) this.getContainerController().getPlatformController());
		this.doMove(c);
		
		
	}

	
}
