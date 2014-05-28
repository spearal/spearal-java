package org.spearal.java.test;

public class BooleanBeanEvolved {

	private boolean female;
	private boolean old;
	private boolean tall;
	private boolean fat;
	
	public BooleanBeanEvolved() {
	}
	
	public BooleanBeanEvolved(boolean female, boolean old, boolean tall, boolean fat) {
		this.female = female;
		this.old = old;
		this.tall = tall;
		this.fat = fat;
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

	public boolean isFat() {
		return fat;
	}

	public void setFat(boolean fat) {
		this.fat = fat;
	}

	@Override
	public String toString() {
		return getClass().getName() + " {" +
			"\n  female: " + female +
			"\n  old: " + old +
			"\n  tall: " + tall +
			"\n  fat: " + fat +
		"\n}";
	}
}
