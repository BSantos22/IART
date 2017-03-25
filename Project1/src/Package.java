public class Package {
	private Location location;
	private int volume;
	private int value;
	
	public Package(Location location, int volume, int value) {
		this.location = location;
		this.volume = volume;
		this.value = value;
	}
	
	public void print() {
		System.out.println(location.getID() + " " + volume + " " + value);
	}
}
