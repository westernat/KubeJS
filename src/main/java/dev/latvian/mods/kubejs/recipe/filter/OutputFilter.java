package dev.latvian.mods.kubejs.recipe.filter;

import dev.latvian.mods.kubejs.core.RecipeLikeKJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import net.minecraft.core.HolderLookup;

public class OutputFilter implements RecipeFilter {
	private final ReplacementMatch match;

	public OutputFilter(ReplacementMatch match) {
		this.match = match;
	}

	@Override
	public boolean test(HolderLookup.Provider registries, RecipeLikeKJS r) {
		return r.hasOutput(registries, match);
	}

	@Override
	public String toString() {
		return "OutputFilter{" + match + '}';
	}
}
