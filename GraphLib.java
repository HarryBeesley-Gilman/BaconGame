

import com.sun.jdi.Value;
import net.datastructures.Vertex;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/*
Date: May 17
Author: Harry Beesley-Gilman
Purpose: Build graphLib methods for later implementation in Bacon Game
*/
/**
 * Library for graph analysis
 * @author Harry Beesley-Gilman on scaffold code from below, May 2022
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2016
 * @purpose fill out methods to do a randomWalk and sort by inDegree
 */
public class GraphLib {

	private TreeMap actorMap; /*Map that connects Actor ID's to Actor Names*/
	private TreeMap movieMap; /*Map that connects Movie ID's to Movie Names*/
	private TreeMap actorMovieMap; /*Map that connects movies (key) to the actors in them (values)*/
	private AdjacencyMapGraph connectionGraph; /*Graph containing every actor as a vertex. Actors are connected to
	co-stars by edges representing the movies they worked in.*/


	/*GraphLib objects takes files as parameter and calls static functions to build graphs from the files upon conception*/
	/*connectionGraph is more complicated to build, so we'll do it separately (not immediately upon creation of the object*/
	public <V, E> GraphLib(String movieFile, String actorFile, String movieActorFile) throws IOException {
		connectionGraph = new AdjacencyMapGraph<V, E>();
		movieMap = movieNameGraph(movieFile);
		actorMap = actorIDGraph(actorFile);
		actorMovieMap = movieActorsGraph(movieActorFile);

	}

	public TreeMap getActorMap() {
		return actorMap;
	}

	public TreeMap getMovieMap() {
		return movieMap;
	}

	public TreeMap getActorMovieMap() {
		return actorMovieMap;
	}

	public AdjacencyMapGraph getConnectionGraph() {
		if (connectionGraph.numVertices() > 0) {
			return connectionGraph;
		} else {
			System.out.println("No connection graph yet. Operation will not work"); /*in case connection graph has not
			been set up yet, this will be triggerd*/
			return null;
		}
	}


	public <V, E> SinglyLinked<V> getPath(AdjacencyMapGraph<V, E> pathTree, V v) throws Exception {
		SinglyLinked<V> pathy = new SinglyLinked<>();/*create new SLL to represent our path*/
		pathy.add(0, v); /*add start vertex*/
		if (!pathTree.hasVertex(v)) { /*if the start vertex isn't in path tree, this won't work. Just return null*/
			System.out.println("Sorry. I can't find the connection you're looking for");
			return null;
		} else {
			while (pathTree.outNeighbors(v).toString() != "[]") { /*while there are still more outNeighbors (meaning
			we can continue traversing upwards towards to root of our tree)*/
				for (V i : pathTree.outNeighbors(v)) { /*add neighbors (should just be one) to our path*/
					pathy.add(pathy.size(), i);
					v = i; /*update the vertex we're looking at*/
				}
			}
		}
		return pathy;

	}


	public <V, E> void createGraph() {
		for (Object h : actorMap.values()) { /*create vertices for each actor*/
			connectionGraph.insertVertex(h);
		}
		if (actorMap.size() > 1) {
			for (Object h2 : actorMovieMap.keySet()) { /*run through each movie*/
				for (Object curr : (ArrayList) actorMovieMap.get(h2)) { /*loop through every combo of actors in each movie */
					for (Object curr2 : (ArrayList) actorMovieMap.get(h2)) { /*if we don't have an appropriate edge between them
				already, insert it*/
						if (curr.toString() != curr2.toString() & !connectionGraph.hasEdge(actorMap.get(curr),
								actorMap.get(curr2))) {
							TreeSet label = new TreeSet<String>();
							label.add((String) movieMap.get(h2));
							connectionGraph.insertUndirected(actorMap.get(curr), actorMap.get(curr2),
									label);
						} /*otherwise, add the movie to our edge label and reinsert it*/
						/*else if we aren't dealing with the same actor twice and there is already a connection edge between
						 * our two actors, we must add the new movie to the edge and reinsert*/
						else if (curr.toString() != curr2.toString() && connectionGraph.hasEdge(actorMap.get(curr),
								actorMap.get(curr2)) & !((TreeSet) connectionGraph.getLabel(actorMap.get(curr),
								actorMap.get(curr2))).contains(((String) movieMap.get(h2)))) {
							TreeSet x = (TreeSet) connectionGraph.getLabel(actorMap.get(curr), actorMap.get(curr2));
							x.add(movieMap.get(h2));
						}
					}
				}
			}
		}
	}

	public <V> AdjacencyMapGraph bfs(V origin) throws Exception { /*this method creates a "tree" (representing a bfs) in
	the form of an AMG*/
		AdjacencyMapGraph pathTree = new AdjacencyMapGraph();  /*create our AMG*/
		pathTree.insertVertex(origin); /*make a vertex for origin*/
		SLLQueue queuey = new SLLQueue(); /*queue for neighbors that need to be investigated*/
		for (Object c : connectionGraph.outNeighbors(origin)) { /*put all outNeighbors of the origin in our tree*/
			if (!pathTree.hasVertex(c)) {
				pathTree.insertVertex(c);
				queuey.enqueue(c);/*add them to our queue*/
				if (connectionGraph.getLabel(origin, c) != null) { /*take their movie label and put it in our graph*/
					pathTree.insertDirected(c, origin, connectionGraph.getLabel(origin, c));
				}
			}
		}
		while (!queuey.isEmpty()) { /*until there are no remaining outNeighbors to check*/
			Object currentVertex = queuey.dequeue(); /*take next in queue*/
			for (Object d : connectionGraph.outNeighbors(currentVertex)) {
				if (!pathTree.hasVertex(d)) { /*take this vertex's neighbors, make vertices for them, connect edges,enqueue*/
					pathTree.insertVertex(d);
					queuey.enqueue(d);
					if (connectionGraph.getLabel(currentVertex, d) != null) {
						pathTree.insertDirected(d, currentVertex, connectionGraph.getLabel(currentVertex, d));
					}
				}
			}
		}
		return pathTree;
	}


	public static TreeMap movieNameGraph(String movieFile) throws IOException { /*runs through movie list file and makes
	a map connecting numbers to film names*/
		BufferedReader in = new BufferedReader(new FileReader(movieFile));
		TreeMap<String, String> movieNameGraph = new TreeMap<>();
		String x = in.readLine();
		while (x != null) {
			String[] x2 = x.split("\\|");
			movieNameGraph.put(x2[0], x2[1]);
			x = in.readLine();

		}
		in.close();
		return movieNameGraph;
	}

	public static TreeMap actorIDGraph(String actorFile) throws IOException { /*runs through actor list files and makes
	map connected actor numbers to names*/
		BufferedReader in = new BufferedReader(new FileReader(actorFile));
		TreeMap<String, String> actorIDGraph = new TreeMap<>();
		String x = in.readLine();
		while (x != null) {
			String[] x2 = x.split("\\|");
			actorIDGraph.put(x2[0], x2[1]);
			x = in.readLine();
		}
		in.close();
		return actorIDGraph;
	}

	public static <V> TreeMap movieActorsGraph(String movieActorsFile) throws IOException {
		/*runs through movie actors files and makes a map connecting movies to an ArrayList featuring all of the actors
		 * who worked in them.*/
		BufferedReader in = new BufferedReader(new FileReader(movieActorsFile));
		TreeMap<String, ArrayList> movieActorsGraph = new TreeMap<>();
		String x = in.readLine();
		while (x != null) {
			String[] x2 = x.split("\\|");
			if (movieActorsGraph.containsKey(x2[0])) {
				ArrayList tempList = new ArrayList();
				for (Object v : movieActorsGraph.get(x2[0])) {
					tempList.add(v);
				}
				tempList.add(x2[1]);
				movieActorsGraph.put(x2[0], tempList);
			} else {
				ArrayList otherTempList = new ArrayList();
				otherTempList.add(x2[1]);
				movieActorsGraph.put(x2[0], otherTempList);
			}
			x = in.readLine();

		}
		in.close();
		return movieActorsGraph;
	}

	public static <V, E> Set<V> missingVertices(Graph<V, E> graph, Graph<V, E> subgraph) {
		Set setty = new TreeSet();
		for (V c : graph.vertices()) {
			setty.add(c);
		}
		for (V d : subgraph.vertices()) {
			setty.remove(d);
		}
		return setty;
	}

	public <V, E> double averageSeparation(V root) throws Exception {
		Graph tree = this.bfs((String) root);
		double vertices = tree.numVertices() - 1; /*root not included*/
		if (tree.numVertices()<2) {return 0;}/*don't have multiple vertices*/
		double counter = 0;
		counter = averageSeperationHelper(tree, root, 1);
		/*steps accumulates each time this function is called recursively before being divided by the number of vertices
		being added to counter and divided by the number of vertices*/
		return counter / vertices;
	}

	/*this other version of the function has the bfs AdjacencyMapGraph already computed. It saves time in my best
	* center of the universe calculation because I don't have to call bfs twice for each actor.*/
	public <V, E> double averageSeparation(V root, AdjacencyMapGraph t) throws Exception {
		Graph tree = t;
		double vertices = tree.numVertices() - 1; /*root not included*/
		if (tree.numVertices()<2) {return 0;}/*don't have multiple vertices*/
		double counter = 0;
		counter = averageSeperationHelper(tree, root, 1);
		/*steps accumulates each time this function is called recursively before being divided by the number of vertices
		being added to counter and divided by the number of vertices*/
		return counter / vertices;
	}
	private static <V, E> int averageSeperationHelper(Graph<V, E> tree, V root, int steps) {
		int counter = 0;
		for (V vertex : tree.inNeighbors(root)) { /*call on all neighbors further "down" (out) the tree, adding one to steps*/
			/*recurisvely calls on their children too.*/
			counter += steps;
			counter += averageSeperationHelper(tree, vertex, steps + 1);
		}
		return counter;
	}

	public void bestCenter() throws Exception {
		String bestActor = null;
		double lowestAvg = 1010101001;
		for (Object o: connectionGraph.vertices()) { /*run through all actors in connection graph*/
			AdjacencyMapGraph g = bfs(o);
			if (g.numVertices()> (connectionGraph.numVertices()/2)) { /*if that vertex is connected to at least half of
			actors*/
				String tempActor = (String) o;
				double tempAvg = averageSeparation(o, g); /*if its average separation is the best yet*/
				if (tempAvg < lowestAvg) {
					lowestAvg = tempAvg; /*set a new best value and actor*/
					bestActor = tempActor;
				}
			}
		}
		if (lowestAvg==1010101001) {
			System.out.println("No actor was connected to at least half of our vertices with an average seperation" +
					"of less than 1010101001. Sorry.");
		}
		else {
			System.out.println("Best actor that is connected to at least half of our other actors is " + bestActor +
					" with average separation of " + lowestAvg);
		}
	}
	public static void main (String [] args){
		AdjacencyMapGraph x = new AdjacencyMapGraph<Integer, Integer>();
		x.insertVertex(1);
		x.insertVertex(2);
		x.insertVertex(3);
		x.insertVertex(4);
		x.insertVertex(5);
		x.insertDirected(1,2,"a");
		x.insertDirected(1,3,"b");
		x.insertDirected(1,4,"c");
		x.insertDirected(1,5,"d");
		x.insertDirected(2,3,"e");
		x.insertDirected(2,5,"f");
		x.insertDirected(3,4,"g");
		x.insertDirected(4,5,"h");
		AdjacencyMapGraph littlex = new AdjacencyMapGraph<Integer, Integer>();
		littlex.insertVertex(1);
		littlex.insertVertex(2);
		System.out.println(missingVertices(x, littlex));
	}

}
