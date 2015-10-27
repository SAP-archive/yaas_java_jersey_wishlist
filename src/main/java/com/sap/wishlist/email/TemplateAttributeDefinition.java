package com.sap.wishlist.email;

/**
 * Represents email template attribute definition. Email template attribute is a
 * placeholder that is filled with some concrete value when sending email.
 *
 * @see TemplateAttributeValue
 */
public class TemplateAttributeDefinition {
	private String key;
	private Boolean mandatory;

	public TemplateAttributeDefinition() {
		// default
	}

	public TemplateAttributeDefinition(final String key, final boolean mandatory) {
		this.key = key;
		this.mandatory = mandatory;
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(final Boolean mandatory) {
		this.mandatory = mandatory;
	}
}