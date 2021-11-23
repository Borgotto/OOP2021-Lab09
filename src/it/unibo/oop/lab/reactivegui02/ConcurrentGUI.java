package it.unibo.oop.lab.reactivegui02;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This is a first example on how to realize a reactive GUI.
 */
public final class ConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    /**
     * Builds a new CGUI.
     */
    public ConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);

        final Agent agent = new Agent();
        new Thread(agent).start();

        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                agent.stopCounting();
                stop.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
            }            
        });
                
        up.addActionListener(e -> agent.countUp());
        down.addActionListener(e -> agent.countDown());
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent implements Runnable {
        private volatile boolean stop;
        private volatile boolean direction = true;
        private volatile int counter;
        

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            // This will happen in the EDT: since i'm reading counter it needs to be volatile.
                            ConcurrentGUI.this.display.setText(Integer.toString(Agent.this.counter));
                        }
                    });

                    if (this.direction) {
                        this.counter++;                        
                    } else {
                        this.counter--;
                    }
                    
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void countDown() {
            this.direction = false;
        }

        public void countUp() {
            this.direction = true;
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }
    }
}
