package org.spearal.test;


public class ClientBooleanBean {
	
	private boolean female;
	private boolean old;
	private boolean tall;
	
	public ClientBooleanBean() {
	}
	
	public ClientBooleanBean(boolean female, boolean old, boolean tall) {
		this.female = female;
		this.old = old;
		this.tall = tall;
	}

	public boolean isFemale() {
		return female;
	}

	public void setFemale(boolean female) {
		this.female = female;
	}

	public boolean isOld() {
		return old;
	}

	public void setOld(boolean old) {
		this.old = old;
	}

	public boolean isTall() {
		return tall;
	}

	public void setTall(boolean tall) {
		this.tall = tall;
	}

	@Override
	public String toString() {
		return getClass().getName() + " {" +
			"\n  female: " + Boolean.valueOf(female) +
			"\n  old: " + Boolean.valueOf(old) +
			"\n  tall: " + Boolean.valueOf(tall) +
		"\n}";
	}
}
