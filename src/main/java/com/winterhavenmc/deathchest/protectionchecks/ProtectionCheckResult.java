package com.winterhavenmc.deathchest.protectionchecks;

public class ProtectionCheckResult {

	ProtectionCheckResultCode resultCode = ProtectionCheckResultCode.ALLOWED;
	ProtectionPlugin protectionPlugin = null;


	public ProtectionCheckResultCode getResultCode() {
		return resultCode;
	}

	public void setResultCode(final ProtectionCheckResultCode resultCode) {
		this.resultCode = resultCode;
	}

	public ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}

	public void setProtectionPlugin(final ProtectionPlugin protectionPlugin) {
		this.protectionPlugin = protectionPlugin;
	}

}
