package fr.evolya.javatoolkit.gui.swing.console;

import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import fr.evolya.javatoolkit.cli.CLISessionStream;
import fr.evolya.javatoolkit.code.Flag;
import fr.evolya.javatoolkit.code.annotations.AsynchOperation;
import fr.evolya.javatoolkit.code.funcint.Callback;
import fr.evolya.javatoolkit.code.utils.StringUtils;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.swing.Clipboard;
import fr.evolya.javatoolkit.gui.swing.JPanelView;

public class ConsolePanelView extends JPanelView implements IConsoleView, CLISessionStream {
	
	/**
	 * Numéro de série de cette vue
	 */
	private static final long serialVersionUID = -256032153113294466L;

	/**
	 * La liste des inputs de l'utilisateur
	 */
	protected LinkedList<String> _bufferInputs = new LinkedList<String>();
    
	/**
	 * Taille maximale du buffer qui mémorise les lignes entrées par l'utilisateur
	 */
	protected int _bufferSize = 50;
    
	/**
	 * L'emplacement actuel dans le buffer des lignes d'inputs
	 */
	protected int bufferPosition = -1;
	
	/**
	 * Le panel contenant la console
	 */
	protected JScrollPane _scrollPane;
    
    /**
     * La console de sortie
     */
	protected JTextArea _outputConsole;
    
    /**
     * Le texte field d'entrée
     */
	protected JPasswordField _inputTextfield;
	
	/**
	 * Le label à côté de l'input
	 */
	protected JLabel _inputLabel;

	/**
	 * Le handler pour la prochaine commande entrée par l'utilisateur
	 */
	protected ConsoleInputHandler _consoleInputHandler;
	
	/**
	 * Le type d'input actuel
	 */
	protected ConsoleInputMode _inputMode;
	
	/**
	 * Feature d'auto scroll down à l'écriture dans l'output
	 */
	protected boolean _autoScrollDown = true;

	/**
	 * Moment où la derniére pression sur la touche tab a été faite
	 */
	protected long _lastTabKeyPress = 0;
	
	/**
	 * 
	 */
	protected Flag _flags = new Flag() {
		@Override
		protected void onChange(Flag newFlags) {
			// Red input add
			if (newFlags.has(EFFECT_RED_INPUT)) {
				if (!has(EFFECT_RED_INPUT)) {
					_inputTextfield.setBackground(Color.RED);
				}
			}
			// Red input : remove
			else if (has(EFFECT_RED_INPUT)) {
				_inputTextfield.setBackground(null);
			}
		}
	};
	
	/**
	 * Events de la console.
	 */
	protected EventSource<ConsoleViewListener> _eventsConsole =
			new EventSource<ConsoleViewListener>(ConsoleViewListener.class, this);

	/**
	 * Constructeur par défaut, qui initialise les composants de la vue.
	 */
    public ConsolePanelView() {
    	super();
        initComponents();
        registerListeners();
        setInputMode(ConsoleInputMode.NORMAL);
    }
	
	/**
     * Initialisation des composants graphiques
     */
    @SuppressWarnings("unchecked")
	protected void initComponents() {
    	
    	Font font = new Font("Consolas", Font.PLAIN, 12);
		
		JPanel panel = new JPanel();
		
		_scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(panel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
				.addComponent(_scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(_scrollPane, GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
		);
		
		// Zone du haut : création des composants
		
		_outputConsole = new JTextArea();
		_outputConsole.setEditable(false);
		_scrollPane.setViewportView(_outputConsole);
		_outputConsole.setFont(font);
		
		// Zone du bas : création des composants
		
		_inputTextfield = new JPasswordField();
		_inputTextfield.setFont(font);
		_inputTextfield.setColumns(10);
		_inputTextfield.setFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		
		_inputLabel = new JLabel(">");
		_inputLabel.setFont(font);
		
		// Zone du bas : mise en place des composants
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(_inputLabel)
					.addGap(18)
					.addComponent(_inputTextfield, GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
					.addComponent(_inputTextfield, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
					.addComponent(_inputLabel))
		);
		panel.setLayout(gl_panel);
		setLayout(groupLayout);

	}
    
    /**
     * Initialise les listeners de l'IHM.
     */
	protected void registerListeners() {
	    	
    	// Pour que l'input soit en focus au lancement de la fenêtre
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt) {
            	_inputTextfield.requestFocus();
            }
        });
        
        // Pour que l'input soit focus si on clique sur le fond de la vue
        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
            	_inputTextfield.requestFocus();
            }
        });
        
        // Quand on saisie du texte dans l'input
        _inputTextfield.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                onInputKeyPress(evt);
            }
        });
        
        // Quand on clic sur l'output, on revient sur l'input
        _outputConsole.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            	// Focus : clic gauche
            	if (evt.getButton() == 1) {
            		if (_inputTextfield.isEnabled()) {
            			_inputTextfield.requestFocus();
            		}
            		else {
            			_inputLabel.requestFocus();
            		}
            		return;
            	}
            	// Past : clic droit
            	if (evt.getButton() == 3) {
            		final String data = Clipboard.getStringContents();
            		if (data != null) {
            			final String input = new String(_inputTextfield.getPassword());
            			final int pos = _inputTextfield.getCaretPosition();
            			final int inputLength = input.length();
            			
            			// Append
            			if (inputLength == pos) {
            				_inputTextfield.setText(input + data);
            			}
            			// Insert
            			else {
            				_inputTextfield.setText(
            						input.substring(0, pos)
            						+ data
            						+ input.substring(pos));
            			}
            		}
            	}
            }
        });
        
        // Quand on sélectionne du texte -> go copier/coller
        _outputConsole.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				if (e.getDot() != e.getMark()) {
					int min = Math.min(e.getMark(), e.getDot());
					int max = Math.max(e.getMark(), e.getDot());
					String copy = _outputConsole.getText().substring(min, max);
					Clipboard.setContents(copy);
					_inputTextfield.requestFocus();
				}
			}
		});
        
        // Quand on valide le champ d'input
        _inputTextfield.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	final String str = new String(_inputTextfield.getPassword());
            	_inputTextfield.setText("");
            	inputCommandAsynch(str, true);
            }
        });
        
        // Quand on fait Ctrl+C sur le label, qui sert de zone capture
        // pour la demande d'interruption
        _inputLabel.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyPressed(KeyEvent e) {
        		 if (e.getKeyCode() == 67 && e.getModifiers() == 2) {
        			 stopInputHandler(true);
        		 }
        	}
		});
        
	}

	@AsynchOperation
	public void inputCommandAsynch(final String cmd, final boolean isUserIntent) {
		inputCommandAsynch(cmd, isUserIntent, null);
	}
	
	@AsynchOperation
	public void inputCommandAsynch(final String cmd, final boolean isUserIntent, final Callback<Boolean, Throwable> callback) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Boolean result = inputCommand(cmd, isUserIntent);
					if (callback != null) callback.onSuccess(result);
				}
				catch (Throwable t) {
					if (callback != null) callback.onFailure(t);
					else t.printStackTrace();
				}
			}
		}, "CommandExecutorThread").start();
	}
	
    /**
     * Executer une commande.
     * 
     * @param cmd
     * @param isUserInput
     * @return TRUE si la commande a bien été prise en compte, ou FALSE si non
     */
	@Override
	public boolean inputCommand(String cmd, boolean isUserIntent) {
		
		// Méfiance...
		if (SwingUtilities.isEventDispatchThread()) {
			throw new IllegalAccessError("ConsolePanelView.inputCommand() should not be"
					+ " launched in EDT");
		}
		
		// Vérification des arguments
    	if (cmd == null) {
    		throw new NullPointerException();
    	}
    	
		// On léve un event before, qui stoppe l'input
		if (!_eventsConsole.trigger("beforeCommandInput", cmd, this, isUserIntent)) {
			return false;
		}
    	
    	// Trim
    	cmd = cmd.trim();
    	
    	// Affichage de la commande dans la sortie
        if (isUserIntent) {
        	if (_inputMode == ConsoleInputMode.NORMAL) {
        		outputWriteLine("> " + cmd);
        	}
        	else {
        		outputWriteLine("> ********");
        	}
        }
        
		// On met la mise en forme par defaut
		effects().set(IConsoleView.EFFECT_DEFAULT);
        
        // Si la commande est vide, on s'arréte ici
        // Note: mais si on a besoin de rentrer un truc vide ?
        /*if (cmd.isEmpty()) {
        	return false;
        }*/
        
        // On désactive le champ d'input
        setInputEnabled(false);
	    
	    // Mémorisation des saisies (uniquement si on est en mode normal)
        if (_inputMode == ConsoleInputMode.NORMAL) {
        	
        	// On remet à zero le curseur
		    bufferPosition = -1;
		    
		    // On ajoute l'entrée
		    if (_bufferInputs.size() > 0) {
		        if (!cmd.equals(_bufferInputs.getFirst())) {
		            _bufferInputs.addFirst(cmd);
		            while (_bufferInputs.size() > _bufferSize) {
		                _bufferInputs.removeLast();
		            }
		        }
		    }
		    else {
		        _bufferInputs.addFirst(cmd);
		    }
		    
        }
        
	    // Notification du handler
	    ConsoleInputHandler handler = null;
	    String rewrite = null;
	    synchronized (this) {
	    	handler = _consoleInputHandler;
	    }
	    if (handler != null) {

			// On permet de réécrire la commande avant son traitement
			rewrite = _consoleInputHandler.rewriteInputCommand(cmd);
			if (rewrite != null) {
				cmd = rewrite;
			}
	    	
			// On lui demande de gérer la commande
			if (!_consoleInputHandler.handleInputCommand(cmd, this, isUserIntent)) {
				
				// On réactive le champ d'input
				setInputEnabled(true);
				
				// On léve un event en indiquant que la commande a eu un handler
				_eventsConsole.trigger("onCommandInput", cmd, this, isUserIntent, true);
				
				// S'il renvoie FALSE, alors le handler est conservé et on
				// ne valide pas la commande.
				return false;
				
			}
			
			// Si c'est bon, le handler est automatiquement détaché
			// On vérifie au passage que le handler n'ai pas été modifié.
			// S'il l'est, on le l'enléve pas.
			// Cela permet de pouvoir modifier le handler dans un handler.
			if (_consoleInputHandler == handler) {
				_consoleInputHandler = null;
			}
			
			// Et on remet le mode normal
			setInputMode(ConsoleInputMode.NORMAL);
				
		}
	    
	    // Trigger d'un event pour que les plugins puissent agir ici
	    _eventsConsole.trigger(
    		"onCommandInput",
    		cmd,
    		this,
    		isUserIntent,
    		handler != null && rewrite == null
	    );
	    
	    // On réactive le champ d'input
	    setInputEnabled(true);
	    
	    // OK pris en charge
	    return true;
	    
	}

	/**
	 * Activer ou désactiver la possibilité d'utiliser le champ d'input
	 */
	@Override
	public void setInputEnabled(final boolean set) {
		
		// On s'assure du lancement dans l'EDT
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setInputEnabled(set);
				}
			});
			return;
		}
		
		// On active ou désactive le champ
		_inputTextfield.setEnabled(set);
		
		// On lui donne le focus en cas d'activation
		if (set) {
			_inputTextfield.requestFocus();
		}
		// Sinon on place le focus sur une zone qui TODO
		else {
			_inputLabel.requestFocus();
		}
	}

	/**
	 * Handler des events de type KeyPress sur l'input
	 * 
	 * @param evt
	 */
	protected void onInputKeyPress(KeyEvent evt) {
	
	    int key = evt.getKeyCode();
	    int modif = evt.getModifiers();
	    
	    //System.out.println("Key : "+key+" Modifier : "+modif);
	    
	    // Right / Left
	    if (key == 37 || key == 39) {
	    	// On remet à jour le curseur
	        bufferPosition = -1;
	        return;
	    }
	    
	    // Page Up/Down
	    else if (key == 33 || key == 34) {
	    	// On simule un event sur l'outputConsole
	    	_outputConsole.dispatchEvent(new KeyEvent(
	    		_outputConsole,
	    		evt.getID(),
	    		evt.getWhen(),
	    		modif,
	    		key,
	    		evt.getKeyChar(),
	    		evt.getKeyLocation()
	    	));
	    }
	    
	    // Up
	    else if (key == 38) {
	    	if (_inputMode != ConsoleInputMode.NORMAL)
	    		return;
	    	// On incrémente la position du curseur
	        bufferPosition++;
	        // Si on est au dessus
	        if (bufferPosition >= _bufferInputs.size()) {
	            bufferPosition = -1;
	            _inputTextfield.setText("");
	        }
	        // On affiche la saisie
	        else _inputTextfield.setText(_bufferInputs.get(bufferPosition));
	    }
	    
	    // Down
	    else if (key == 40) {
	    	if (_inputMode != ConsoleInputMode.NORMAL)
	    		return;
	    	// On décrémente la position du curseur
	        bufferPosition--;
	        // Si on est en dessous
	        if (bufferPosition < 0) {
	            bufferPosition = _bufferInputs.size() - 1;
	            _inputTextfield.setText("");
	        }
	        // On affiche la saisie
	        else _inputTextfield.setText(_bufferInputs.get(bufferPosition));
	    }
	    
	    // Ctrl+Q
	    else if (key == 81 && modif == 2) {
	    	_eventsConsole.trigger("onKeyCloseIntent", this, evt, new Boolean(true) /* is user intent */);
	    }
	    
	    // Ctrl+C
	    else if (key == 67 && modif == 2) {
	    	stopInputHandler(true);
	    }
	    
	    // Tab
	    else if (key == KeyEvent.VK_TAB){
	    	if (_inputMode != ConsoleInputMode.NORMAL)
	    		return;
	    	long now = new Date().getTime();
	    	if (_lastTabKeyPress != 0 && now - _lastTabKeyPress < 1000) {
	    		_eventsConsole.trigger("onAutoCompleteIntent", this, evt, new Boolean(true) /* is user intent */);
	    		_lastTabKeyPress = 0;
	    	}
	    	else {
	    		_lastTabKeyPress  = now;
	    	}
	    }
	    
	    // Others good chars
	    else if (((key >= 32 && key <= 255) || key == 8) && modif == 0) {
	        // On remet le buffer à 0 dés que l'on bouge
	        bufferPosition = -1;
	    }
	    
	}
	
	public void append(String msg) {
		outputWrite(msg);
	}
	
	public void appendLine(String msg) {
		outputWriteLine(msg);
	}

	/**
	 * Ecrire une string dans l'output
	 * 
	 * @param msg
	 */
	@Override
	public synchronized void outputWrite(final String msg) {
		
		// On s'assure du lancement dans l'EDT
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					outputWrite(msg);
				}
			});
			return;
		}
		
		
        // On rajoute la ligne
		_outputConsole.append(msg);
        
		// Autoscoll
    	if (_autoScrollDown) {
    		_outputConsole.setCaretPosition(_outputConsole.getDocument().getLength());
    	}
    	
	}
	
	/**
	 * Ecrire une ligne dans l'output
	 * 
	 * @param msg
	 */
	@Override
	public void outputWriteLine(final String msg) {
		outputWrite(msg + System.getProperty("line.separator"));
	}
	
	/**
	 * La taille du buffer qui mémorise les commandes entrée précedement par l'user
	 * @return
	 */
	@Override
	public int getBufferSize() {
		return _bufferSize;
	}

	/**
	 * Modifier la taille du buffer qui mémorise les commandes entrée précedement par l'user
	 * @return
	 */
	@Override
	public void setBufferSize(int size) {
		_bufferSize = size;
	}

	/**
	 * Détruire la vue.
	 */
	@Override
	public void dispose() {
		_bufferInputs.clear();
		_bufferInputs = null;
		_bufferSize = 0;
		_inputTextfield = null;
		_outputConsole = null;
		remove(_scrollPane);
		_scrollPane = null;
		_consoleInputHandler = null;
		super.dispose();
	}
	
	/**
	 * Cette méthode permet de mettre en place un handler qui receptionnera
	 * la prochaine commande entrée par l'utilisateur.
	 * 
	 * C'est pour donner la possibilité d'interroger l'user, en lui posant
	 * une question, et en traitant sa réponse. Si la réponse lui convient,
	 * il est automatiquement retiré aprés son utilisation.
	 *  
	 * Le Ctrl+C peut eventuellement permettre de faire sauter le handler.
	 * 
	 * @param handler
	 */
	@Override
	public synchronized void setInputHandler(ConsoleInputHandler handler) {
		
		// Vérification des arguments
		if (handler == null) {
			throw new NullPointerException();
		}
		
		// Enregistrer le handler
		_consoleInputHandler = handler;
		
		// Poser la question
		if (_consoleInputHandler.hasQuestionString()) {
			outputWriteLine(_consoleInputHandler.getQuestionString());
		}
		
		// Modifier le mode d'input
		setInputMode(_consoleInputHandler.getInputMode());
		
	}
	
	/**
	 * Cette méthode permet de tenter l'interruption du consoleInputHandler
	 * s'il existe. 
	 */
	public void stopInputHandler() {
		stopInputHandler(false);
	}
	
	/**
	 * Cette méthode permet de tenter l'interruption du consoleInputHandler
	 * s'il existe. 
	 * 
	 * @param isUserIntent
	 */
	public synchronized boolean stopInputHandler(boolean isUserIntent) {
		
		// Il n'y a pas de handler pour le moment, donc c'est le job en cours qu'on cherche à couper
		if (_consoleInputHandler == null) {
			_eventsConsole.trigger("onInterruptRequest", this, isUserIntent);
			return false;
		}
		
		// On ne peut pas stopper ce handler, donc on ne fait rien
		if (!_consoleInputHandler.isStoppable()) {
			return false;
		}
		
		// Event before
		if (!_eventsConsole.trigger("beforeInputHandlerStop", this, _consoleInputHandler, isUserIntent)) {
			return false;
		}
		
		// On le retire tout simplement, en faisant une copie avant
		ConsoleInputHandler copy = _consoleInputHandler;
		_consoleInputHandler = null;
		
		// Et on affiche une ligne vide pour symboliser l'action
		outputWriteLine(">");
		
		// On reset le mode d'input
		setInputMode(ConsoleInputMode.NORMAL);
		
		// Event after
		_eventsConsole.trigger("afterInputHandlerStop", this, copy, isUserIntent);
		
		// OK c'est bon
		return true;

	}
	
	/**
	 * Renvoie TRUE si l'autoscroll de l'output est activé.
	 */
	@Override
	public boolean isAutoScrollDown() {
		return _autoScrollDown;
	}

	/**
	 * Active ou désactive l'autoscroll de l'output.
	 */
	@Override
	public void setAutoScrollDown(boolean enable) {
		_autoScrollDown = enable;
	}
	
	/**
	 * Modifier le mode de saisie de l'input
	 */
	@Override
	public synchronized boolean setInputMode(ConsoleInputMode mode) {
		
		// Uniquement si on change de mode
		if (_inputMode == mode) {
			return false;
		}
		
		// On met le nouveau
		switch (mode) {
			case PASSWORD :
				// On masque les caractéres
				_inputTextfield.setEchoChar('*');
				break;
			case NORMAL :
			default :
				// Si l'ancien mode était password, on efface les caratéres
				if (_inputMode == ConsoleInputMode.PASSWORD) {
					_inputTextfield.setText("");
				}
				// On affiche les caractéres
				_inputTextfield.setEchoChar((char)0);
				break;
		}

        // On enregistre le nouveau type d'input
        _inputMode = mode;
        
        // OK c'est fait
        return true;
        
	}
	
	public Flag effects() {
		return _flags;
	}

	public JTextField getInputField() {
		return _inputTextfield;
	}
	
	public JTextArea getOutputField() {
		return _outputConsole;
	}

	public JLabel getInputLabel() {
		return _inputLabel;
	}

	public ConsoleInputHandler getInputHandler() {
		return _consoleInputHandler;
	}
	
	public boolean hasInputHandler() {
		return _consoleInputHandler != null;
	}

	@Override
	public String getInputText() {
		return new String(_inputTextfield.getPassword());
	}

	@Override
	public void setInputText(String value) {
		_inputTextfield.setText(value);
	}

	@Override
	public void outputClean() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					outputClean();
				}
			});
			return;
		}
		_outputConsole.setText("");
	}

	@Override
	public EventSource<ConsoleViewListener> getEventsConsole() {
		return _eventsConsole;
	}

	public int getBufferPosition() {
		return bufferPosition;
	}

	public void setBufferPosition(int bufferPosition) {
		this.bufferPosition = bufferPosition;
	}

	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

	public JTextArea getOutputConsole() {
		return _outputConsole;
	}

	@Override
	public void outputCleanLines(int howMany) {
		if (howMany <= 0) return;
		// On s'assure du lancement dans l'EDT
		if (!SwingUtilities.isEventDispatchThread()) {
			final int n = howMany;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					outputCleanLines(n);
				}
			});
			return;
		}
		String data = _outputConsole.getText();
		for (int i = data.length() - 1; i >= 0; --i) {
			if (data.charAt(i) == StringUtils.NL_CHAR) {
				howMany--;
				if (howMany == 0) {
					_outputConsole.setText(data.substring(0, i + 1));
					return;
				}
			}
		}
		_outputConsole.setText(StringUtils.EMPTYSTRING);
	}

	@Override
	public boolean isRichTextSupported() {
		return false;
	}

	@Override
	public void setStyle(ConsoleTextStyle... style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearStyle(ConsoleTextStyle... style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearStyles() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String applyStyles(String text, ConsoleTextStyle... styles) {
		throw new UnsupportedOperationException();
	}

}
