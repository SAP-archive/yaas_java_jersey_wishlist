package com.sap.wishlist.email;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailTemplateDefinition {
	private String code;
	private String owner;
	private String name;
	private String description;

	@JsonProperty("definableAttributes")
	private List<TemplateAttributeDefinition> templateAttributeDefinitions;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public List<TemplateAttributeDefinition> getTemplateAttributeDefinitions() {
		return templateAttributeDefinitions;
	}

	public void setTemplateAttributeDefinitions(
			final List<TemplateAttributeDefinition> templateAttributeDefinitions) {
		this.templateAttributeDefinitions = templateAttributeDefinitions;
	}
}