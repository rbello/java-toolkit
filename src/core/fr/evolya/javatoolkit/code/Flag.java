package fr.evolya.javatoolkit.code;

import fr.evolya.javatoolkit.code.annotations.ToOverride;

public class Flag {

	protected int _flags;
	
	public Flag() {
		_flags = 0;
	}
	
	public Flag(int flags) {
		_flags = flags;
	}

	public int get() {
		return _flags;
	}
	
	public boolean has(int flag) {
		return (_flags & flag) == flag;
	}
	
	public void add(int flag) {
		set(_flags | flag);
	}
	
	public void remove(int flag) {
		set(_flags ^ flag);
	}

	public void set(int newFlags) {
		onChange(new Flag(newFlags));
		_flags = newFlags;
	}

	@ToOverride
	protected void onChange(Flag newFlags) {
	}
	
}
