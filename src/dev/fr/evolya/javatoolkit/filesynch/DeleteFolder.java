package fr.evolya.javatoolkit.filesynch;

public class DeleteFolder extends SynchOperation {

	private SynchDirectory target;
	private String path;

	public DeleteFolder(SynchDirectory target, String path) {
		super("Delete folder '" + path + "' --> " + target);
		this.target = target;
		this.path = path;
	}

	@Override
	public void execute() {
		
	}
	
}
