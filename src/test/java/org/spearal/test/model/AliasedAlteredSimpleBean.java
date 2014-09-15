/**
 * == @Spearal ==>
 * 
 * Copyright (C) 2014 Franck WOLFF & William DRAI (http://www.spearal.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spearal.test.model;

import java.io.Serializable;

/**
 * @author Franck WOLFF
 */
public class AliasedAlteredSimpleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean booleanValue;
	private int intValue;
	private double doubleValue;
	private String stringValueBis;
	

	public AliasedAlteredSimpleBean() {
	}

	public AliasedAlteredSimpleBean(boolean booleanValue, int intValue, double doubleValue, String stringValue) {
		this.booleanValue = booleanValue;
		this.intValue = intValue;
		this.doubleValue = doubleValue;
		this.stringValueBis = stringValue;
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public String getStringValueBis() {
		return stringValueBis;
	}

	public void setStringValueBis(String stringValue) {
		this.stringValueBis = stringValue;
	}
	
	@Override
	public int hashCode() {
		return (
			(booleanValue ? 1 : 0) +
			intValue +
			(int)doubleValue +
			(stringValueBis == null ? 0 : stringValueBis.hashCode())
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof AliasedAlteredSimpleBean))
			return false;
		AliasedAlteredSimpleBean that = (AliasedAlteredSimpleBean)obj;
		return (
			booleanValue == that.booleanValue &&
			intValue == that.intValue &&
			Double.compare(doubleValue, that.doubleValue) == 0 &&
			(stringValueBis == that.stringValueBis || (stringValueBis != null && stringValueBis.equals(that.stringValueBis)))
		);
	}
}
