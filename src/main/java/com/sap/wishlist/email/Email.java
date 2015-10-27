package com.sap.wishlist.email;

import java.util.List;

public class Email {
	private String toAddress;
	private String toName;
	private String fromAddress;
	private String templateCode;
	private String templateOwner;
	private String locale;
	protected List<TemplateAttributeValue> attributes;

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(final String toAddress) {
		this.toAddress = toAddress;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(final String toName) {
		this.toName = toName;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(final String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	public String getTemplateOwner() {
		return templateOwner;
	}

	public void setTemplateOwner(String templateOwner) {
		this.templateOwner = templateOwner;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public List<TemplateAttributeValue> getAttributes() {
		return attributes;
	}

	public void setAttributes(final List<TemplateAttributeValue> attributeValues) {
		this.attributes = attributeValues;
	}
}