package com.gingerdroids.utils_java;

import java.awt.Color;

/**
 * Maps angles to colours, in a circularly smooth way. 
 * <p>
 * See <code>AppColourForTheta</code> for a demonstration. 
 *
 */
public class ColourForTheta { 
	
	public static ColourForTheta instance ; 

	private static final double zero = 0.0;

	private static final double oneThird = 1.0 / 3.0;
	
	private static final double twoThirds = 2.0 / 3.0;

	private static final double piTimesTwo = 2 * Math.PI ; 
	
	public static ColourForTheta getInstance() { 
		if (instance==null) instance = new ColourForTheta(1.0, 1024); 
		return instance ; 
	}
	
	private final double scaledPeakLength ; 
	
	private final int intervalCount ; 
	
	private final int [] colours ; 
	
	private final double redStart = oneThird ; 
	private final double greenStart = zero ; 
	private final double blueStart = twoThirds ; 

	/**
	 * Constructor. 
	 * 
	 * @param peakLength Between 0 and 1, inclusive. Zero or one are the best values. 
	 * The larger this is, the brighter the colours, on average. 
	 * For zero, the components will sum to (near) 255, and there will only be two non-zero components. 
	 * For one, the components will sum to near 2*255, and each component will be non-zero (almost) everywhere. 
	 * @param intervalCount How fine-grained to divide up the length 0..2pi. 
	 */
	public ColourForTheta(double peakLength, int intervalCount) { 
		if (peakLength<0) throw new IllegalArgumentException("'peakLength' is "+peakLength+". Must be in 0..1 inclusive."); 
		if (peakLength>1) throw new IllegalArgumentException("'peakLength' is "+peakLength+". Must be in 0..1 inclusive."); 
		this.scaledPeakLength = peakLength * oneThird ; 
		this.intervalCount = intervalCount ; 
		this.colours = new int[intervalCount] ; 
		for (int i=0 ; i<intervalCount ; i++) { 
			double redFrac = 1 + (i/(double)intervalCount) - redStart ; 
			int red = intensity(redFrac); 
			double greenFrac = 1 + (i/(double)intervalCount) - greenStart ; 
			int green = intensity(greenFrac); 
			double blueFrac = 1 + (i/(double)intervalCount) - blueStart ; 
			int blue = intensity(blueFrac); 
//			red = 0 ; 
//			blue = 0 ; 
			int rgb = new Color(red, green, blue).getRGB(); 
			colours[i] = rgb ; 
		}
	}

	/**
	 * Returns an RGB for theta. 
	 */
	public final int forTheta(double theta) { 
		if (theta>=0) { 
			return for0to2pi(theta%piTimesTwo); 
		} else { 
			double normed = piTimesTwo - ((-theta)%piTimesTwo); 
			return for0to2pi(normed); 
		}
	}
	
	/**
	 * Returns an RGB for theta in the range 0 to 2*PI, including zero, but not necessarily 2*PI. 
	 */
	private int for0to2pi(double theta) { 
		int index = (int) Math.round(theta*intervalCount/piTimesTwo); 
		if (index<intervalCount) return colours[index] ; 
		else return colours[0] ; 
	}
	
	private int intensity(double frac) { 
		frac = frac % 1.0 ; 
		if (frac<=oneThird) { 
			return twoFiveFive(frac*3); 
		} else {
			frac -= oneThird ; 
			if (frac<scaledPeakLength) { 
				return 255 ; 
			} else { 
				frac -= scaledPeakLength ; 
				if (frac<oneThird) { 
					return twoFiveFive(3*(oneThird-frac)); 
				} else {
					return 0 ; 
				}
			}
		}
	}
	
	private int twoFiveFive(double x) { 
		int i = (int) Math.floor(256*x); 
		if (i==256) return 255 ; 
		return i ; 
	}
	
	/* *
	 * Displays the colours generated by {@link ColourForTheta}. 
	 * Innermost is with <code>peakLength</code> arg <code>0.0</code>, outermost is <code>1.0</code>, 
	 * and a middle section is a smooth transition. 
	 * /
	public static class Display {  
		
		private BufferedImage image ;
		private JFrame frame; 
		private Component imageCanvas; 
		
		public Display() { 
			final int radius = 200 ; 
			final int length = 2 * radius + 1 ; 
			int innerRadius = (int) (radius * 0.6) ; 
			int outerRadius = (int) (radius * 0.75) ; 
			ArcTangentDiscrete atan = ArcTangentDiscrete.getInstance(); 
			//////  Build colour generators 
			int kMax = outerRadius-innerRadius;
			ColourForTheta [] colours = new ColourForTheta[kMax+1] ; 
			for (int k=0 ; k<=kMax ; k++) { 
				double peakLength = k / (double) kMax ; 
				colours[k] = new ColourForTheta(peakLength, 1024); 
			} 
			//////  Build image
			this.image = new BufferedImage(length, length, BufferedImage.TYPE_INT_RGB); 
			for (int xImage=0 ; xImage<length ; xImage++) { 
				int x = xImage - radius ; 
				for (int yImage=0 ; yImage<length ; yImage++) { 
					int y = yImage - radius ; 
					int r = (int) Math.round(Math.sqrt(x*x+y*y)); 
					if (r<radius) { 
						int k = r - innerRadius ; 
						if (k<0) k = 0 ; 
						else if (k>kMax) k = kMax ; 
						double theta = atan.xy(x, y); 
						int rgb = colours[k].forTheta(theta); 
						image.setRGB(xImage, yImage, rgb);
					} else { 
						image.setRGB(xImage, yImage, Color.WHITE.getRGB());
					}
				}
			}
			
			//////  Display image
			this.frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
			this.imageCanvas = new ImageCanvas();
			frame.getContentPane().add(imageCanvas); 
			frame.setVisible(true); 
			imageCanvas.getPreferredSize(); // Sets sizes in topPane
			frame.pack();
			imageCanvas.repaint();
		}
		
		@SuppressWarnings("serial")
		private class ImageCanvas extends Canvas { 
			
			@Override
			public void paint(Graphics g) {
				g.drawImage(image, 0, 0, null); 
			}
			
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(image.getWidth(), image.getHeight());
			}
		}

	}
	
	public static void main(String [] args) { 
		new Display();
	}
	*/

	
}
