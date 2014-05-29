package org.spearal.test;

public class BooleanBean {

	private boolean female;
	private boolean old;
	private boolean tall;
	private int value = -23;
	
	public BooleanBean() {
	}
	
	public BooleanBean(boolean female, boolean old, boolean tall) {
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

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return getClass().getName() + " {" +
			"\n  female: " + female +
			"\n  old: " + old +
			"\n  tall: " + tall +
			"\n  value: " + value +
		"\n}";
	}
}
