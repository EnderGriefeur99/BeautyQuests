package fr.skytasul.quests.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreationRunnables;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.api.stages.StageCreator;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.NPCStarter;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;
import net.citizensnpcs.api.npc.NPC;

public class QuestsAPI {
	
	/**
	 * Register new stage type into the plugin
	 * @param type StageType object
	 * @param item ItemStack shown in stages GUI when choosing stage type
	 * @param runnables Instance of special runnables
	 */
	public static <T extends AbstractStage> void registerStage(StageType type, ItemStack item, StageCreationRunnables<T> runnables) {
		if (type.dependCode != null) if (!Bukkit.getPluginManager().isPluginEnabled((type.dependCode))){
			BeautyQuests.getInstance().getLogger().warning("Plugin " + type.dependCode + " not enabled. Stage injecting interrupted.");
			return;
		}
		StageType.types.add(type);
		StageCreator.creators.add(new StageCreator<T>(type, item, runnables));
		DebugUtils.logMessage("Stage registered (" + type.name + ", " + (StageCreator.creators.size()-1) + ")");
	}
	
	/**
	 * Register new requirement type into the plugin
	 * @param clazz Class who extends AbstractRequirement
	 * @param item ItemStack shown in requirements GUI
	 * @param runnables Instance of special runnables
	 */
	public static <T extends AbstractRequirement> void registerRequirement(Class<T> clazz, ItemStack item, RequirementCreationRunnables<T> runnables) {
		RequirementCreator.creators.add(new RequirementCreator<T>(clazz, item, runnables));
		DebugUtils.logMessage("Requirement registered (class: " + clazz.getSimpleName() + ")");
	}
	
	/**
	 * Register new reward type into the plugin
	 * @param clazz Class who extends AbstractReward
	 * @param item ItemStack shown in rewards GUI
	 * @param runnables Instance of special runnables
	 */
	public static <T extends AbstractReward> void registerReward(Class<T> clazz, ItemStack item, RewardCreationRunnables<T> runnables) {
		RewardCreator.creators.add(new RewardCreator<T>(clazz, item, runnables));
		DebugUtils.logMessage("Reward registered (class: " + clazz.getSimpleName() + ")");
	}
	
	public static void registerMobFactory(MobFactory<?> factory) {
		MobFactory.factories.add(factory);
		Bukkit.getPluginManager().registerEvents(factory, BeautyQuests.getInstance());
		DebugUtils.logMessage("Mob factory registered (id: " + factory.getID() + ")");
	}
	

	public static List<Quest> getQuestsStarteds(PlayerAccount acc){
		return getQuestsStarteds(acc, false);
	}

	public static List<Quest> getQuestsStarteds(PlayerAccount acc, boolean withoutScoreboard){
		List<Quest> launched = new ArrayList<>();
		for (Quest qu : BeautyQuests.getInstance().getQuests()){
			if (qu.hasStarted(acc) && (withoutScoreboard ? qu.isScoreboardEnabled() : true)) launched.add(qu);
		}
		return launched;
	}

	public static void updateQuestsStarteds(PlayerAccount acc, boolean withoutScoreboard, List<Quest> list) {
		for (Quest qu : BeautyQuests.getInstance().getQuests()) {
			boolean contains = list.contains(qu);
			if (qu.hasStarted(acc) && (withoutScoreboard ? qu.isScoreboardEnabled() : true)) {
				if (!list.contains(qu)) list.add(qu);
			}else if (contains) list.remove(qu);
		}
	}

	public static int getStartedSize(PlayerAccount acc){
		int i = 0;
		for (Quest qu : BeautyQuests.getInstance().getQuests()){
			if (qu.canBypassLimit()) continue;
			if (qu.hasStarted(acc)) i++;
		}
		return i;
	}

	public static List<Quest> getQuestsFinished(PlayerAccount acc){
		List<Quest> finished = new ArrayList<>();
		for (Quest qu : BeautyQuests.getInstance().getQuests()){
			if (qu.hasFinished(acc)) finished.add(qu);
		}
		return finished;
	}

	public static List<Quest> getQuestsUnstarted(PlayerAccount acc, boolean hide){
		List<Quest> finished = new ArrayList<>();
		for (Quest qu : BeautyQuests.getInstance().getQuests()){
			if (hide && qu.isHid()) continue;
			if (!qu.hasFinished(acc) && !qu.hasStarted(acc)) finished.add(qu);
		}
		return finished;
	}

	public static List<Quest> getQuestsAssigneds(NPC npc) {
		NPCStarter starter = BeautyQuests.getInstance().getNPCs().get(npc);
		return starter == null ? Collections.emptyList() : new ArrayList<>(starter.getQuests());
	}
	
	public static boolean isQuestStarter(NPC npc){
		return BeautyQuests.getInstance().getNPCs().containsKey(npc);
	}

	public static boolean hasQuestStarted(Player p, NPC npc){
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		for (Quest qu : getQuestsAssigneds(npc)){
			if (qu.hasStarted(acc)) return true;
		}
		return false;
	}

	public static Quest getQuestFromID(int id){
		return BeautyQuests.getInstance().getQuests().stream().filter(x -> x.getID() == id).findFirst().orElse(null);
	}
	
	public static List<Quest> getQuests(){
		return BeautyQuests.getInstance().getQuests();
	}
	
}
