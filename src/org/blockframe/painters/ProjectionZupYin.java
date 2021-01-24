package org.blockframe.painters;

/**
 * Projects 3D into 2D. The page is the x,z plane (x rightwards, z upwards) and the y-axis goes into the page. 
 * <p>
 * The camera is at the origin, and the screen is the plane y=0. 
 * <p>
 * The location of the camera and screen are inconvenient for objects centred around the origin. 
 * Most applications will wrap this base projection with shifts, scales and rotations - see {@link Projection}. 
 */
public class ProjectionZupYin extends Projection { 
	
	public final double yCamera = 0 ; 
	
	public final double yScreen = 1 ; 
	
	private final double screenDepth ; 
	
	public ProjectionZupYin() { 
		this.screenDepth = yScreen - yCamera ; 
	}

	public ProjectedCoords calculate(double xxx, double yyy, double zzz) { 
		double pointDepth = yyy - yCamera ; 
		double screenFactor = screenDepth / pointDepth ; 
		double xProjected = xxx * screenFactor ; 
		double zProjected = zzz * screenFactor ; 
		return new ProjectedCoords(xProjected, zProjected);
	}

}
