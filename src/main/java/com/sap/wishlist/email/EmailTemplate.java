package com.sap.wishlist.email;

import java.io.InputStream;

public class EmailTemplate {
	private String code;
	private String owner;
	private String fileType;
	private InputStream dataStream;

	private EmailTemplate(Builder builder) {
		this.dataStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(builder.filePath);
		this.code = builder.code;
		this.owner = builder.owner;
		this.fileType = builder.fileType + "_" + builder.locale;
	}

	public String getCode() {
		return code;
	}

	public String getOwner() {
		return owner;
	}

	public String getFileType() {
		return fileType;
	}

	public InputStream getDataStream() {
		return dataStream;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String filePath;
		private String code;
		private String owner;
		private String fileType;
		private String locale;

		public Builder setFilePath(final String filePath) {
			this.filePath = filePath;
			return this;
		}

		public Builder setCode(final String code) {
			this.code = code;
			return this;
		}

		public Builder setOwner(final String owner) {
			this.owner = owner;
			return this;
		}

		public Builder setFileType(final String fileType) {
			this.fileType = fileType;
			return this;
		}

		public Builder setLocale(final String locale) {
			this.locale = locale;
			return this;
		}

		public EmailTemplate build() {
			return new EmailTemplate(this);
		}

	}
}