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

package com.winterhavenmc.deathchest.storage;


/**
 * An abstract class that declares methods for managing persistent storage of death chests and chest blocks.
 */
public abstract class DataStoreAbstract implements DataStore {

	protected boolean initialized;

	protected DataStoreType type;


	/**
	 * Check if the datastore is initialized
	 *
	 * @return {@code true} if the datastore is initialized, {@code false} if it is not
	 */
	public boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set datastore initialized value
	 *
	 * @param initialized the boolean value to assign to the datastore initialized field
	 */
	public void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}


	/**
	 * Get the datastore type
	 *
	 * @return the datastore type of this datastore instance
	 */
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Override toString method to return the datastore type name
	 *
	 * @return the name of this datastore instance
	 */
	@Override
	public String toString() {
		return this.type.toString();
	}

}
