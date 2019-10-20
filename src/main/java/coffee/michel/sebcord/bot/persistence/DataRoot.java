/*
 * Erstellt am: 18 Oct 2019 22:40:32
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import one.microstream.persistence.lazy.Lazy;

/**
 * @author Jonas Michel
 *
 */
class DataRoot {

	private Map<String, Lazy<Set<String>>> authorziedFeatures = new HashMap<>();

	public Map<String, Lazy<Set<String>>> getAuthorziedFeatures() {
		return authorziedFeatures;
	}

	public void setAuthorziedFeatures(Map<String, Lazy<Set<String>>> authorziedFeatures) {
		this.authorziedFeatures = authorziedFeatures;
	}

}