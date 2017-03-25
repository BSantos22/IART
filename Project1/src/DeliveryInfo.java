import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

public class DeliveryInfo {
	private Truck truck;
	private HashMap<Integer, Location> locations;
	private ArrayList<Package> packages;
	
	public DeliveryInfo() {
		locations = new HashMap<Integer,Location>();
		packages = new ArrayList<Package>();
		parseDocument();
	}
	
	public Truck getTruck() {
		return truck;
	}
	
	public HashMap<Integer, Location> getLocations() {
		return locations;
	}
	
	public ArrayList<Package> getDeliveries() {
		return packages;
	}
	
	public void printTruck() {
		System.out.println("----Truck----");
		truck.print();
	}
	
	public void printLocations() {
		System.out.println("----Locations----");
		for (int key: locations.keySet()) {
			locations.get(key).print();
		}
	}
	
	public void printPackages() {
		System.out.println("----Packages----");
		for (int i = 0; i < packages.size(); i++) {
			packages.get(i).print();
		}
	}
	
	public void parseDocument() {
		try {
			File inputFile = new File("data/data1.xml");
			
			// Create a DocumentBuilder
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			// Create a Document from a file or stream
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			
			// Get truck info
			NodeList nList = doc.getElementsByTagName("truck");
			for (int i = 0; i < nList.getLength(); i++) {
				Element eElement = (Element) nList.item(i);
				int fuel = Integer.parseInt(eElement.getAttribute("fuel"));
				int load = Integer.parseInt(eElement.getAttribute("load"));
				
				truck = new Truck(fuel, load);
			}
			
			// Get locations
			nList = doc.getElementsByTagName("location");
			for (int i = 0; i < nList.getLength(); i++) {
				Element eElement = (Element) nList.item(i);
				int id = Integer.parseInt(eElement.getAttribute("id"));
				int x = Integer.parseInt(eElement.getAttribute("x"));
				int y = Integer.parseInt(eElement.getAttribute("y"));
				boolean fuel = Boolean.parseBoolean(eElement.getAttribute("fuel"));
				
				Location location = new Location(id, x, y, fuel);
				locations.put(id, location);
			}
			
			// Get connections
			nList = doc.getElementsByTagName("connection");
			for (int i = 0; i < nList.getLength(); i++) {
				Element eElement = (Element) nList.item(i);
				int location1 = Integer.parseInt(eElement.getAttribute("location1"));
				int location2 = Integer.parseInt(eElement.getAttribute("location2"));
				int fuel = Integer.parseInt(eElement.getAttribute("fuel"));
				
				Location l1 = locations.get(location1);
				Location l2 = locations.get(location2);
				
				l1.addConnection(l2, fuel);
				l2.addConnection(l1, fuel);
			}
			
			// Get deliveries
			nList = doc.getElementsByTagName("package");
			for (int i = 0; i < nList.getLength(); i++) {
				Element eElement = (Element) nList.item(i);
				int locationID = Integer.parseInt(eElement.getAttribute("location"));
				int volume = Integer.parseInt(eElement.getAttribute("volume"));
				int value = Integer.parseInt(eElement.getAttribute("value"));
				
				Location location = locations.get(locationID);
				
				Package delivery = new Package(location, volume, value);
				packages.add(delivery);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createDocument() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("deliveryInfo");
			doc.appendChild(rootElement);
			
			// truck
			Element truck = doc.createElement("truck");
			rootElement.appendChild(truck);
			Scanner reader = new Scanner(System.in);
			
			System.out.println("----Truck----");
			System.out.println("Fuel: ");
			int fuel = reader.nextInt();
			System.out.println("Load: ");
			int load = reader.nextInt();
			
			truck.setAttribute("fuel", Integer.toString(fuel));
			truck.setAttribute("load", Integer.toString(load));		
					
			// locations
			Element locations = doc.createElement("locations");
			rootElement.appendChild(locations);
			
			System.out.println("----Locations----");
			System.out.println("Number of locations: ");
			int nLocations = reader.nextInt();
			
			
			// connections
			Element connections = doc.createElement("connections");
			rootElement.appendChild(connections);
			
			System.out.println("----Connections----");
			System.out.println("Number of connections: ");
			int nConnections = reader.nextInt();
			
			// packages
			Element packages = doc.createElement("packages");
			rootElement.appendChild(packages);
			
			System.out.println("----Packages----");
			System.out.println("Number of packages: ");
			int nPackages = reader.nextInt();
			
			// write to xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("data/" + System.currentTimeMillis() + ".xml"));
			
			transformer.transform(source, result);	
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}
