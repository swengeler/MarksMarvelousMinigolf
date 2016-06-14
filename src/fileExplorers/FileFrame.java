package fileExplorers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import terrains.World;

@SuppressWarnings("serial")
public class FileFrame extends JFrame {
	
	private World tmpWorld;
	
	public FileFrame(String option, World world) {
		this.setPreferredSize(new Dimension(300,200));
		this.setLocationRelativeTo(null);
		JPanel panel = new JPanel();
		JButton button = new JButton("Close");
		JTextField text = new JTextField();
		button.addActionListener(new CloseListener());	
		panel.setLayout(new BorderLayout());
		panel.add(text, BorderLayout.NORTH);
		panel.add(button, BorderLayout.CENTER);
		JFileChooser c = new JFileChooser();
		if (option.equals("save")) {
			c.setCurrentDirectory(new File("sav"));
		    int rVal = c.showSaveDialog(this);
		    if (rVal == JFileChooser.APPROVE_OPTION) {
		    	String filename = c.getSelectedFile().getName();
		        String dir = c.getCurrentDirectory().toString();
		        String pathname = dir + "/" + filename;
		        try {
		        	FileOutputStream fos = new FileOutputStream(pathname + ".ser");
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(world);
					oos.close();
		        } catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		       text.setText("Course saved");
		    }
		    if (rVal == JFileChooser.CANCEL_OPTION) {
		    	text.setText("No action");
		    }
		} else if(option.equals("load")){
			c.setCurrentDirectory(new File("sav"));
		    int rVal = c.showOpenDialog(this);
		    if (rVal == JFileChooser.APPROVE_OPTION) {
		    	String filename = c.getSelectedFile().getName();
		        String dir = c.getCurrentDirectory().toString();
		        String pathname = dir + "/" + filename;
		        try {
		        	FileInputStream fis = new FileInputStream(pathname);
					ObjectInputStream ois = new ObjectInputStream(fis);
					tmpWorld = (World) ois.readObject();
					ois.close();
		        } catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
		       text.setText("Course loaded");
		    }
		    if (rVal == JFileChooser.CANCEL_OPTION) {
		    	text.setText("No action");
		    }
		}
		this.add(panel);
		this.pack();
	}

	public void setVisible() {
		setVisible(true);
	}
	
	public World returnWorld() {
		return tmpWorld;
	}
	
	class CloseListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			FileFrame.this.dispatchEvent(new WindowEvent(FileFrame.this, WindowEvent.WINDOW_CLOSING));
		}
	}
}
