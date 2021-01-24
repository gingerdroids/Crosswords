package org.blockframe.painters;

public class ProjectionLogger extends Projection { 
	
	public static ProjectionLogger instance ; 
	
	public static boolean isOn = true ; 
	
	public final Projection wrapped ; 
	
	public double xxxMin = Double.POSITIVE_INFINITY ; 
	
	public double xxxMax = Double.NEGATIVE_INFINITY ; 
	
	public double yyyMin = Double.POSITIVE_INFINITY ; 
	
	public double yyyMax = Double.NEGATIVE_INFINITY ; 
	
	public double zzzMin = Double.POSITIVE_INFINITY ; 
	
	public double zzzMax = Double.NEGATIVE_INFINITY ; 
	
	public double xxMin = Double.POSITIVE_INFINITY ; 
	
	public double xxMax = Double.NEGATIVE_INFINITY ; 
	
	public double yyMin = Double.POSITIVE_INFINITY ; 
	
	public double yyMax = Double.NEGATIVE_INFINITY ; 
	
	private boolean haveLogged = false ; 
	
	public ProjectionLogger(Projection wrapped) { 
		if (instance!=null) throw new RuntimeException("Already have an instance."); 
		this.wrapped = wrapped ; 
		ProjectionLogger.instance = this ; 
	}

	@Override
	public ProjectedCoords calculate(double xxx, double yyy, double zzz) { 
		if (isOn) { 
			this.haveLogged = true ; 
			if (xxx<xxxMin) xxxMin = xxx ; 
			if (xxx>xxxMax) xxxMax = xxx ; 
			if (yyy<yyyMin) yyyMin = yyy ; 
			if (yyy>yyyMax) yyyMax = yyy ; 
			if (zzz<zzzMin) zzzMin = zzz ; 
			if (zzz>zzzMax) zzzMax = zzz ; 
		}
		ProjectedCoords result = wrapped.calculate(xxx, yyy, zzz);
		if (isOn) { 
			if (result.x<xxMin) xxMin = result.x ; 
			if (result.x>xxMax) xxMax = result.x ; 
			if (result.y<yyMin) yyMin = result.y ; 
			if (result.y>yyMax) yyMax = result.y ; 
		}
		return result;
	}
	
	public static void print() { 
		instance.printInstance(); 
	}
		
	public void printInstance() { 
		if (!haveLogged) return ;
		System.out.println("xxxMin is "+xxxMin); 
		System.out.println("xxxMax is "+xxxMax); 
		System.out.println("yyyMin is "+yyyMin); 
		System.out.println("yyyMax is "+yyyMax); 
		System.out.println("zzzMin is "+zzzMin); 
		System.out.println("zzzMax is "+zzzMax); 
		System.out.println("xxMin is "+xxMin); 
		System.out.println("xxMax is "+xxMax); 
		System.out.println("yyMin is "+yyMin); 
		System.out.println("yyMax is "+yyMax); 
	}

}
