package AStar;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import ProblemData.Location;
import ProblemData.Connection;

public class AStar {
	private static HashMap<String, Distance> calculated_distances = new HashMap<String, Distance>();
	
	public static AStarRoute hamiltonianPathAStar(ArrayList<Location> nodes, Location start, String opt) {
		int fuelPerKm = UserInterface.UserInterface.deliveryInfo.getTruck().getFuelPerKm();
		int fuelAvailable = UserInterface.UserInterface.deliveryInfo.getTruck().getFuel();
		int truckLoad = UserInterface.UserInterface.deliveryInfo.getTruck().getLoad();
		
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).setAStarVars(0, 0, 0, null);
		}
		
		PriorityQueue<AStarRoute> open = new PriorityQueue<AStarRoute>();
		ArrayList<AStarRoute> closed = new ArrayList<AStarRoute>();
		
		AStarRoute startRoute = new AStarRoute(start);
		open.add(startRoute);
		
		AStarRoute bestRoute = new AStarRoute(start);
		double fuelUsed = 0;
		double loadUsed = 0;
		int routeExplored = 0;
		
		while (open.size() != 0) {
			AStarRoute q = open.poll();
			routeExplored++;
			
			if (q.getFuel() > fuelAvailable || q.getLoad() > truckLoad) {				
				System.out.println("Fuel used: " + fuelUsed);
				System.out.println("Load used: " + loadUsed);
				System.out.println("Routes explored: " + routeExplored);
				return bestRoute;
			}
			
			for (Location newNode : nodes) {
				if (q.contains(newNode) && !newNode.equals(start)) {
					continue;
				}
				
				AStarRoute successor = new AStarRoute(q);
				successor.addLocation(newNode);
				
				successor.setFuel(successor.getDistance()*fuelPerKm);
				successor.setLoad();
				
				double g=0, h=0;
				
				if (opt.equals("number")) {
					g = successor.getDistance();
					h = hamiltonianPathHeuristic(successor, nodes, start);
				}
				else if (opt.equals("enhanced")) {
					g = successor.getDistance()/successor.getRoute().size();
					h = hamiltonianPathHeuristic(successor, nodes, start)/(nodes.size()-successor.getRoute().size());
				}
				else if (opt.equals("value")) {
					g = successor.getDistance()/successor.getRoute().size()/successor.getValue();
					h = (hamiltonianPathHeuristic(successor, nodes, start)/(nodes.size()-successor.getRoute().size()));
				}
				
				double f = g + h;
				
				if (newNode.equals(start) && successor.getFuel() <= fuelAvailable && successor.getLoad() <= truckLoad) {
					if (successor.getRoute().size() == nodes.size()+1) {
						System.out.println("Fuel used: " + successor.getFuel());
						System.out.println("Load used: " + successor.getLoad());
						System.out.println("Routes explored: " + routeExplored);
						return successor;
					}
					else if (successor.getRoute().size() > bestRoute.getRoute().size() || (successor.getRoute().size() == bestRoute.getRoute().size() && successor.getDistance() < bestRoute.getDistance())) {
						bestRoute = successor;
						fuelUsed = successor.getFuel();
						loadUsed = successor.getLoad();
					}
				}
				else {
					successor.setHeuristic(f);
					open.add(successor);
				}
			}
			
			closed.add(q);
		}
		
		return null;
	}	
	
	public static double hamiltonianPathHeuristic(AStarRoute route, ArrayList<Location> everyNode, Location start) {		
		ArrayList<Location> remainingNodes = new ArrayList<Location>();
		
		for (Location n : everyNode) {
			if (!route.contains(n)) {
				remainingNodes.add(n);
			}
		}
		remainingNodes.add(start);
		
		return minimumSpanningTree(remainingNodes);
	}
	
	private static double minimumSpanningTree(ArrayList<Location> nodes) {
		if (nodes.size() == 0) {
			return 0;
		}
		
		ArrayList<Location> tree = new ArrayList<Location>();
		tree.add(nodes.get(0));
		
		while (tree.size() != nodes.size()) {
			Location add = null;
			double min = Integer.MAX_VALUE;
			
			for (Location l1 : tree) {
				for (Location l2 : nodes) {
					if (!tree.contains(l2)) {
						ArrayList<Location> p = new ArrayList<Location>();
						double d = shortestDistance(l1, l2, p);
						if (d < min) {
							add = l2;
							min = d;
						}
					}
				}
			}
			
			tree.add(add);
		}
		
		return 0;
	}
	
	public static double shortestDistance(Location start, Location goal, ArrayList<Location> path) {
		if (start.equals(goal)) {
			return 0;
		}
		
		String name1 = start.getID() + "_" + goal.getID();
		String name2 = goal.getID() + "_" + start.getID();
		
		if (calculated_distances.containsKey(name1)) {
			Distance d = calculated_distances.get(name1);
			setPath(path, d, false);
			return d.getWeight();
		}
		
		if (calculated_distances.containsKey(name2)) {
			Distance d = calculated_distances.get(name2);
			setPath(path, d, true);
			return d.getWeight();
		}
		
		UserInterface.UserInterface.deliveryInfo.resetAStarVars();
		ArrayList<Location> closed = new ArrayList<Location>();
		PriorityQueue<Location> open = new PriorityQueue<Location>();
		
		open.add(start);
		
		while (open.size() != 0) {
			Location q = open.poll();
			
			for (Connection connection: q.getConnections()) {
				Location successor;
				if (connection.getLocation1() == q) {
					successor = connection.getLocation2();
				}
				else {
					successor = connection.getLocation1();
				}
				
				if (successor.equals(goal)) {
					closed.add(q);
					closed.add(successor);
					successor.setParent(q);
					double weight = q.getG() + successor.linearDistance(q);
					
					setPath(path, start, goal);
					
					Distance d = new Distance(start, goal, path, weight);
					calculated_distances.put(name1, d);
					
					return weight;
				}
				
				double g = q.getG() + successor.linearDistance(q);
				double h = heuristic(successor, goal);
				double f = g + h;
				
				if (inContainer(successor, open)) {
					if (successor.getF() > f) {
						open.remove(successor);
						successor.setAStarVars(g, h, f, q);
						open.add(successor);
					}
				}
				else if (inContainer(successor, closed)) {
					if (successor.getF() > f) {
						closed.remove(successor);
						successor.setAStarVars(g, h, f, q);
						open.add(successor);
					}
				}
				else {
					successor.setAStarVars(g, h, f, q);
					open.add(successor);
				}
			}
			
			closed.add(q);
		}
		
		return Integer.MAX_VALUE;
	}
	
	public static double heuristic(Location l, Location goal) {
		return l.linearDistance(goal);
	}

	public static boolean inContainer(Location successor, AbstractCollection<Location> container) {
		Iterator<Location> it = container.iterator();
		while (it.hasNext()) {
			Location l = it.next();
			if (l.equals(successor)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void setPath(ArrayList<Location> path, Distance d, boolean reverse) {
		path.clear();
		if (reverse) {
			for (int i = d.getPath().size()-1; i >= 0; i--) {
				path.add(d.getPath().get(i));
			}
		}
		else {
			for (int i = 0; i < d.getPath().size(); i++) {
				path.add(d.getPath().get(i));
			}
		}	
	}
	
	public static void setPath(ArrayList<Location> path, Location start, Location goal) {
		path.clear();
		
		Location l = goal;
		while (l != null) {
			path.add(l);
			l = l.getParent();
		}
		
		Collections.reverse(path);
	}
	
	public static double getTotalWeight(ArrayList<Location> list) {
		double total = 0;
		
		for (int i = 0; i < list.size()-1; i++) {
			ArrayList<Location> path = new ArrayList<Location>();
			total += AStar.shortestDistance(list.get(i), list.get(i+1), path);
		}
		
		return total;
	}
	
}
