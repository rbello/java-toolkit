package fr.evolya.javatoolkit.gui.swing.console;

import java.awt.AWTEvent;
import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.cli.CLISessionStream.ConsoleInputMode;
import fr.evolya.javatoolkit.code.Flag;
import fr.evolya.javatoolkit.events.alpha.IListener;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.swing.JFrameView;
import fr.evolya.javatoolkit.gui.swing.SwingHelper;

public class ConsoleFrameView extends JFrameView implements IConsoleView {

	/**
	 * Num�ro de s�rie de cette IHM.
	 */
	private static final long serialVersionUID = -7600296298748181911L;

	/**
	 * Un controleur tout simple pour initialiser cette vue.
	 */
	public static class DefaultConsoleFrameViewController extends AppViewController<ConsoleFrameView, JFrame, AWTEvent, Component> {

		@Override
		public ConsoleFrameView constructView() {
			return new ConsoleFrameView();
		}

		@Override
		protected void onViewClosed() { }

		@Override
		protected void onViewCreated() { }

	}

	private ConsolePanelView _panel;

	/**
	 * Create the frame.
	 */
	public ConsoleFrameView() {
		setTitle("Console");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new CardLayout(0, 0));
		final ConsoleFrameView view = this;
		_panel = new ConsolePanelView();
		_panel.getEventsView().redirect(getEventsView());
		// On redirige les demandes de close du panel vers la vue
		// (pour être utilisé par un viewcontroller)
	    _panel.getEventsView().bind("onViewCloseIntent", new IListener<String>() {
			@Override
			public boolean notifyEvent(String event, Object... args) {
				getEventsView().trigger("onViewCloseIntent", view, args[1], args[2]);
				// TODO ça ne marche pas car on ne peut pas régler la priorité
				// sur l'eventsource
				return false;
			}
	    });
		contentPane.add(_panel);
	}

	@Override
	public int getBufferSize() {
		return _panel.getBufferSize();
	}

	@Override
	public boolean inputCommand(String cmd, boolean isUserIntent) {
		return _panel.inputCommand(cmd, isUserIntent);
	}

	@Override
	public boolean isAutoScrollDown() {
		return _panel.isAutoScrollDown();
	}

	@Override
	public void outputWrite(String msg) {
		_panel.outputWrite(msg);
	}

	@Override
	public void outputWriteLine(String msg) {
		_panel.outputWriteLine(msg);
	}

	@Override
	public void setInputHandler(ConsoleInputHandler handler) {
		_panel.setInputHandler(handler);
	}

	@Override
	public void setAutoScrollDown(boolean enable) {
		_panel.setAutoScrollDown(enable);
	}

	@Override
	public void setBufferSize(int size) {
		_panel.setBufferSize(size);
	}

	@Override
	public void setInputEnabled(boolean set) {
		_panel.setInputEnabled(set);
	}

	@Override
	public boolean setInputMode(ConsoleInputMode mode) {
		return _panel.setInputMode(mode);
	}

	@Override
	public boolean stopInputHandler(boolean isUserIntent) {
		return _panel.stopInputHandler(isUserIntent);
	}

	@Override
	public Flag effects() {
		return _panel.effects();
	}

	public JTextField getInputField() {
		return _panel.getInputField();
	}
	
	public JTextArea getOutputField() {
		return _panel.getOutputField();
	}
	
	public JLabel getInputLabel() {
		return _panel.getInputLabel();
	}
	
	public void bounce() {
		SwingHelper.makeMeBounce(this);
	}

	public ConsoleInputHandler getInputHandler() {
		return _panel.getInputHandler();
	}
	
	public boolean hasInputHandler() {
		return _panel.hasInputHandler();
	}

	@Override
	public String getInputText() {
		return _panel.getInputText();
	}

	@Override
	public void setInputText(String value) {
		_panel.setInputText(value);
	}

	@Override
	public void outputClean() {
		_panel.outputClean();
	}

	@Override
	public EventSource<ConsoleViewListener> getEventsConsole() {
		return _panel.getEventsConsole();
	}

}
