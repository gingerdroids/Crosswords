package org.blockframe.painters;

public class ProjectionOutScale extends Projection { 
	
	private final double xFactor ; 
	
	private final double yFactor ; 
	
	private final Projection wrappedProjection ; 
	
	public ProjectionOutScale(double xFactor, double yFactor, Projection wrappedProjection) {
		this.xFactor = xFactor ; 
		this.yFactor = yFactor ; 
		this.wrappedProjection = wrappedProjection ; 
	}

	public ProjectedCoords calculate(double xxx, double yyy, double zzz) {
		ProjectedCoords wrappedResult = wrappedProjection.calculate(xxx, yyy, zzz); 
		ProjectedCoords result = new ProjectedCoords(wrappedResult.x*xFactor, wrappedResult.y*yFactor); 
		return result ;
	}

}
