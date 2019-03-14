package game;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerImplementation implements ServerInterface {
    private List<String> players = new ArrayList<>();
    private String server_name;
    private Map<String, MUD> mud_games = new HashMap<>();

    // surely there has to be a better way than this
    private MUD current_mud;

    public String menu() {
        String msg = "\nMENU";

        msg += "\n\tCreate a new mud game (Create <gamename>)";
        msg += "\n\tJoin a mud game (Join <gamename>)";

        if(mud_games.size() <= 0) {
            msg += "\n\t\t-> No mud games exist";
        }
        else {
            for (int i = 0; i < mud_games.keySet().size(); i++) {
                msg = msg.concat("\n\t\t-> " + mud_games.keySet().toArray()[i]);
            }
        }


        return msg;
    }

    public boolean playerExists(String name) {

        return this.players.contains(name);
    }

    public boolean gameExists(String mud_name) {

        return this.mud_games.keySet().contains(mud_name);
    }

    public void playerJoin(String username) {
        this.players.add(username);
        System.out.println("\n" + username + " has joined the server");
    }

    public void playerQuit(String username) {
        this.players.remove(username);
        System.out.println("\n" + username + " has quit the server");
    }

    public String playersOnline() {

        return "These players are online: " + this.players;
    }

    public String playerStartLocation(String username, String mud_name) {
        this.setMUDGameInstance(mud_name);
        this.current_mud.addPlayer(current_mud.startLocation(), username);
        return current_mud.startLocation();
    }

    public String playerLook(String location) {

        return this.current_mud.locationInfo(location);
    }

    public String playerMove(String user_loc, String user_move, String user_name) {

        return this.current_mud.movePlayer(user_loc, user_move, user_name);
    }

    public boolean playerTake(String loc, String item) {

        return this.current_mud.takeItem(loc, item);
    }

    private void createServer(int port_registry, int port_server) throws RemoteException {
        try {
            this.server_name = (InetAddress.getLocalHost()).getCanonicalHostName();
        }
        catch(UnknownHostException e) {
            System.err.println("Error, cannot get hostname: " + e.getMessage());
        }

        System.setProperty("java.security.policy", ".policy");
        System.setSecurityManager(new SecurityManager());

        ServerInterface mud_interface = (ServerInterface)UnicastRemoteObject.exportObject(this, port_server);


        String url = "rmi://" + this.server_name + ":" + port_registry + "/mud";
        System.out.println("\nServer registered on " + url);

        try {

            Naming.rebind(url, mud_interface);
        }
        catch(MalformedURLException e) {
            System.err.println("Error, Malformed url: " + e.getMessage());
        }

        System.out.println( "\tHostname: \t\t" + this.server_name +
                            "\n\tServer port: \t" + port_server +
                            "\n\tRegistry port: \t" + port_registry +
                            "\n\nServer is running. . ."
        );

    }

    private void setMUDGameInstance(String mud_name) {

        this.current_mud = this.mud_games.get(mud_name);
    }

    public void createMUDGameInstance(String mud_name) {
        String edges = "./args/mymud.edg";
        String messages = "./args/mymud.msg";
        String things = "./args/mymud.thg";
        System.out.println("\nMUD game of the name " + mud_name + " has been created");
        MUD mud_map = new MUD(edges, messages, things);
        this.mud_games.put(mud_name, mud_map);
    }

    ServerImplementation(int port_registry, int port_server) throws RemoteException {
        createServer(port_registry, port_server);

        // create a default mud instance
        this.createMUDGameInstance("default");
    }

}
