import java.io.IOException;
import java.sql.SQLOutput;
import java.util.Scanner;

import java.util.Set;
import java.util.TreeMap;

/*
Date: May 17
Author: Harry Beesley-Gilman
Purpose: to build a functional "Bacon" game
*/

public class BaconGame {
    private GraphLib gamey;


    public BaconGame(String movieFile, String actorFile, String movieActorFile) throws IOException {
        gamey = new GraphLib(movieFile, actorFile, movieActorFile); /*makes Graphlib object with our files*/
        gamey.createGraph(); /*makes connection graph for that graphlib object*/
    }

    public void runGame() throws Exception {
        System.out.println("Here are the commands for our game! Start by building a shortest path tree \n"
        + "Press s to input an actor and generate their shortest path tree \n"
        + "Press p to input an actor and construct a path back to the root of our path tree \n"
        + "Press e to find actors that aren't included in our path tree. \n"
        + "Press a to input an actor and find the average distance-from-root of all actors in their tree.\n"
        + "Press q to quit game \n" + "Press b to find the best center of the universe.");
        Scanner in = new Scanner(System.in);
        String input = "";
        boolean tempGraphSetYet = false; /*says whether our tree has been generated around a center yet. Necessary
        for other methods to work*/
        String parent = null; /*can be good to store the last parent (actor) we've put in*/
        AdjacencyMapGraph tempGraph = new AdjacencyMapGraph<>();
        while(!input.equals("q")) { //keep cycling until the user asks to quit
            input = in.next();

            if (input.equals("s")) { /*code to generate a shortest path treee around an actor*/
                System.out.println("Let's generate a shortest path tree. Who should be the center of your universe?");
                parent = (in.nextLine());
                parent = (in.nextLine()); /*update parent*/

                while (!this.gamey.getActorMap().containsValue(parent)) { /*until we have an input that the system recognizes*/
                    System.out.println("Hmm. We can't find them. Try again or press q to quit.");
                    parent = (in.nextLine());
                    if (parent.contentEquals("q")) {
                        input = "q";
                        break;
                    } /*allow user who can't think of a valid actor to quit by break out of both while loops*/
                }

                if (input.contentEquals("q")) {
                    break;
                }


                tempGraph = gamey.bfs(parent); /*run the BFS around a recognized actor*/
                tempGraphSetYet = true; /*allows other methods to work*/
                System.out.println("BFS tree generated around " + parent + ". What now?");
            }

            if (tempGraphSetYet == false && (input.equals("p") || input.equals("p") || input.equals("e") || input.equals("a"))) {
                System.out.println("Cannot perform operation because we have not built our tree yet.");
                /*if graph has not been built yet, other methods will not work*/
            }

            else {
                if (input.equals("p")) {
                    System.out.println("Enter an actor to find our path back to the origin.");
                    input = in.nextLine();
                    input = in.nextLine(); /*take entered actor, generate path with getPath method, print output*/
                    SinglyLinked path = gamey.getPath(tempGraph, input);
                    if (path != null) {
                        System.out.println("Our path is " + gamey.getPath(tempGraph, input));
                    }
                    System.out.println("What now");
                }

                if (input.equals("e")) { /*run missingVertices method on overall graph of all actors and connection
                 graph for that particular vertex*/
                    AdjacencyMapGraph x = gamey.getConnectionGraph();
                    Set missingVerts = GraphLib.missingVertices(x, tempGraph);
                    if (missingVerts.isEmpty()) {
                        System.out.println("No excluded actors. Moving on...");
                    } else {
                        System.out.println("We have " + missingVerts.size() + " actor(s) missing. Listing now: " + missingVerts);
                        System.out.println("What now?");
                    }
                }

                if (input.equals("a")) {  /*run averageSeperation recursive method from GraphLib class on that parent
                actor*/
                    System.out.println("What actor would you like to calculate average seperation for?");
                    parent = in.nextLine();
                    parent = in.nextLine();
                    if (gamey.getActorMap().containsValue(parent)) {
                        System.out.println("For all the actors connected to " + parent + ", the average separation is " +
                                gamey.averageSeparation(parent));
                        System.out.println("What now?");
                    } else {
                        System.out.println("Not a valid parent");
                    }
                }

                if (input.equals("b")) {
                    System.out.println("Finding best universe center");
                    gamey.bestCenter();
                    System.out.println("What now?");

                }
            }
        }
        System.out.println("Game over!");
    }

        public static void main (String [] args) throws Exception {
        BaconGame myGame = new BaconGame("inputs/movies.txt", "inputs/actors.txt", "inputs/movie-actors.txt");
        myGame.runGame();
        }

}
