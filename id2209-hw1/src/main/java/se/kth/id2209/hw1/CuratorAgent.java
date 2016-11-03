package se.kth.id2209.hw1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class CuratorAgent extends Agent {

    public static final long UPDATE_PERIOD = 60000;
    private Hashtable<Integer, Artifact> artifactTable;

    @Override
    protected void setup() {

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd1 = new ServiceDescription();
        ServiceDescription sd2 = new ServiceDescription();
        sd1.setType("offer artifact details");
        sd1.setName(getLocalName());
        sd2.setType("offer catalog");
        sd2.setName(getLocalName());
        dfd.addServices(sd1);
        dfd.addServices(sd2);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("<" + getLocalName() + ">: registered to the DF");

        addBehaviour(new ParallelServerBehaviour(this, 10));
        print("is ready!");
        artifactTable = new Hashtable();
        addBehaviour(new RepeatedUpdateBehaviour(this, UPDATE_PERIOD));

    }

    private void print(String s) {
        System.out.println("[" + getLocalName() + "]\t" + s);
    }

    /**
     * RepeatedUpdatedBehaviour
     */
    private static class RepeatedUpdateBehaviour extends TickerBehaviour {

        CuratorAgent agent;

        RepeatedUpdateBehaviour(CuratorAgent agent, long period) {
            super(agent, period);
            this.agent = agent;
            agent.addBehaviour(new UpdateBehaviour(agent));
        }

        @Override
        protected void onTick() {
            agent.addBehaviour(new UpdateBehaviour(agent));
        }
    } // end RepeatedUpdatedBehaviour

    /***
     * UpdateBehaviour
     */
    private static class UpdateBehaviour extends OneShotBehaviour {

        CuratorAgent agent;

        UpdateBehaviour(CuratorAgent agent) {
            this.agent = agent;
        }

        @Override
        public void action() {
            // update available artifacts
            agent.artifactTable = Main.getArtifactDB();
            agent.print("artifacts updated: " + agent.artifactTable.toString());
            // send updated genres to tour guide
            try {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("TourGuide", AID.ISLOCALNAME));
                msg.setLanguage("English");
                msg.setOntology("Artifact-catalogue-ontology");
                msg.setContentObject(getGenres());
                agent.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HashSet<String> getGenres() {
            HashSet<String> genres = new HashSet();
            Iterator it = agent.artifactTable.values().iterator();
            while (it.hasNext()) {
                genres.add(((Artifact) it.next()).getGenre());
            }
            return genres;
        }
    } // end UpdateBehaviour

    /**
     * ParallelServerBehaviour
     */
    private static class ParallelServerBehaviour extends ParallelBehaviour {

        ParallelServerBehaviour(CuratorAgent agent, int num) {
            for (int i = 0; i < num; i++) {
                addSubBehaviour(new ServerBehaviour(agent));
            }
        }
    } // end ParallelServerBehaviour

    /**
     * ServerBehaviour
     */
    private static class ServerBehaviour extends CyclicBehaviour {

        CuratorAgent agent;

        ServerBehaviour(CuratorAgent agent) {
            this.agent = agent;
        }

        @Override
        public void action() {
            ACLMessage msg = agent.receive();
            if (msg != null) {
                agent.print("received:\t" + msg.getOntology() + "\t" + msg.getPerformative() + "\tSender: " + msg.getSender().getLocalName());
                agent.addBehaviour(new HandleMessageBehaviour(agent, msg));
            }
        }

        static class HandleMessageBehaviour extends OneShotBehaviour {

            CuratorAgent agent;
            ACLMessage msg;

            HandleMessageBehaviour(CuratorAgent agent, ACLMessage msg) {
                this.agent = agent;
                this.msg = msg;
            }

            @Override
            public void action() {
                switch (msg.getOntology()) {
                    case "Personalized-tour-ontology":
                        try {
                            HashSet<Integer> personalizedTour = getPersonalizedTour((HashSet<String>) msg.getContentObject());
                            ACLMessage inform = Common.newACLMessage(ACLMessage.INFORM, new AID("TourGuide", AID.ISLOCALNAME), "Personalized-tour-ontology", personalizedTour);
                            inform.addReplyTo((AID) msg.getAllReplyTo().next());
                            agent.send(inform);
                            agent.print("sent:\t\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent: " + inform.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Artifact-details-ontology":
                        try {
                            HashSet<Artifact> artifactDetails = getArtifactDetails((HashSet<Integer>) msg.getContentObject());
                            ACLMessage inform = Common.newACLMessage(ACLMessage.INFORM, msg.getSender(), "Artifact-details-ontology", artifactDetails);
                            agent.send(inform);
                            agent.print("sent:\t\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent: " + inform.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }

            HashSet<Integer> getPersonalizedTour(HashSet<String> matches) {
                HashSet<Integer> idSet = new HashSet();
                Iterator it = agent.artifactTable.values().iterator();
                while (it.hasNext()) {
                    Artifact artifact = (Artifact) it.next();
                    if (matches.contains(artifact.getGenre())) idSet.add(artifact.getId());
                }
                return idSet;
            }

            HashSet<Artifact> getArtifactDetails(HashSet<Integer> idSet) {
                HashSet<Artifact> artifactSet = new HashSet();
                Iterator it = idSet.iterator();
                while (it.hasNext()) {
                    Integer id = (Integer) it.next();
                    artifactSet.add(agent.artifactTable.get(id));
                }
                return artifactSet;
            }
        }
    } // end ServerBehaviour
}
