package se.kth.id2209.hw1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TourGuideAgent extends Agent {

    private HashSet<String> genres;

    @Override
    protected void setup() {



        /*searching for catlogue(contains museum information using search method
             *
         */
        DFAgentDescription dfd=new DFAgentDescription();
        ServiceDescription sd =new ServiceDescription();
        sd.setType("offer Catalogue");

        dfd.addServices(sd);

        DFAgentDescription[] result = null;
        //service searching from DfService offercatalogue

        try {
            result = DFService.search(this, dfd);
        } catch (FIPAException ex) {
            Logger.getLogger(TourGuideAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("<" + getLocalName() + ">: found " + result.length + " museums");

        /*
              DF Subscription Services gets continuously updated,if any change happens in the curator.
               immediately  tourguide agent get the notification from DF Service.
        */

        dfd = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType("offer catalog");
        dfd.addServices(sd);
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults(new Long(1));
        send(DFService.createSubscriptionMessage(this, getDefaultDF(), dfd, sc));


        /*
              registering  for offer tour services
        */
        dfd = new DFAgentDescription();
        ServiceDescription sd1 =new ServiceDescription();
        sd1.setType("offer tour ");
        sd1.setName(getLocalName());
        dfd.addServices(sd1);
        try {
            DFService.register(this, dfd );
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }


        /* its recieve  generate tour request from Profiler agent for user based on user intersts
           tour guide agent palnning virtual tour and gave reply.
          */
        genres = new HashSet();
        addBehaviour(new ParallelServerBehaviour(this, 10));
        print("is ready");
    }

    private void print(String s) {
        System.out.println("[" + getLocalName() + "]\t" + s);
    }

    /**
     * ParallelServerBehaviour
     */
    private static class ParallelServerBehaviour extends ParallelBehaviour {

        ParallelServerBehaviour(TourGuideAgent agent, int num) {
            for (int i = 0; i < num; i++) {
                addSubBehaviour(new ServerBehaviour(agent));
            }
        }
    } // end ParallelServerBehaviour

    /**
     * ServerBehaviour
     */
    private static class ServerBehaviour extends CyclicBehaviour {

        TourGuideAgent agent;

        ServerBehaviour(TourGuideAgent agent) {
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

            TourGuideAgent agent;
            ACLMessage msg;

            HandleMessageBehaviour(TourGuideAgent agent, ACLMessage msg) {
                this.agent = agent;
                this.msg = msg;
            }

            @Override
            public void action() {
                switch (msg.getOntology()) {
                    case "Artifact-catalogue-ontology":
                        try {
                            agent.genres = (HashSet) msg.getContentObject();
                            agent.print("genres updated: " + agent.genres.toString());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Personalized-tour-ontology":
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            try {
                                HashSet<String> matches = getMatches((User) msg.getContentObject());
                                ACLMessage request = Common.newACLMessage(ACLMessage.REQUEST, new AID("Curator", AID.ISLOCALNAME), "Personalized-tour-ontology", matches);
                                request.addReplyTo(msg.getSender());
                                agent.send(request);
                                agent.print("sent:\t\t" + request.getOntology() + "\t" + request.getPerformative() + "\tContent: " + request.getContentObject());
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        } else if (msg.getPerformative() == ACLMessage.INFORM) {
                            try {
                                ACLMessage inform = Common.newACLMessage(ACLMessage.INFORM, (AID) msg.getAllReplyTo().next(), "Personalized-tour-ontology", msg.getContentObject());
                                agent.send(inform);
                                agent.print("sent:\t\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent: " + inform.getContentObject());
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            HashSet<String> getMatches(User user) {
                HashSet<String> matches = new HashSet();
                Iterator it = user.getInterests().iterator();
                while (it.hasNext()) {
                    String interest = (String) it.next();
                    if (agent.genres.contains(interest)) matches.add(interest);
                }
                return matches;
            }
        }
    } // end ServerBehaviour
}