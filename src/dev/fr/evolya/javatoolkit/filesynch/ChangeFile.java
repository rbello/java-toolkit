package fr.evolya.javatoolkit.filesynch;

public class ChangeFile extends SynchOperation {

	private SynchDirectory target;
	private String path;
	private SynchDirectory source;

	public ChangeFile(SynchDirectory target, String path, SynchDirectory source) {
		super("Change file '" + path + "' --> " + target);
		this.target = target;
		this.path = path;
		this.source = source;
	}

	@Override
	public void execute() throws Exception {
		if (!source.copyFileTo(path, target)) {
			if (!target.copyFileFrom(path, source)) {
				System.err.println("[Synch] Unable to create file " + path);
			}
		}
	}
	
}
