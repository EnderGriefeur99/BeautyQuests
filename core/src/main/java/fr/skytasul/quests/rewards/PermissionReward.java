package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.gui.permissions.PermissionListGUI;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.types.Permission;

public class PermissionReward extends AbstractReward {
	
	public final List<Permission> permissions;

	public PermissionReward(){
		this(new ArrayList<>());
	}

	public PermissionReward(List<Permission> permissions) {
		super("permReward");
		if (!DependenciesManager.vault) throw new MissingDependencyException("Vault");
		this.permissions = permissions;
	}

	public String give(Player p){
		for (Permission perm : permissions) {
			perm.give(p);
		}
		return null;
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("perms", Utils.serializeList(permissions, Permission::serialize));
	}

	protected void load(Map<String, Object> savedDatas){
		if (savedDatas.containsKey("perm")) {
			permissions.add(new Permission((String) savedDatas.get("perm"), false, null));
		}else if (savedDatas.containsKey("permissions")) { // TODO remove on 0.19
			Map<String, Boolean> map = (Map<String, Boolean>) savedDatas.get("permissions");
			for (Entry<String, Boolean> en : map.entrySet()) {
				permissions.add(new Permission(en.getKey(), en.getValue(), null));
			}
		}else {
			permissions.addAll(Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("perms"), Permission::deserialize));
		}
	}

	public static class Creator implements RewardCreationRunnables<PermissionReward> {

		public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked) {
			if (!datas.containsKey("permissions")) datas.put("permissions", new ArrayList<>());
			List<Permission> permissions = (List<Permission>) datas.get("permissions");
			new PermissionListGUI(permissions, () -> {
				ItemUtils.lore(clicked, "Permissions : " + permissions.size());
				gui.reopen(p, true);
			}).create(p);
		}

		public void edit(Map<String, Object> datas, PermissionReward reward, ItemStack is) {
			datas.put("permissions", new ArrayList<>(reward.permissions));
			ItemUtils.lore(is, "Permissions : " + reward.permissions.size());
		}

		public PermissionReward finish(Map<String, Object> datas) {
			return new PermissionReward((List<Permission>) datas.get("permissions"));
		}

	}

}
