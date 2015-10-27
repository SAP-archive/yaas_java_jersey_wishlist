package com.sap.wishlist.email;

/**
 * Represents a concrete value the email template is filled with when sending
 * the email. This attribute must be declared as a part of a template.
 *
 * @see TemplateAttributeDefinition
 */
public class TemplateAttributeValue {
	private String key;
	private String value;

	public TemplateAttributeValue() {
		//
	}

	public TemplateAttributeValue(final String key, final String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}