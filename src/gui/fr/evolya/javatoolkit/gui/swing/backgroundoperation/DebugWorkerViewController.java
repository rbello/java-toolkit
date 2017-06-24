package fr.evolya.javatoolkit.gui.swing.backgroundoperation;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.threading.worker.IOperation;
import fr.evolya.javatoolkit.threading.worker.IRunnable;
import fr.evolya.javatoolkit.threading.worker.IWorker;
import fr.evolya.javatoolkit.threading.worker.WorkerListener;
import fr.evolya.javatoolkit.threading.worker.WorkerState;

public class DebugWorkerViewController
	extends AppViewController<DebugWorkerView, JFrame, AWTEvent, Component>
	implements WorkerListener, Runnable {

	private IWorker _worker;
	private App _mainApp;

	public DebugWorkerViewController(IWorker worker) {
		if (worker == null) {
			throw new NullPointerException();
		}
		_worker = worker;
	}

	@Override
	protected void connected(App app) {
		_mainApp = app;
		app.getEventsApp().bind("onApplicationStarted", this);
	}
	
	@Override
	protected void onViewCreated() {
		
		_worker.getEventsWorker().bind(this);
		
		// Start
		getView().getBtnStart().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				_worker.start();
			}
		});
		getView().getBtnStart().setEnabled(_worker.getState() == WorkerState.STOPPED);
		
		// Stop
		getView().getBtnStop().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				_worker.stop();
			}
		});
		getView().getBtnStop().setEnabled(_worker.getState() != WorkerState.STOPPED);
		
		// Label
		getView().getLblState().setText(_worker.getState().toString());
		
		// Combo box d'ajout
		getView().getComboBox().setModel(new DefaultComboBoxModel<String>(new String[] {"", "Random", "Blocker" }));
		getView().getComboBox().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (getView().getComboBox().getSelectedIndex() == 0) return;
				String value = (String) getView().getComboBox().getSelectedItem();
				switch (value) {
				case "Blocker" :
					_worker.invokeLater(new IRunnable() {
						@Override
						public void run() throws InterruptedException {
							System.err.println("Debut du blocker " + hashCode());
							while (true) {
								for (int i = 0, l = 25648150; i < l; i++) {
									String str = "a" + "b" + i;
								}
								Thread.sleep(1);
							}
						}
					});
					break;
				case "Random":
					_worker.invokeLater(new IRunnable() {
						@Override
						public void run() throws InterruptedException {
							long time = (long) (Math.random() * 15000);
							System.err.println("Debut du timer pour " + Math.round(time / 1000) + " secondes");
							Thread.sleep(time);
							System.err.println("Fin du timer");
						}
					});
					break;
				}
				getView().getComboBox().setSelectedIndex(0);
			}
		});
		
		for (IOperation job : _worker.getOperations())
		{
			addJob(job);
		}
		
	}

	@Override
	protected void onViewClosed() {
		_worker.getEventsWorker().unbind((WorkerListener)this);
		_mainApp.getEventsApp().unbind(this);
	}

	@Override
	protected DebugWorkerView constructView() {
		DebugWorkerView view = new DebugWorkerView();
		view.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return view;
	}

	@Override
	public void onWorkerStateChanged(WorkerState newState, WorkerState oldState, IWorker worker) {
		getView().getLblState().setText(newState.toString());
		switch (newState) {
		case STOPPED :
			getView().getBtnStart().setEnabled(true);
			getView().getBtnStop().setEnabled(false);
			break;
		case STOPPING :
			getView().getBtnStart().setEnabled(false);
			getView().getBtnStop().setEnabled(false);
			break;
		default :
			getView().getBtnStart().setEnabled(false);
			getView().getBtnStop().setEnabled(true);
			break;
		}
		updateJobsCount(worker);
	}

	@Override
	public void onJobAdded(IOperation job, IWorker worker) {
		addJob(job);
		updateJobsCount(worker);
	}

	private void addJob(final IOperation job) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				getView().getTableModel().addRow(new Object[] { job, "", "" });
			}
		});
	}

	@Override
	public void onJobStarted(IOperation job, IWorker worker) {
		updateJobsCount(worker);
	}

	@Override
	public void onJobFinished(final IOperation job, IWorker worker, Throwable error) {
		updateJobsCount(worker);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (int i = 0, l = getView().getTableModel().getRowCount(); i < l; ++i) {
					if (getView().getTableModel().getValueAt(i, 0) == job) {
						getView().getTableModel().removeRow(i);
						return;
					}
				}
			}
		});
	}

	private void updateJobsCount(IWorker worker) {
		getView().getLblJobs().setText(worker.getRunningCount() + "/" + worker.getPendingCount());
	}

	@Override
	public void onJobInterrupted(IOperation job, IWorker worker, InterruptedException error) {
		
	}

	@Override
	public void run() {
		buildView(true);
	}

}
