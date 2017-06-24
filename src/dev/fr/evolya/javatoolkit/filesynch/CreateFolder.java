package fr.evolya.javatoolkit.filesynch;

public class CreateFolder extends SynchOperation {

	private SynchDirectory target;
	private String path;

	public CreateFolder(SynchDirectory target, String path) {
		super("Create folder '" + path + "' --> " + target);
		this.target = target;
		this.path = path;
	}

	public void execute() throws Exception {
		target.mkdir(path);
	}
	
}
