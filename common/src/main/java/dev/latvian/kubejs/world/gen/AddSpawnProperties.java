package dev.latvian.kubejs.world.gen;

import dev.latvian.kubejs.world.gen.filter.BiomeFilter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * @author LatvianModder
 */
public class AddSpawnProperties {
	public MobCategory _category = MobCategory.CREATURE;
	public int weight = 10;
	public EntityType<?> _entity = null;
	public int minCount = 4;
	public int maxCount = 4;
	public BiomeFilter biomes = BiomeFilter.ALWAYS_FALSE;

	public void setCategory(String s) {
		_category = MobCategory.byName(s);
	}

	public void setEntity(String s) {
		_entity = EntityType.byString(s).orElse(null);
	}
}
