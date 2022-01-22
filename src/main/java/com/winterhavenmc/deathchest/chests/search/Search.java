package com.winterhavenmc.deathchest.chests.search;

public interface Search {

	/**
	 * Execute search algorithm
	 */
	void execute();


	/**
	 * Get search result
	 *
	 * @return SearchResult object
	 */
	SearchResult getSearchResult();

}
