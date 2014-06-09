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
public class ParentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private int parentIntProperty;
	private String parentStringProperty;
	
	public ParentBean() {
	}

	public ParentBean(int parentIntProperty, String parentStringProperty) {
		this.parentIntProperty = parentIntProperty;
		this.parentStringProperty = parentStringProperty;
	}

	public int getParentIntProperty() {
		return parentIntProperty;
	}

	public void setParentIntProperty(int parentIntProperty) {
		this.parentIntProperty = parentIntProperty;
	}

	public String getParentStringProperty() {
		return parentStringProperty;
	}

	public void setParentStringProperty(String parentStringProperty) {
		this.parentStringProperty = parentStringProperty;
	}

	@Override
	public int hashCode() {
		return (parentIntProperty + (parentStringProperty == null ? 0 : parentStringProperty.hashCode()));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ParentBean))
			return false;
		ParentBean that = (ParentBean)obj;
		return (
			parentIntProperty == that.parentIntProperty &&
			(parentStringProperty == that.parentStringProperty ||
				(parentStringProperty != null && parentStringProperty.equals(that.parentStringProperty)))
		);
	}
}
