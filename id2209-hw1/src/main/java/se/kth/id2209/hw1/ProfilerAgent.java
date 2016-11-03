package se.kth.id2209.hw1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.HashSet;
import java.util.logging.Logger;

public class ProfilerAgent extends Agent {

    public static final long UPDATE_DELAY = 5000;
    private HashSet<Artifact> artifactSet;
    private User user;
    private AID tourGuide;
    @Override
    protected void setup() {

        //searching offered tours in services


        DFAgentDescription dfd=new DFAgentDescription();
        ServiceDescription sd =new ServiceDescription();
        sd.setType("offer Tour");
        dfd.addServices(sd);
        DFAgentDescription[] result = null;
        try{
            result = DFService.search(this, dfd);
        }catch (FIPAException ex) {
            Logger.getLogger(TourGuideAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("<" + getLocalName() + ">: registered to the DF");

        System.out.println("User profiler:" + getLocalName() + ":: found " + result.length + " tour guides");
        if (result.length > 0) {
            tourGuide = result[0].getName();
            System.out.println("User profile:" + getLocalName() + ":: found " + result[0].getName());
        }


        artifactSet = new HashSet();
        user = (User) getArguments()[0];
        addBehaviour(new DelayedUpdateBehaviour(this, UPDATE_DELAY));
        print("is ready");
    }

    private void print(String s) {
        System.out.println("[" + getLocalName() + "]\t" + s);
    }

    /***
     * DelayedUpdateBehaviour
     */
    private static class DelayedUpdateBehaviour extends WakerBehaviour {

        ProfilerAgent agent;

        DelayedUpdateBehaviour(ProfilerAgent agent, long timeout) {
            super(agent, timeout);
            this.agent = agent;
        }

        @Override
        protected void onWake() {
            agent.addBehaviour(new UpdateBehaviour(agent));
        }
    } // end DelayedUpdateBehaviour

    /**
     * UpdateBehaviour
     */
    private static class UpdateBehaviour extends FSMBehaviour {

        HashSet<Integer> idSet;

        UpdateBehaviour(ProfilerAgent agent) {
            idSet = new HashSet();
            registerFirstState(new S0Behaviour(agent, this), "S0");
            registerState(new S1Behaviour(agent, this), "S1");
            registerLastState(new OneShotBehaviour() {
                @Override
                public void action() {
                    agent.print("interesting items: " + agent.artifactSet);
                }
            }, "S2");
            registerTransition("S0", "S1", 0);
            registerTransition("S0", "S2", 1);
            registerDefaultTransition("S1", "S2");
        }

        static class S0Behaviour extends SequentialBehaviour {
            ProfilerAgent agent;
            UpdateBehaviour update;
            DataStore ds;

            S0Behaviour(ProfilerAgent agent, UpdateBehaviour update) {
                this.agent = agent;
                this.update = update;
                addSubBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        ACLMessage request = Common.newACLMessage(ACLMessage.REQUEST, new AID("TourGuide", AID.ISLOCALNAME), "Personalized-tour-ontology", agent.user);
                        agent.send(request);
                        try {
                            agent.print("sent:\t\t" + request.getOntology() + "\t" + request.getPerformative() + "\tContent: " + request.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                });
                addSubBehaviour(new MsgReceiver(agent, MessageTemplate.MatchOntology("Personalized-tour-ontology"), MsgReceiver.INFINITE, ds = new DataStore(), 0));
            }

            @Override
            public int onEnd() {
                if (ds.get(0) == null) {
                    agent.print("received:\tnothing!");
                    return 1;
                }
                ACLMessage inform = (ACLMessage) ds.get(0);
                try {
                    agent.print("received:\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent: " + inform.getContentObject());
                    update.idSet = (HashSet<Integer>) inform.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        } // end S0Behaviour

        static class S1Behaviour extends SequentialBehaviour {
            ProfilerAgent agent;
            UpdateBehaviour update;
            DataStore ds;

            S1Behaviour(ProfilerAgent agent, UpdateBehaviour update) {
                this.agent = agent;
                this.update = update;
                addSubBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        ACLMessage request = Common.newACLMessage(ACLMessage.REQUEST, new AID("Curator", AID.ISLOCALNAME), "Artifact-details-ontology", update.idSet);
                        agent.send(request);
                        try {
                            agent.print("sent:\t\t" + request.getOntology() + "\t" + request.getPerformative() + "\tContent: " + request.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                });
                addSubBehaviour(new MsgReceiver(agent, MessageTemplate.MatchOntology("Artifact-details-ontology"), MsgReceiver.INFINITE, ds = new DataStore(), 1));
            }

            @Override
            public int onEnd() {
                if (ds.get(1) == null) {
                    agent.print("received:\tnothing!");
                    return 1;
                }
                ACLMessage inform = (ACLMessage) ds.get(1);
                try {
                    agent.print("received:\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent: " + inform.getContentObject());
                    agent.artifactSet = (HashSet<Artifact>) inform.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        } // end S1Behaviour
    } // end UpdateBehaviour
}