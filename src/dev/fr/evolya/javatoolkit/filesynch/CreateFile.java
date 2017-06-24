package fr.evolya.javatoolkit.filesynch;

public class CreateFile extends SynchOperation {

	private SynchDirectory target;
	private String path;
	private SynchDirectory source;

	public CreateFile(SynchDirectory target, String path, SynchDirectory source) {
		super("Create file '" + path + "' --> " + target);
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
