package agents;



import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.graphstream.ui.util.CubicCurve.MyCanvas;

import env.Attribute;
import env.Environment;
import env.Environment.Couple;
import mas.abstractAgent;
import mas.DummyExplorerAgent.RandomWalkBehaviour;

public class AgentExplorer extends abstractAgent{

	protected HashMap<String , HashMap<String,Integer>> maze ;
	protected boolean stuck = false ;
	private ArrayList<AID> senders ;
	private String pos;
	private String dest;
	protected HashMap<String,String> parentNoeud ;
	protected HashMap <String, StateMaze> etatNoeud;
	protected HashMap<String,Behaviour> comportement ;



	int clock = 2000;



	protected void setup(){
		super.setup();
		maze = new HashMap<String , HashMap<String,Integer>>();
		senders = new ArrayList<AID>();
		parentNoeud = new  HashMap<String,String>();
		etatNoeud = new HashMap<String, StateMaze>();
		comportement = new HashMap<String, Behaviour>();



		//get the parameters given into the object[]
		final Object[] args = getArguments();
		if(args[0]!=null){
			realEnv = (Environment) args[0];
			realEnv.deployAgent(this.getLocalName());

		}else{
			System.out.println("Erreur lors du tranfert des parametres");
		}

		//Add the behaviours
		comportement.put("listen", new ListenerBehaviour(this));
		comportement.put("explore", new ExplorerBehaviour(this,realEnv));

		addBehaviour(comportement.get("explore"));
		addBehaviour(comportement.get("listen"));

		System.out.println("the agent "+this.getLocalName()+ " is started");


		// Ajout au pages jaune.
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID()); /* getAID est l'AID de l'agent qui veut s'enregistrer*/
		ServiceDescription sd  = new ServiceDescription();
		sd.setType( "AventurierDeLextreme" ); /* il faut donner des noms aux services qu'on propose (ici explorer)*/
		sd.setName(getName() );
		dfd.addServices(sd);

		try {  
			DFService.register(this, dfd ); 
			AMSService.deregister(this);
		}
		catch (FIPAException fe) { fe.printStackTrace(); }


	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/** Creer un graphe sans puit pour le parcours en largeur
	 * 
	 * @param depart le point de départ
	 * @return un graphe
	 */
	public HashMap<String,HashMap<String,Integer>> createGraphe(String depart ){
		HashMap<String,HashMap<String,Integer>> result = new HashMap<String,HashMap<String,Integer>>();
		for (String s : maze.keySet()){
			if (s == depart || etatNoeud.get(s) != StateMaze.Puits){
				HashMap<String,Integer> temp = new HashMap<String,Integer>();
				result.put(s, temp);
				for ( String s2 :maze.get(s).keySet()){
					if(etatNoeud.get(s2)!= StateMaze.Puits){
						result.get(s).put(s2, 0);
					}
				}
			}
		}
		return result ;
	}





	// Parcours en LARGEUR (pas encore finit)
	public ArrayList<String> PlusCourtChemin (String depart, String fin){
		System.out.println( getLocalName() +"debut="+depart  + "   fin="+ fin);
		afficherGraphe();

		/**INITIALISATION**/

		// la structure comprenant les arretes du graphe
		HashMap<String,HashMap<String,Integer>> graphe = createGraphe(depart);
		// la structure comprenant les aprents de chaque noeud ( Enfant/Parent)
		HashMap<String, String> parent = new HashMap<String, String>();
		// la stucture donnant pour chaque noeud sa valeur dans le parcour en largeur
		HashMap<String,Integer> valeurNoeud = new HashMap<String,Integer>();
		// La file , parce que dans un parcour en largeur , on a besoin d'une file
		ArrayList<String> file = new ArrayList<String>() ;
		file.add(depart);
		// initialise le noeud de depart a 0
		String courant= file.get(0) ;
		valeurNoeud.put(courant, Integer.valueOf(0));

		/**BOUCLE**/


		while (!file.isEmpty() && courant != fin  && courant != null){
			courant = file.get(0);

			file.remove(0);
			if(!graphe.get(courant).isEmpty() ){
				for (String s : graphe.get(courant).keySet()){

					if(!file.contains(s)|| !valeurNoeud.containsKey(s)){
						file.add(s);
						valeurNoeud.put(s, Integer.valueOf( valeurNoeud.get(courant).intValue() + 1));
						parent.put(s, courant);
					}
					else{
						//						System.out.println("courat= "+ courant + " existe dans valnoeud="+ valeurNoeud.containsKey(courant));
						//						System.out.println(valeurNoeud.get(courant));
						Integer valeur =Integer.valueOf( valeurNoeud.get(courant).intValue() + 1);
						if ( valeurNoeud.get(s).compareTo(valeur) > 0){
							parent.put(s, courant);
							valeurNoeud.put(s, valeur);
						}
					}
					if (s == fin){
						file.add(courant);
						parent.put(s, courant);
						courant = s ;
						break;
					}
				}
			}


		}

		/** Construction du chemin **/

		if (courant == null || courant != fin){
			System.out.println("WE HAVE A PROBLEME   " + "  courant="+depart + " fin=" +courant);
			ArrayList<String> result = new ArrayList<String>();
			result.add(depart);
			return result;
		}
		ArrayList<String> result = new ArrayList<String>();
		courant = fin;
		result.add(fin );
		while (courant != depart  && courant != null){
			courant =parent.get(courant);
			result.add(parent.get(courant));
			

		}
		System.out.println(result);
		ArrayList<String> result2 = new ArrayList<String>();
		for (int i= result.size()-1 ; i >=0 ; i--){
			result2.add(result.get(i));
		}
		return result2;

	}



	public void afficherGraphe(){
		String s ="";
		for (String s1 : maze.keySet()){
			s+= s1 +"={";
			for (String s2 : maze.get(s1).keySet()){
				s+= s2 +" ; ";
			}
			s+= "} \n";
		}
		System.out.println(s);
	}


	/*******************************************************
	 * 
	 * 
	 * 				BEHAVIOURS  1  : THE EXPLORER
	 * 
	 * 
	 *******************************************************/


	public class ExplorerBehaviour extends TickerBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private Environment realEnv;
		private String lastPosition;

		public ExplorerBehaviour (final Agent myagent,Environment realEnv) {
			// Pour changer la vitesse , c'est ICI
			super(myagent, clock);
			this.realEnv=realEnv;
			stuck = false ;

			System.out.println("EXPLORER MODE for " + myagent.getLocalName());


		}


		// Truc a faire dans le béhavior d'exploration
		@Override
		protected void onTick() {

			String myPosition=getCurrentPosition();
			if (myPosition!="" && stuck == false){
				majMap(); 
				//System.out.println("maj done");
				//System.out.println(etatNoeud);

				//System.out.println(myPosition);
				//System.out.println(realEnv.observe(myPosition, this.myAgent.getLocalName()));


				List<Attribute> lattribute= realEnv.observe(myPosition, this.myAgent.getLocalName()).get(0).getR();
				for(Attribute a:lattribute){
					switch (a) {
					case TREASURE:
						System.out.println("My current backpack capacity is:"+ getBackPackFreeSpace());
						System.out.println("Value of the treasure on the current position: "+a.getValue());
						System.out.println("The agent grabbed :"+pick());
						System.out.println("the remaining backpack capacity is: "+ getBackPackFreeSpace());

						if (Integer.parseInt(a.getValue().toString()) >0){
							String posTresore = myPosition;
							myAgent.addBehaviour(new TalkTreasurBehaviour(myAgent, realEnv, posTresore,(Integer) a.getValue()));
						}
						break;
					default:
						break;
					}
				}


				int size = realEnv.observe(getCurrentPosition(), this.myAgent.getLocalName()).get(0).getR().size();
				env.Attribute b = null;
				if (size !=0){b = realEnv.observe(getCurrentPosition(), this.myAgent.getLocalName()).get(0).getR().get(0);}
				pos = myPosition;
				dest = parcourDifferent(myPosition);
				// Pour éviter l'interblocage (prend l'avantage sur le reste des traitement)
				if (lastPosition == pos){
					dest = anywhere(pos);
				}

				myAgent.addBehaviour (new TalkBehaviour (myAgent,realEnv,pos , dest,defineEtat(b)));
			}

			if (stuck == true ){
				comportement.put("destination", new FinalDestinationBehaviour(myAgent,myPosition , findDestination(), realEnv));
				myAgent.addBehaviour(comportement.get("destination"));
				this.stop();
				myAgent.removeBehaviour(comportement.get("explore"));
			}
			else{
				lastPosition = myPosition;
				move(myPosition, dest);
			}



		}

		// donne au hasard une destination dans les noeud fils du noeud courant
		protected String anywhere(String myPosition) {
			while (true){
				for (String s : maze.get(myPosition ).keySet()){
					if (Math.random() < 1.0/ maze.get(myPosition).size() )
						return s ;
				}
			}
		}

		// Fonction condant l'algo de parcours du labyrhinte
		protected String getRoad (String myPosition){
			for (String s : maze.get(myPosition).keySet()){
				if (maze.get(myPosition).get(s) == 0){
					parentNoeud.put(s, myPosition);
					return s;
				}
			}
			//System.out.println("The agent is not on the road again");
			stuck = true ;
			return myPosition ;
		}

		// le parcours en longeur , remplace getRoad
		protected String parcourLong (String myPosition){
			for (String s : maze.get(myPosition).keySet()){
				if (  maze.get(myPosition).get(s) == 0 && etatNoeud.get(s) != StateMaze.Puits){
					if (myPosition != s /*&& parentNoeud.containsKey(s)==false*/) parentNoeud.put(s, myPosition);
					maze.get(myPosition).put(s, 1);
					maze.get(s).put(myPosition, 1);

					return s;
				}
			}
			while (parentNoeud.containsKey(myPosition)){

				//System.out.println(parentNoeud);
				return parentNoeud.get(myPosition);
			}
			System.out.println("i have no idea what i'm doing");
			//System.out.println(maze);
			//this.myAgent.doWait(1000000);
			while (true){
				for (String s : maze.get(myPosition).keySet()){
					if (Math.random() < 1.0/ maze.get(myPosition).size() )
						return s ;
				}
			}



		}

		protected String parcourDifferent (String myPosition){
			for (String s : maze.get(myPosition).keySet()){
				if (  maze.get(myPosition).get(s) == 0 && etatNoeud.get(s) != StateMaze.Puits){
					if (myPosition != s /*&& parentNoeud.containsKey(s)==false*/) parentNoeud.put(s, myPosition);
					maze.get(myPosition).put(s, 1);
					maze.get(s).put(myPosition, 1);

					return s;
				}
			}
			//System.out.println("plus court chemin en marche !");
			stuck = true ;
			return myPosition;

		}


		//Met a jour le labyrinthe connu de l'agent
		protected void majMap() {
			String myPosition=getCurrentPosition();
			// creer un état pour le noeud
			if (etatNoeud.containsKey(myPosition)== false) etatNoeud.put(dest, StateMaze.Inconnue);
			//Met a jour les chemin de la position actuelle au position ateignable et vice versa
			if (myPosition!=""){
				List<Couple<String,List<Attribute>>> lobs=observe(myPosition);
				majMap1(myPosition,lobs);
				majMap2(lobs,myPosition);

				// Met a jour les noeuds 
				System.out.println("position=" + myPosition  + " name =" + this.myAgent.getLocalName()   );
				if (realEnv.observe(myPosition, this.myAgent.getLocalName()).get(0).getR().isEmpty()){
					majEtat(myPosition, StateMaze.Rien);
				}
				for(env.Attribute a : realEnv.observe(myPosition, this.myAgent.getLocalName()).get(0).getR()){
					majEtat(myPosition, defineEtat(a));
				}

			}
		}


		// Met a jour dans le hasmap les chemins de la position courantes au prosition ateingable
		protected void majMap1(String myPosition,List<Couple<String,List<Attribute>>> lobs)
		{
			for (int i = 0 ; i < lobs.size();i++)
			{
				String dest = lobs.get(i).getL();
				// creer un état pour le noeud
				if (etatNoeud.containsKey(dest)== false) etatNoeud.put(dest, StateMaze.Inconnue);
				// si maze ne contient pas myPosition comme neoud entrant
				if (i==0 &&!maze.containsKey(myPosition))
				{
					HashMap<String,Integer> temp = new HashMap<String,Integer>();
					temp.put(dest, 0);
					maze.put(myPosition, temp);
				}
				// si l'arrete myPosition/dest n'existe pas.
				if (!maze.get(myPosition).containsKey(dest) && dest != myPosition)
				{
					maze.get(myPosition).put(dest, 0);

				}
			}
		}


		// met à jour le hashmap les chemins des position ateignable a la position courante.
		protected void majMap2(List<Couple<String,List<Attribute>>> lobs,String dest)
		{
			for (int i = 0 ; i < lobs.size();i++)
			{
				String pos =  lobs.get(i).getL(); 
				if(!maze.containsKey(pos))
				{
					HashMap<String,Integer> temp = new HashMap<String,Integer>();
					temp.put(dest, 0);
					maze.put(pos, temp);
				}
				if(!maze.get(pos).containsKey(dest) && dest != pos)
				{
					maze.get(pos).put(dest, 0);
				}
			}
		}


		// Definie dans quel état on est 
		private StateMaze defineEtat (Attribute etat){
			if (etat == env.Attribute.WIND){
				return StateMaze.Puits;
			}
			return StateMaze.Rien;

		}


		// Met a jour les états des différents noeuds
		protected void majEtat (String myPosition , StateMaze etat){
			etatNoeud.put(myPosition, StateMaze.Visiter);
			for (String s : maze.get(myPosition).keySet()){
				if(etatNoeud.get(s) != StateMaze.Rien && etatNoeud.get(s) != StateMaze.Visiter){
					etatNoeud.put(s, etat);
				}
				for (String s2 : maze.get(s).keySet() ){
					if(etatNoeud.get(s2) != StateMaze.Rien && etatNoeud.get(s2) != StateMaze.Visiter){
						etatNoeud.put(s2, etat);
					}
				}
			}
		}



		public String findDestination() {
			for( String s : etatNoeud.keySet()){
				if ( etatNoeud.get(s) != StateMaze.Visiter && s != null) return s ;
			}
			System.out.println("PAS TROUVER DE CHEMIN");
			return pos ;
		}






	}



	/*******************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 2 :  THE TALKER
	 * 
	 * 
	 ********************************************************/


	public class TalkBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private Environment realEnv;

		private StateMaze state;


		public TalkBehaviour (final Agent myagent,Environment realEnv,String pos,String dest,StateMaze state) {
			super();
			this.realEnv=realEnv;
			this.state = state ;


		}

		// Definie dans quel état on est 
		private StateMaze defineEtat (Attribute etat){
			if (etat == env.Attribute.WIND){
				return StateMaze.Puits;
			}
			return StateMaze.Rien;

		}


		@Override
		public void action() {
			//Create a message in order to send it to the choosen agent


			//récupère les adresses de ses gens
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd  = new ServiceDescription();
			sd.setType( "AventurierDeLextreme" ); /* le même nom de service que celui qu'on a déclaré*/
			dfd.addServices(sd);

			DFAgentDescription[] result;
			try {
				result = DFService.search(this.myAgent, dfd);
				AID a = new AID() ;
				for (int i=0 ; i < result.length; i ++){

					a = result[i].getName();

					if (!a.equals(this.myAgent.getAID()) ){
						final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());


						msg.addReceiver(a); // hardcoded= bad, must give it with objtab				 


						msg.setContent(pos + "@" + dest + "&" + state);
						this.myAgent.send(msg);
					}
				}

			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}




			this.finished=true;
		}



		@Override
		public boolean done() {
			myAgent.addBehaviour (new ListenerBehaviour (myAgent));
			return finished;
		}



	}



	/*********************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 3 : THE LISTENER
	 * 
	 * 
	 *********************************************************/




	public class ListenerBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to communicate with others agents in order to reach a precise decision, 
		 * it tries to form a coalition. This behaviour is the first step of the paxos
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;

		long time ;
		boolean tresoreEnVue;
		int size ;
		String destination ;


		public ListenerBehaviour(final Agent myagent) {

			super(myagent);
			tresoreEnVue = false ;
			size = Integer.MAX_VALUE ;
		}


		public void action() {
			//1) receive the message


			if (tresoreEnVue && System.currentTimeMillis()- time > clock){
				tresoreEnVue = false;
				size = Integer.MAX_VALUE ;
				System.out.println(myAgent.getLocalName() + " va chercher le trésore");
				// enlever le behavior d'exploration et mettre le behavior de recherche de tresore
				////////////////////////////////////////////////////// Stoper le explorer behavior et commence le filan destiantion behavior
				if (comportement.containsKey("destination"))
					myAgent.removeBehaviour(comportement.get("destination"));
				if (comportement.containsKey("explore"))
					myAgent.removeBehaviour(comportement.get("explore"));
				comportement.put("destination", new FinalDestinationBehaviour(myAgent, getCurrentPosition(), destination, realEnv));
				myAgent.addBehaviour(comportement.get("destination"));
			}

			final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);



			final ACLMessage msg = this.myAgent.receive(msgTemplate);

			if (msg != null ) {	
				//System.out.println(this.myAgent.getName()+" dit \"TU DIS QUOI ???\"");

				System.out.println(myAgent.getLocalName()+" a reçu "+ msg.getContent());

				AID sender = msg.getSender();
				if ( ! senders.contains(sender)){
					senders.add(sender);
				}


				// Lit le message et met a jour le maze
				String message = msg.getContent();

				if (message.contains("Treasure")){
					System.out.println(myAgent.getLocalName() + " traite le message tresore part 1");
					String[]listeMsg = message.split("@");
					calculTreasure(listeMsg[1], Integer.valueOf(listeMsg[2]));
					System.out.println(myAgent.getLocalName() + " traite le message tresore part 2");
					destination = listeMsg[1];

				}

				else if (message.contains("Capacity")){
					compareDistance(message);
				}

				else {

					String[]result= new String[3];
					String[]listeMsg = message.split("@");
					String[]listeMsg2 = listeMsg[1].split("&"); 
					result[0]=listeMsg[0]; 
					result[1]= listeMsg2[0];
					result[2]=listeMsg2[1]; 
					majMap3(result[0],result[1]);


					// Modifie les états comme si l'agent avait était lui même sur la case
					majEtat2(result[0], StateMaze.valueOf( result[2]));


					// Modifie les états en considérant comme sur la case ou a était l'agent envoyer de message
					//majEtat2V2(result[0], StateMaze.valueOf( result[2]));


					//System.out.println("<----Message received from "+msg.getSender()+" ,content= "+msg.getContent());

					this.finished=true;	
				}
			}
			else{
				//	System.out.println("pas de message");
			}

		}

		public boolean done() {
			//if (Liste.size() < 10) return false;
			return finished;
		}




		private void majMap3(String pos ,String dest){
			if(!maze.containsKey(pos))
			{
				HashMap<String,Integer> temp = new HashMap<String,Integer>();
				temp.put(dest, 1);
				maze.put(pos, temp);
			}
			if(!maze.get(pos).containsKey(dest))
			{
				maze.get(pos).put(dest, 1);
			}

			if(!maze.containsKey(dest))
			{
				HashMap<String,Integer> temp = new HashMap<String,Integer>();
				temp.put(pos, 1);
				maze.put(dest, temp);
			}
			if(!maze.get(dest).containsKey(pos))
			{
				maze.get(dest).put(pos, 1);
			}
		}

		private void majEtat2 (String pos , StateMaze etat){
			for (String s : maze.get(pos).keySet()){
				if(etatNoeud.get(s) != StateMaze.Rien && etatNoeud.get(s) != StateMaze.Visiter){
					etatNoeud.put(s, etat);
				}
				for (String s2 : maze.get(s).keySet()){
					if(etatNoeud.get(s2) != StateMaze.Rien && etatNoeud.get(s2) != StateMaze.Visiter){
						etatNoeud.put(s2, etat);
					}
				}
			}
		}

		private void compareDistance (String msg ) {
			String[]result=  msg.split("@");
			if (Integer.valueOf(result[1]) < size) tresoreEnVue = false ;
			if (Integer.valueOf(result[1]) == size && Integer.valueOf(result[2]) > getBackPackFreeSpace() )tresoreEnVue = false ;

		}

		private void  calculTreasure(String position , int value){
			if (getBackPackFreeSpace()  ==0){
				return ;
			}
			time = System.currentTimeMillis();
			tresoreEnVue = true ;
			System.out.println("test");
			//size = PlusCourtChemin(getCurrentPosition(), position).size();
			System.out.println("test1");
			myAgent.addBehaviour(new TalkCapacityTreasurBehaviour(myAgent, realEnv, 10, getBackPackFreeSpace()));
			System.out.println("test2");

		}


	}

	private void majEtat2V2 (String pos , StateMaze etat){
		if(etatNoeud.get(pos) != StateMaze.Rien){
			etatNoeud.put(pos, etat);
		}
	}



	/**********************************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 4 : THE TREASUR FINDER
	 * 
	 * 
	 **********************************************************************/





	public class FinalDestinationBehaviour extends TickerBehaviour{
		/**
		 * When an agent choose to communicate with others agents in order to reach a precise decision, 
		 * it tries to form a coalition. This behaviour is the first step of the paxos
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;


		private Environment realEnv;

		ArrayList<String> chemin ;
		int position ;

		public FinalDestinationBehaviour(final Agent myagent, String depart, String destination,Environment realEnv) {

			super(myagent, clock);
			//			this.maze = maze;
			//			this.etatNoeud = etatNoeud;
			chemin = PlusCourtChemin(depart, destination);
			this.realEnv = realEnv;
			position =0;
			System.out.println("RECHERCHE DE TRESORE !!!for " + myagent.getLocalName());
			String s = "";
			for (String s1 : chemin){
				s+= "->"+s1;
			}
			System.out.println(s);
		}


		protected void onTick() {

			String myPosition=getCurrentPosition();
			if (myPosition!="" ){
				if  ( position < chemin.size() -1)move(myPosition, chemin.get(position));
				position += 1;
				if (position >= chemin.size()-1){
					myAgent.addBehaviour(new ExplorerBehaviour(myAgent, realEnv));
					myAgent.removeBehaviour(this);
				}
			}

		}


	}


	/*******************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 5 :  THE TALKER OF TREASUR
	 * 
	 * 
	 ********************************************************/


	public class TalkTreasurBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private Environment realEnv;

		private StateMaze state;
		private int valeurT;

		public TalkTreasurBehaviour (final Agent myagent,Environment realEnv,String pos, int valeurT) {
			super();
			this.realEnv=realEnv;
			this.state = state ;
			this.valeurT = valeurT;

		}



		@Override
		public void action() {
			//Create a message in order to send it to the choosen agent


			//récupère les adresses de ses gens
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd  = new ServiceDescription();
			sd.setType( "AventurierDeLextreme" ); /* le même nom de service que celui qu'on a déclaré*/
			dfd.addServices(sd);

			DFAgentDescription[] result;
			try {
				result = DFService.search(this.myAgent, dfd);
				AID a = new AID() ;
				for (int i=0 ; i < result.length; i ++){

					a = result[i].getName();

					if (!a.equals(this.myAgent.getAID()) ){
						final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());


						msg.addReceiver(a); // hardcoded= bad, must give it with objtab				 


						msg.setContent("Treasure@"+ pos+"@"+valeurT );
						this.myAgent.send(msg);
					}
				}

			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}




			this.finished=true;
		}



		@Override
		public boolean done() {
			myAgent.addBehaviour (new ListenerBehaviour (myAgent));
			return finished;
		}



	}

	/*******************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 6 :  THE TALKER OF CAPACITY
	 * 
	 * 
	 ********************************************************/


	public class TalkCapacityTreasurBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private Environment realEnv;

		private StateMaze state;
		private int valeurT;
		int size;

		public TalkCapacityTreasurBehaviour (final Agent myagent,Environment realEnv,int size, int valeurT) {
			super();
			this.realEnv=realEnv;
			this.state = state ;
			this.valeurT = valeurT;
			this.size = size;
		}



		@Override
		public void action() {
			//Create a message in order to send it to the choosen agent


			//récupère les adresses de ses gens
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd  = new ServiceDescription();
			sd.setType( "AventurierDeLextreme" ); /* le même nom de service que celui qu'on a déclaré*/
			dfd.addServices(sd);

			DFAgentDescription[] result;
			try {
				result = DFService.search(this.myAgent, dfd);
				AID a = new AID() ;
				for (int i=0 ; i < result.length; i ++){

					a = result[i].getName();

					if (!a.equals(this.myAgent.getAID()) ){
						final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());


						msg.addReceiver(a); // hardcoded= bad, must give it with objtab				 

						System.out.println("Capacity@"+ size+"@"+valeurT );
						msg.setContent("Capacity@"+ size+"@"+valeurT );
						this.myAgent.send(msg);
					}
				}

			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}




			this.finished=true;
		}



		@Override
		public boolean done() {
			myAgent.addBehaviour (new ListenerBehaviour (myAgent));
			return finished;
		}
	}

}





