package it.unibo.oop.reactivegui03;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private final Agent agent;
    private final TimerAgent timerAgent;
    final JButton up;
    final JButton down;
    final JButton stop;

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        this.up = new JButton("up");
        panel.add(up);
        this.down = new JButton("down");
        panel.add(down);
        this.stop = new JButton("stop");
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        this.agent = new Agent();
        new Thread(agent).start();
        this.timerAgent = new TimerAgent();
        new Thread(timerAgent).start();
        stop.addActionListener(e -> {
            agent.stopCounting();
            stop.setEnabled(false);
            up.setEnabled(false);
            down.setEnabled(false);
        });
        up.addActionListener(e -> agent.setUp(true));
        down.addActionListener(e -> agent.setUp(false));
    }

    /*
     * The timer agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     * It stops counting after 10 seconds of running.
     */
    private final class TimerAgent implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            if (!agent.isStopped()) {
                SwingUtilities.invokeLater(() -> {
                    agent.stopCounting();
                    stop.setEnabled(false);
                    up.setEnabled(false);
                    down.setEnabled(false);                
                });
            }
        }
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility.
         */
        private volatile boolean stop;
        private volatile boolean up = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (up) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * External command to set the direction of counting.
         */
        public void setUp(final boolean up) {
            this.up = up;
        }

        /**
         * Getter for stop flag.
         */
        public boolean isStopped() {
            return stop;
        }

    }
}
