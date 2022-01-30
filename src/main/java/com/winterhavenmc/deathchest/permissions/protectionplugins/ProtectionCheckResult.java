/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathchest.permissions.protectionplugins;

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
