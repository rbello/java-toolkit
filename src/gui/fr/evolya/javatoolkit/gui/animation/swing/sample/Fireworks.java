package fr.evolya.javatoolkit.gui.animation.swing.sample;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.evolya.javatoolkit.gui.animation.AnimationConfig;
import fr.evolya.javatoolkit.gui.animation.Timeline;
import fr.evolya.javatoolkit.gui.animation.Timeline.RepeatBehavior;
import fr.evolya.javatoolkit.gui.animation.TimelineScenario;
import fr.evolya.javatoolkit.gui.animation.callback.TimelineScenarioCallback;
import fr.evolya.javatoolkit.gui.animation.interpolator.CorePropertyInterpolators;
import fr.evolya.javatoolkit.gui.animation.swing.AWTPropertyInterpolators;
import fr.evolya.javatoolkit.gui.animation.swing.SwingRepaintTimeline;

public class Fireworks extends JFrame {

	public static void main(String[] args) {
		
		AnimationConfig.getInstance().addPropertyInterpolatorSource(new CorePropertyInterpolators());
		AnimationConfig.getInstance().addPropertyInterpolatorSource(new AWTPropertyInterpolators());
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Fireworks().setVisible(true);
			}
		});
	}
	
	private Set<VolleyExplosion> volleys;

	private Map<VolleyExplosion, TimelineScenario> volleyScenarios;

	private JPanel mainPanel;
	
	public Fireworks() {
		
	   super("Swing Fireworks");

	   this.mainPanel = new JPanel() {
	      @Override
	      protected void paintComponent(Graphics g) {
	         super.paintComponent(g);
	         synchronized (volleys) {
	            for (VolleyExplosion exp : volleys)
	               exp.paint(g);
	         }
	      }
	   };
	   this.mainPanel.setBackground(Color.black);
	   this.mainPanel.setPreferredSize(new Dimension(480, 320));

	   Timeline repaint = new SwingRepaintTimeline(this);
	   repaint.playLoop(RepeatBehavior.LOOP);

	   this.volleys = new HashSet<VolleyExplosion>();
	   this.volleyScenarios = new HashMap<VolleyExplosion, TimelineScenario>();

	   this.mainPanel.addMouseListener(new MouseAdapter() {
	      @Override
	      public void mousePressed(MouseEvent e) {
	         synchronized (volleys) {
	            for (TimelineScenario scenario : volleyScenarios.values())
	               scenario.suspend();
	         }
	      }

	      @Override
	      public void mouseReleased(MouseEvent e) {
	         synchronized (volleys) {
	            for (TimelineScenario scenario : volleyScenarios.values())
	               scenario.resume();
	         }
	      }
	   });

	   mainPanel.addComponentListener(new ComponentAdapter() {
	      @Override
	      public void componentResized(ComponentEvent e) {
	         if ((mainPanel.getWidth() == 0) || (mainPanel.getHeight() == 0))
	            return;
	         new Thread() {
	            @Override
	            public void run() {
	               while (true) {
	                  addExplosions(5);
	               }
	            }
	         }.start();
	      }
	   });

	   this.add(mainPanel);
	   this.pack();
	   this.setLocationRelativeTo(null);
	   this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void addExplosions(int count) {
		final CountDownLatch latch = new CountDownLatch(count);

		for (int i = 0; i < count; i++) {
			int r = (int) (255 * Math.random());
			int g = (int) (100 + 155 * Math.random());
			int b = (int) (50 + 205 * Math.random());
			Color color = new Color(r, g, b);

			int x = 60 + (int) ((mainPanel.getWidth() - 120) * Math.random());
			int y = 60 + (int) ((mainPanel.getHeight() - 120) * Math.random());
			final VolleyExplosion exp = new VolleyExplosion(x, y, color);
			synchronized (volleys) {
				volleys.add(exp);
				TimelineScenario scenario = exp.getExplosionScenario();
				scenario.addCallback(new TimelineScenarioCallback() {
					@Override
					public void onTimelineScenarioDone() {
						synchronized (volleys) {
							volleys.remove(exp);
							volleyScenarios.remove(exp);
							latch.countDown();
						}
					}
				});
				volleyScenarios.put(exp, scenario);
				scenario.play();
			}
		}

		try {
			latch.await();
		} catch (Exception exc) {
		}
	}

	
	
}