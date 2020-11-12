package fish.payara.jakartaee.tck;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        try {
            GlassFishRuntime runtime = GlassFishRuntime.bootstrap();
            GlassFishProperties glassfishProperties = new GlassFishProperties();
            glassfishProperties.setPort("http-listener", 9080);
            glassfishProperties.setPort("https-listener", 9081);
            GlassFish glassfish = runtime.newGlassFish(glassfishProperties);
            glassfish.start();
            glassfish.getCommandRunner().run("set-hazelcast-configuration", "--dynamic", "true", "--clusterMode", "multicast", "--multicastPort", "2904", "--multicastGroup", "224.2.2.4");

            System.out.println("############################################");
            System.out.println("##### Running Provided ASadmin Command #####");
            System.out.println("############################################");

            // Construct base send-asadmin-command args
            String[] sendAsadminCommandParameters = new String[args.length + 1];
            sendAsadminCommandParameters[0] = "--command";
            sendAsadminCommandParameters[1] = args[0];

            if (args.length > 1) {
                // Add command parameters
                // Note that there is currently some odd behaviour with send-asadmin-command which requires the primary
                // parameter of the command being sent to be set first before any of the other parameters (opposite to how
                // commands are normally written), so we need to add the last parameter first
                sendAsadminCommandParameters[2] = args[args.length - 1];
                for (int i = 1; i < args.length - 1; i++) {
                    sendAsadminCommandParameters[i + 2] = args[i];
                }
            }

            // Run command
            CommandResult commandResult = glassfish.getCommandRunner().run("send-asadmin-command", sendAsadminCommandParameters);

            System.out.println("##### Command Output #####");
            System.out.println(commandResult.getOutput());
            if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)) {
                System.out.println(commandResult.getFailureCause());
            }
            glassfish.stop();
        } catch (GlassFishException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
