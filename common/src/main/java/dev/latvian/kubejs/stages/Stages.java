package dev.latvian.kubejs.stages;

import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.core.PlayerKJS;
import dev.latvian.kubejs.net.KubeJSNet;
import dev.latvian.kubejs.net.MessageAddStage;
import dev.latvian.kubejs.net.MessageRemoveStage;
import dev.latvian.kubejs.net.MessageSyncStages;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;
import me.shedaniel.architectury.hooks.PlayerHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public abstract class Stages {
	// TODO: Sync initial stages

	private static final Event<Consumer<StageCreationEvent>> OVERRIDE_CREATION = EventFactory.createConsumerLoop();
	private static final Event<Consumer<StageChangeEvent>> ADDED = EventFactory.createConsumerLoop();
	private static final Event<Consumer<StageChangeEvent>> REMOVED = EventFactory.createConsumerLoop();

	private static Stages createEntityStages(Player player) {
		if (PlayerHooks.isFake(player) || KubeJS.PROXY.isClientButNotSelf(player)) {
			return NoStages.NULL_INSTANCE;
		}

		StageCreationEvent event = new StageCreationEvent(player);
		OVERRIDE_CREATION.invoker().accept(event);

		if (event.getPlayerStages() != null) {
			return event.getPlayerStages();
		}

		return new TagWrapperStages(player);
	}

	public static void overrideCreation(Consumer<StageCreationEvent> event) {
		OVERRIDE_CREATION.register(event);
	}

	public static void added(Consumer<StageChangeEvent> event) {
		ADDED.register(event);
	}

	public static void invokeAdded(Stages stages, String stage) {
		ADDED.invoker().accept(new StageChangeEvent(stages, stage));
	}

	public static void removed(Consumer<StageChangeEvent> event) {
		REMOVED.register(event);
	}

	public static void invokeRemoved(Stages stages, String stage) {
		REMOVED.invoker().accept(new StageChangeEvent(stages, stage));
	}

	public static Stages get(@Nullable Player player) {
		if (player == null) {
			return NoStages.NULL_INSTANCE;
		}

		Stages stages = ((PlayerKJS) player).getStagesRawKJS();

		if (stages == null) {
			stages = createEntityStages(player);
			((PlayerKJS) player).setStagesKJS(stages);
		}

		return stages;
	}

	// End of static //

	public final Player player;

	public Stages(Player p) {
		player = p;
	}

	public abstract boolean addNoUpdate(String stage);

	public abstract boolean removeNoUpdate(String stage);

	public abstract Collection<String> getAll();

	public boolean has(String stage) {
		return getAll().contains(stage);
	}

	@Deprecated
	public final Collection<String> getList() {
		return getAll();
	}

	public boolean add(String stage) {
		if (addNoUpdate(stage)) {
			if (player instanceof ServerPlayer) {
				KubeJSNet.MAIN.sendToPlayers(((ServerPlayer) player).server.getPlayerList().getPlayers(), new MessageAddStage(player.getUUID(), stage));
			}

			invokeAdded(this, stage);
			return true;
		}

		return false;
	}

	public boolean remove(String stage) {
		if (removeNoUpdate(stage)) {
			if (player instanceof ServerPlayer) {
				KubeJSNet.MAIN.sendToPlayers(((ServerPlayer) player).server.getPlayerList().getPlayers(), new MessageRemoveStage(player.getUUID(), stage));
			}

			invokeRemoved(this, stage);
			return true;
		}

		return false;
	}

	public final boolean set(String stage, boolean enabled) {
		return enabled ? add(stage) : remove(stage);
	}

	public final boolean toggle(String stage) {
		return set(stage, !has(stage));
	}

	public boolean clear() {
		Collection<String> all = getAll();

		if (all.isEmpty()) {
			return false;
		}

		for (String s : new ArrayList<>(all)) {
			remove(s);
		}

		return true;
	}

	public void sync() {
		if (player instanceof ServerPlayer) {
			KubeJSNet.MAIN.sendToPlayers(((ServerPlayer) player).server.getPlayerList().getPlayers(), new MessageSyncStages(player.getUUID(), getAll()));
		}
	}

	public void replace(Collection<String> stages) {
		Collection<String> all = getAll();

		for (String s : new ArrayList<>(all)) {
			removeNoUpdate(s);
		}

		for (String s : stages) {
			addNoUpdate(s);
		}
	}
}
