package fr.evolya.javatoolkit.filesynch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSynchManager {

	private SynchDirectory _left;
	private SynchDirectory _right;
	
	/*public static void main(String[] args) {
		
		SynchDirectory left = new SynchDirectoryFilesystem(
				"C:\\Users\\rbello\\Desktop\\PERSO\\WorspaceJava\\sample\\src");
		FTPServerReference server = new FTPServerReference("localhost", "test", "test");
		SynchDirectory right = new SynchDirectoryFTP(server , "/");
		
		FileSynchManager mgr = new FileSynchManager(left, right);
		
		mgr.init();
		
		System.out.println("Execute synchro");
		System.out.println("Left: " + mgr.getLeftDirectory());
		System.out.println("Right: " + mgr.getRightDirectory());
		List<SynchOperation> operations = mgr.detectChanges("");
		System.out.println("Operations: " + operations.size());
		for (SynchOperation op : operations) {
			System.out.println(op);
			try {
				op.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		mgr.close();
		
	}*/
	
	public FileSynchManager(SynchDirectory left, SynchDirectory right) {
		this._left = left;
		this._right = right;
	}

	public SynchDirectory getLeftDirectory() {
		return _left;
	}

	public SynchDirectory getRightDirectory() {
		return _right;
	}

	public List<SynchOperation> detectChanges() {
		return detectChanges("");
	}
	
	public List<SynchOperation> detectChanges(String path) {
		if (!isReady()) {
			return null;
		}
		List<SynchOperation> operations = new ArrayList<SynchOperation>();
		detectChanges(path, _right, _left, operations, true);
		detectChanges(path, _left, _right, operations, false);
		return operations;
	}

	private void detectChanges(String path, SynchDirectory left, SynchDirectory right, List<SynchOperation> operations, boolean createOnly) {
		
		String[] list = left.list(path);
		if (list == null) return;
		for (String sub : list) {
			final String subpath = path + "/" + sub;
			if (!needToDetect(subpath)) continue;
			if (left.isDirectory(subpath)) {
				handleDirectory(operations, left, right, subpath, createOnly);
				continue;
			}
			handleFile(operations, left, right, subpath, createOnly);
		}
	}

	public boolean needToDetect(String subpath) {
		// Cette implémentation ne gère pas le temporisation du refresh
		return true;
	}

	protected void handleFile(List<SynchOperation> operations,
			SynchDirectory left, SynchDirectory right, final String subpath, boolean createOnly) {
		
		// Le fichier n'existe pas à droite
		if (!right.isFile(subpath)) {
			
			operations.add(new CreateFile(right, subpath, left));
			return;
		}
		
		if (createOnly) return;
		
		/*boolean changed = false;
		
		// Les deux fichiers n'ont pas la même taille
		if (left.getSize(subpath) != right.getSize(subpath)) {
			changed = true;
		}*/
		
		// Les deux fichiers n'ont pas la même date de modification
		Date dateLeft = left.getModifiedDate(subpath);
		Date dateRight = right.getModifiedDate(subpath);
		
//		System.out.println(subpath);
//		System.out.println("Left : " + dateLeft + " (" + left.getTimeZone().getID() + ")");
//		Calendar l = Calendar.getInstance(left.getTimeZone());
//		l.setTime(dateLeft);
//		l.setTimeZone(TimeZone.getDefault());
//		dateLeft = l.getTime();
//		System.out.println("Left : " + dateLeft + " (Europe/Paris)");
//		System.out.println("Right: " + dateRight + " (" + right.getTimeZone().getID() + ")");
//		l = Calendar.getInstance(right.getTimeZone());
//		l.setTime(dateRight);
//		l.setTimeZone(TimeZone.getDefault());
//		dateRight = l.getTime();
//		System.out.println("Right: " + dateRight + " (Europe/Paris)");
		
//		System.out.println(dateLeft + " against " + dateRight);
//		System.out.println(dateLeft.getTime() + " against " + dateRight.getTime());

		// Si la modification est plus récente à gauche
		if (dateLeft.after(dateRight)) {
//			System.out.println(dateLeft + " (+) against " + dateRight);
			operations.add(new ChangeFile(right, subpath, left));
		}
		else if (dateLeft.before(dateRight)) {
//			System.out.println(dateLeft + " against " + dateRight + " (+)");
			operations.add(new ChangeFile(left, subpath, right));
		}
	}

	protected void handleDirectory(List<SynchOperation> operations,
			SynchDirectory left, SynchDirectory right, final String subpath, boolean createOnly) {
		
		// On regarde s'il n'existe pas à droite
		if (!right.isDirectory(subpath)) {
			
			// On regarde quand le dossier a été créé à gauche
			Date dateLeft = left.getCreatedDate(subpath);
			
			// On regarde quand le dossier a été supprimé à droite.
			Date dateRight = right.getDeletedDate(subpath);
			
			// Si on a les deux dates
			if (dateLeft != null && dateRight != null && !createOnly) {
				
				// Si la suppression a été faite après la création du dossier,
				// on applique la suppression à gauche
				if (dateLeft.after(dateRight)) {
					operations.add(new DeleteFolder(left, subpath));
					return;
				}
			
			}
			
			// Sinon, on fabrique le dossier à droite
			operations.add(new CreateFolder(right, subpath));
			
		}
		
		// Récursivité
		detectChanges(subpath, left, right, operations, createOnly);
	}

	public void init() throws Exception {
		_right.open();
		_left.open();
	}

	public void close() {
		try {
			_right.close();
			_right = null;
			_left.close();
			_left = null;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "FileSynchManager[left=" + _left + "; right=" + _right + "]";
	}

	public boolean isReady() {
		return _left.isReady() && _right.isReady();
	}
	
}
