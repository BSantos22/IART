package AStar;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import ProblemData.Location;

public class AStar {
	private static HashMap<String, Distance> calculated_distances = new HashMap<String, Distance>();
	
	public static double run(Location start, Location goal, ArrayList<Location> closed) {
		String name1 = start.getID() + "_" + goal.getID();
		String name2 = goal.getID() + "_" + start.getID();
		if (calculated_distances.containsKey(name1)) {
			Distance d = calculated_distances.get(name1);
			changeClosed(closed, d, false);
			return d.getWeight();
		}
		
		if (calculated_distances.containsKey(name2)) {
			Distance d = calculated_distances.get(name2);
			changeClosed(closed, d, true);
			return d.getWeight();
		}
		
		UserInterface.UserInterface.deliveryInfo.resetAStarVars();
		closed.clear();
		PriorityQueue<Location> open = new PriorityQueue<Location>();
		
		open.add(start);
		
		while (open.size() != 0) {
			Location q = open.poll();
			
			for (Location successor: q.getConnections().keySet()) {
				if (successor.equals(goal)) {
					closed.add(q);
					closed.add(successor);
					double weight = q.getG() + successor.distance(q);
					
					Distance d = new Distance(start, goal, closed, weight);
					calculated_distances.put(name1, d);
					
					return weight;
				}
				
				double g = q.getG() + successor.distance(q);
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
		return l.distance(goal);
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
	
	public static void changeClosed(ArrayList<Location> closed, Distance d, boolean reverse) {
		closed.clear();
		if (reverse) {
			for (int i = d.getPath().size()-1; i >= 0; i--) {
				closed.add(d.getPath().get(i));
			}
		}
		else {
			for (int i = 0; i < d.getPath().size(); i++) {
				closed.add(d.getPath().get(i));
			}
		}	
	}
}
