package se.kth.id2209.hw1;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class Main {

    private static final String PKG = "se.kth.id2209.hw1";
    private static Hashtable<Integer, Artifact> artifactDB;

    public static void main(String[] args) throws StaleProxyException, UnknownHostException {

        // Init database
        artifactDB = new Hashtable();
        for (int i = 0; i < 5; i++) artifactDB.put(i, new Artifact(i));

        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        int port = 1099;

        // Get JADE runtime
        Runtime rt = Runtime.instance();
        rt.setCloseVM(true);

        // Create main container
        Profile mProfile = new ProfileImpl(ipAddress, port, null);
        rt.createMainContainer(mProfile).createNewAgent("rma", "jade.tools.rma.rma", new Object[0]).start();

        // Create agent containers
        Profile aProfile = new ProfileImpl(ipAddress, port, null);
        AgentContainer c1 = rt.createAgentContainer(aProfile);
        c1.createNewAgent("Curator", PKG + ".CuratorAgent", new Object[0]).start();
        c1.createNewAgent("TourGuide", PKG + ".TourGuideAgent", new Object[0]).start();
        AgentContainer c2 = rt.createAgentContainer(new ProfileImpl());
        for (int i = 0; i < 1; i++) {
            c2.createNewAgent("Profiler" + i, PKG + ".ProfilerAgent", new Object[]{new User()}).start();
        }
    }

    public static Hashtable<Integer, Artifact> getArtifactDB() {
        return artifactDB;
    }
}
