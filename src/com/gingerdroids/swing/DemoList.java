package com.gingerdroids.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DemoList {

	public static void main(String[] args) {
		Integer [] zeroToFour = new Integer[] {0, 1, 2, 3, 4} ; 
		NumberList numberList = new NumberList(); 
		numberList.setList(Arrays.asList(zeroToFour));

		FrameHolder frameHolder = new FrameHolder(); 
		frameHolder.show(numberList.getListPanel()); 
	}
	
	private static class NumberList extends GBList_Draft<Integer, NumberList.NumberButton> {

		@Override
		protected NumberButton getViewFor(Integer item) {
			return new NumberButton(item); 
		} 
		
		private class NumberButton extends JButton { 
			final int number ; 
			NumberButton(final Integer number) { 
				super(""+number); 
				this.number = number ; 
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JPanel listPanel = getListPanel();
						NumberList.this.add(listPanel.getComponentCount()-number-1);
						NumberList.this.remove(number);
						System.out.println(""+listPanel.getComponentCount()); 
						listPanel.validate();
						listPanel.repaint(); 
					}
				});
			}
		}
	}

}
