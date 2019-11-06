/*
 * Erstellt am: 19 Oct 2019 13:00:22
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui.menu;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import discord4j.core.object.util.Permission;

/**
 * @author Jonas Michel
 *
 */
public interface UIFeature {

	String getName();

	Class<? extends UIVisualization> getUIClass();

	List<UIFeature> getChildFeatures();

	default List<UIFeature> getChildFeatures(Set<Permission> permissions) {
		return filter(getChildFeatures(), permissions);
	}

	Set<Permission> getPermissions();

	public interface UIVisualization {

		Class<? extends UIFeature> getFeature();

	}

	public static List<UIFeature> filter(List<UIFeature> features, Set<Permission> permissions) {
		return features.stream().filter(cf -> permissions.containsAll(cf.getPermissions())).collect(Collectors.toList());
	}
}
