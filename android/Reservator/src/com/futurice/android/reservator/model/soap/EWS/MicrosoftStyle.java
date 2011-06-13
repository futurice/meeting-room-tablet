package com.futurice.android.reservator.model.soap.EWS;

import org.simpleframework.xml.stream.CamelCaseStyle;

public class MicrosoftStyle extends CamelCaseStyle {
	public String getAttribute(String name) {
		return getElement(name);
	}
}
