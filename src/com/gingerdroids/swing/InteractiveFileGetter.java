package com.gingerdroids.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class InteractiveFileGetter { 
	
	private final Object lock = new Object(); 
	
	private File file ; 
	
	private boolean isDone ; 

	public File chooseFile() { 
		synchronized (lock) { 
			this.file = null ; 
			this.isDone = false ; 
			final JFrame frame = new JFrame(); 
			final JFileChooser chooser = new JFileChooser(); 
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			configure(frame, chooser);
			chooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String actionCommand = e.getActionCommand();
					if (actionCommand.equals("ApproveSelection")) { 
						File file = chooser.getSelectedFile(); 
						InteractiveFileGetter.this.file = file ; 
					} else if (actionCommand.equals("CancelSelection")) { 
						frame.dispose();
					} else { 
						System.err.println("Huh? Command was: "+actionCommand); 
					}
					InteractiveFileGetter.this.isDone = true ; 
				}
			});
			frame.add(chooser); 
			frame.pack();
			frame.setVisible(true);
			while (!isDone) try { Thread.sleep(50L); } catch (InterruptedException e1) {} 
			frame.dispose(); 
			return file ; 
		}
	}

	/**
	 * Configure the {@link JFrame} and {@link JFileChooser}. 
	 * <p>
	 * For example: <pre>
frame.setTitle("Select the PDF file");
frame.setPreferredSize(new Dimension(800, 400)); 
chooser.setFileFilter(new FileNameExtensionFilter("PDF documents", "PDF"));
	 * </pre>
	 * @param frame
	 * @param chooser
	 */
	protected void configure(final JFrame frame, final JFileChooser chooser) {
	}

}
