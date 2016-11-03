package se.kth.id2209.hw1;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;

public class Common {

    public static ACLMessage newACLMessage(int performative, AID receiver, String ontology, Serializable contentObj) {
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(receiver);
        msg.setLanguage("English");
        msg.setOntology(ontology);
        try {
            msg.setContentObject(contentObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
}
