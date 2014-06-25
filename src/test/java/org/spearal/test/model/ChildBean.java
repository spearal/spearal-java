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

import java.util.Set;

/**
 * @author Franck WOLFF
 */
public class ChildBean extends ParentBean {

	private static final long serialVersionUID = 1L;

	private boolean childBooleanProperty;
	private double childDoubleProperty;
	
	private Set<SimpleBean> simpleBeans;
	
	public ChildBean() {
		//this.simpleBeans = new HashSet<SimpleBean>();
	}

	public ChildBean(int parentIntProperty, String parentStringProperty, boolean childBooleanProperty, double childDoubleProperty) {
		super(parentIntProperty, parentStringProperty);
		
		this.childBooleanProperty = childBooleanProperty;
		this.childDoubleProperty = childDoubleProperty;
		//this.simpleBeans = new HashSet<SimpleBean>();
	}

	public boolean isChildBooleanProperty() {
		return childBooleanProperty;
	}

	public void setChildBooleanProperty(boolean childBooleanProperty) {
		this.childBooleanProperty = childBooleanProperty;
	}

	public double getChildDoubleProperty() {
		return childDoubleProperty;
	}

	public void setChildDoubleProperty(double childDoubleProperty) {
		this.childDoubleProperty = childDoubleProperty;
	}

	public Set<SimpleBean> getSimpleBeans() {
		return simpleBeans;
	}

	public void setSimpleBeans(Set<SimpleBean> simpleBeans) {
		this.simpleBeans = simpleBeans;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + (childBooleanProperty ? 1 : 0) + (int)childDoubleProperty;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ChildBean))
			return false;
		ChildBean that = (ChildBean)obj;
		return (
			super.equals(obj) &&
			childBooleanProperty == that.childBooleanProperty &&
			Double.compare(childDoubleProperty, that.childDoubleProperty) == 0 &&
			simpleBeans == that.simpleBeans || (simpleBeans != null && simpleBeans.equals(that.simpleBeans))
		);
	}
}
