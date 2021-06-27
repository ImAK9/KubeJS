package dev.latvian.kubejs.stages;

import dev.latvian.kubejs.entity.EntityJS;
import dev.latvian.kubejs.player.PlayerEventJS;

/**
 * @author LatvianModder
 */
public class StageEventJS extends PlayerEventJS {
	private final StageChangeEvent event;

	public StageEventJS(StageChangeEvent e) {
		event = e;
	}

	public Stages getPlayerStages() {
		return event.getPlayerStages();
	}

	@Override
	public EntityJS getEntity() {
		return entityOf(event.getPlayer());
	}

	public String getStage() {
		return event.getStage();
	}
}