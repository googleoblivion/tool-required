package com.unmoon;

import com.google.common.collect.Sets;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Menu;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.cluescrolls.clues.item.AnyRequirementCollection;

import javax.inject.Inject;
import java.util.Set;

import static net.runelite.api.MenuAction.GAME_OBJECT_FIRST_OPTION;
import static net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirements.any;
import static net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirements.item;
import static net.runelite.client.util.Text.removeTags;

@Slf4j
@PluginDescriptor(
	name = "Tool Required"
)
public class ToolRequiredPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ToolRequiredConfig config;

	@Provides
	ToolRequiredConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ToolRequiredConfig.class);
	}

	@Getter
	private Item[] equippedItems = new Item[0];

	@Getter
	private Item[] inventoryItems = new Item[0];

	@Getter
	private Item[] playerItems = new Item[0];

	private boolean hasStoredCanoeAxe()
	{
		final ItemContainer canoeAxeContainer = client.getItemContainer(InventoryID.CANOE_AXE);
		if (canoeAxeContainer == null)
		{
			return false;
		}

		for (Item item : canoeAxeContainer.getItems())
		{
			if (item != null && item.getId() > 0 && item.getQuantity() > 0)
			{
				return true;
			}
		}

		return false;
	}

	private boolean isCanoeStation(String target)
	{
		return "Canoe Station".equalsIgnoreCase(target);
	}

	private boolean hasAxeForTarget(String target)
	{
		return ANY_AXE.fulfilledBy(playerItems)
			|| isCanoeStation(target) && hasStoredCanoeAxe();
	}

	private static final AnyRequirementCollection ANY_AXE = any("Any Axe",
			item(ItemID.IRON_AXE),
			item(ItemID.BRONZE_AXE),
			item(ItemID.STEEL_AXE),
			item(ItemID.MITHRIL_AXE),
			item(ItemID.ADAMANT_AXE),
			item(ItemID.RUNE_AXE),
			item(ItemID.BLACK_AXE),
			item(ItemID.DRAGON_AXE),
			item(ItemID.ANMA_AXE),
			item(ItemID.INFERNAL_AXE),
			item(ItemID.INFERNAL_AXE_EMPTY),
			item(ItemID._3A_AXE),
			item(ItemID.TRAIL_GILDED_AXE),
			item(ItemID.CRYSTAL_AXE),
			item(ItemID.CRYSTAL_AXE_INACTIVE),
			item(ItemID.GAUNTLET_AXE_HM),
			item(ItemID.GAUNTLET_AXE),
			item(ItemID.TRAILBLAZER_AXE),
			item(ItemID.LEAGUE_TRAILBLAZER_AXE),
			item(ItemID.TRAILBLAZER_AXE_EMPTY),
			item(ItemID.TRAILBLAZER_AXE_NO_INFERNAL),
			item(ItemID.BRONZE_AXE_2H),
			item(ItemID.IRON_AXE_2H),
			item(ItemID.STEEL_AXE_2H),
			item(ItemID.BLACK_AXE_2H),
			item(ItemID.MITHRIL_AXE_2H),
			item(ItemID.ADAMANT_AXE_2H),
			item(ItemID.RUNE_AXE_2H),
			item(ItemID.DRAGON_AXE_2H),
			item(ItemID.CRYSTAL_AXE_2H),
			item(ItemID.CRYSTAL_AXE_2H_INACTIVE),
			item(ItemID._3A_AXE_2H),
			item(ItemID.TRAILBLAZER_RELOADED_AXE),
			item(ItemID.TRAILBLAZER_RELOADED_AXE_EMPTY),
			item(ItemID.TRAILBLAZER_RELOADED_AXE_NO_INFERNAL)
	);

	private static final AnyRequirementCollection ANY_PICKAXE = any("Any Pickaxe",
			item(ItemID.BRONZE_PICKAXE),
			item(ItemID.IRON_PICKAXE),
			item(ItemID.STEEL_PICKAXE),
			item(ItemID.ADAMANT_PICKAXE),
			item(ItemID.MITHRIL_PICKAXE),
			item(ItemID.RUNE_PICKAXE),
			item(ItemID.NZONE_RUNE_PICKAXE),
			item(ItemID.NZONE_MITHRIL_PICKAXE),
			item(ItemID.NZONE_IRON_PICKAXE),
			item(ItemID.DRAGON_PICKAXE),
			item(ItemID.BLACK_PICKAXE),
			item(ItemID.DRAGON_PICKAXE_PRETTY),
			item(ItemID.INFERNAL_PICKAXE),
			item(ItemID.INFERNAL_PICKAXE_EMPTY),
			item(ItemID._3A_PICKAXE),
			item(ItemID.TRAIL_GILDED_PICKAXE),
			item(ItemID.ZALCANO_PICKAXE),
			item(ItemID.CRYSTAL_PICKAXE),
			item(ItemID.CRYSTAL_PICKAXE_INACTIVE),
			item(ItemID.GAUNTLET_PICKAXE_HM),
			item(ItemID.GAUNTLET_PICKAXE),
			item(ItemID.TRAILBLAZER_PICKAXE),
			item(ItemID.LEAGUE_TRAILBLAZER_PICKAXE),
			item(ItemID.TRAILBLAZER_PICKAXE_EMPTY),
			item(ItemID.TRAILBLAZER_PICKAXE_NO_INFERNAL),
			item(ItemID.TRAILBLAZER_RELOADED_PICKAXE),
			item(ItemID.TRAILBLAZER_RELOADED_PICKAXE_EMPTY),
			item(ItemID.TRAILBLAZER_RELOADED_PICKAXE_NO_INFERNAL)
	);
	private final Set<String> cutOverrides = Sets.newHashSet("Sulliuscep");
	private final Set<String> chopOverrides = Sets.newHashSet(
			"Jungle Bush", "Pineapple plant", "Canoe Station", "Vines", "Tendrils", "Bruma roots",
			"Rotten sapling", "Sapling", "Thick vine", "Thick vines", "Corrupt Phren Roots", "Phren Roots"
	);

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event)
	{
		final ItemContainer itemContainer = event.getItemContainer();
		if (event.getContainerId() == InventoryID.WORN)
		{
			equippedItems = itemContainer.getItems();
			playerItems = new Item[equippedItems.length + inventoryItems.length];
			System.arraycopy(equippedItems, 0, playerItems, 0, equippedItems.length);
			System.arraycopy(inventoryItems, 0, playerItems, equippedItems.length, inventoryItems.length);
		}
		else if (event.getContainerId() == InventoryID.INV) {
			inventoryItems = itemContainer.getItems();
			playerItems = new Item[equippedItems.length + inventoryItems.length];
			System.arraycopy(equippedItems, 0, playerItems, 0, equippedItems.length);
			System.arraycopy(inventoryItems, 0, playerItems, equippedItems.length, inventoryItems.length);
		}
	}

	@Subscribe
	public void onPostMenuSort(PostMenuSort postMenuSort)
	{
		if (client.isMenuOpen()) {return;}

		Menu root = client.getMenu();
		MenuEntry[] entries = root.getMenuEntries();

		for (MenuEntry entry : entries) {
			if (entry.getType() != GAME_OBJECT_FIRST_OPTION) {continue;}
			if (config.chopDown()) {
				String target = removeTags(entry.getTarget());
				boolean isTreeTarget = target.contains("ree") || chopOverrides.contains(target) || isCanoeStation(target);
				// target.contains("ree") is Tree check (without the T because of case-sensitive)
				if (entry.getOption().startsWith("Chop")
					&& isTreeTarget
					&& !hasAxeForTarget(target)) {
					root.removeMenuEntry(entry);
					// removeMenuEntry will set `entry` to a different, potentially to-be-removed option,
					// but we need to continue iteration instead of calling removeMenuEntry again, or we will
					// end up with duplicated menu entries (which is bad).
					continue;
				}
				else if (entry.getOption().startsWith("Cut")
					&& (target.contains("ree") || cutOverrides.contains(target))
					&& !ANY_AXE.fulfilledBy(playerItems)) {
					root.removeMenuEntry(entry);
					// removeMenuEntry will set `entry` to a different, potentially to-be-removed option,
					// but we need to continue iteration instead of calling removeMenuEntry again, or we will
					// end up with duplicated menu entries (which is bad).
					continue;
				}
			}
			if (config.mine() && entry.getOption().equals("Mine") && !ANY_PICKAXE.fulfilledBy(playerItems)) {
				root.removeMenuEntry(entry);
			}
		}
	}
}
