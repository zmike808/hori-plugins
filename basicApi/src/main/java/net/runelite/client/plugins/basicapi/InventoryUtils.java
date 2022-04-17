package net.runelite.client.plugins.basicapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.queries.InventoryItemQuery;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.iutils.BankUtils;
import net.runelite.client.plugins.iutils.MenuUtils;
import net.runelite.client.plugins.iutils.MouseUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class InventoryUtils {

    @Inject
    private Client client;

    @Inject
    private MouseUtils mouse;

    @Inject
    private MenuUtils menu;

    @Inject
    private BankUtils bank;

    public boolean isFull() {
        return getEmptySlots() <= 0;
    }

    public boolean isEmpty() {
        return getEmptySlots() >= 28;
    }

    public boolean isOpen() {
        if (client.getWidget(WidgetInfo.INVENTORY) == null) {
            return false;
        }
        return !client.getWidget(WidgetInfo.INVENTORY).isHidden();
    }

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
        }
    }

    public List<WidgetItem> getItems(Collection<Integer> ids) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        List<WidgetItem> matchedItems = new ArrayList<>();

        if (inventoryWidget != null) {
            getAllItems().forEach(a -> {
                if(!matchedItems.contains(a.getId()) && ids.contains(a.getId()))
                    matchedItems.add(a);
            });
        }

        return matchedItems;
    }

    //Requires Inventory visible or returns empty
    public List<WidgetItem> getItems(String itemName) {
        return new InventoryWidgetItemQuery()
                .filter(i -> client.getItemDefinition(i.getId())
                        .getName()
                        .toLowerCase()
                        .contains(itemName))
                .result(client)
                .list;
    }

    public Collection<WidgetItem> getAllItems() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return inventoryWidget.getWidgetItems();
        }
        return null;
    }

    public Collection<Integer> getAllItemIDs() {
        Set<Integer> inventoryIDs = new HashSet<>();
        getAllItems().forEach(a -> {
            if(!inventoryIDs.contains(a.getId()))
                inventoryIDs.add(a.getId());

        });
        return inventoryIDs;
    }

    public List<Item> getAllItemsExcept(List<Integer> exceptIDs) {
        exceptIDs.add(-1); //empty inventory slot
        ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (inventoryContainer != null) {
            Item[] items = inventoryContainer.getItems();
            return Arrays.asList(items).stream().filter(a -> !exceptIDs.contains(a.getId())).collect(Collectors.toList());
        }
        return null;
    }

    public WidgetItem getWidgetItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return getAllItems().stream().filter(a -> a.getId() == id).findFirst().get();
        }
        return null;
    }

    public WidgetItem getWidgetItem(Collection<Integer> ids) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if(inventoryWidget != null){
            return getAllItems().stream().filter(a -> ids.contains(a.getId())).findFirst().get();
        }
        return null;
    }

    public Item getItemExcept(List<Integer> exceptIDs) {
        exceptIDs.add(-1); //empty inventory slot
        ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (inventoryContainer != null) {
            Item[] items = inventoryContainer.getItems();
            List<Item> itemList = new ArrayList<>(Arrays.asList(items));
            itemList.removeIf(item -> exceptIDs.contains(item.getId()));
            return itemList.isEmpty() ? null : itemList.get(0);
        }
        return null;
    }

    public WidgetItem getItemMenu(ItemManager itemManager, String menuOption, int opcode, Collection<Integer> ignoreIDs) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ignoreIDs.contains(item.getId())) {
                    continue;
                }
                String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions) {
                    if (action != null && action.equals(menuOption)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public WidgetItem getItemMenu(Collection<String> menuOptions) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                String[] menuActions = client.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions) {
                    if (action != null && menuOptions.contains(action)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public WidgetItem getWidgetItemMenu(ItemManager itemManager, String menuOption, int opcode) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions) {
                    if (action != null && action.equals(menuOption)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public int getItemCount(int id, boolean stackable) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    if (stackable) {
                        return item.getQuantity();
                    }
                    total++;
                }
            }
        }
        return total;
    }

    public boolean containsItem(int itemID) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }

        return new InventoryItemQuery(InventoryID.INVENTORY)
                .idEquals(itemID)
                .result(client)
                .size() >= 1;
    }

    public boolean containsItem(String itemName) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }

        WidgetItem inventoryItem = new InventoryWidgetItemQuery()
                .filter(i -> client.getItemDefinition(i.getId())
                        .getName()
                        .toLowerCase()
                        .contains(itemName))
                .result(client)
                .first();

        return inventoryItem != null;
    }

    public boolean containsStackAmount(int itemID, int minStackAmount) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        Item item = new InventoryItemQuery(InventoryID.INVENTORY)
                .idEquals(itemID)
                .result(client)
                .first();

        return item != null && item.getQuantity() >= minStackAmount;
    }

    public boolean containsItemAmount(int id, int amount, boolean stackable, boolean exactAmount) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    if (stackable) {
                        total = item.getQuantity();
                        break;
                    }
                    total++;
                }
            }
        }
        return (!exactAmount || total == amount) && (total >= amount);
    }

    public boolean containsItemAmount(Collection<Integer> ids, int amount, boolean stackable, boolean exactAmount) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ids.contains(item.getId())) {
                    if (stackable) {
                        total = item.getQuantity();
                        break;
                    }
                    total++;
                }
            }
        }
        return (!exactAmount || total == amount) && (total >= amount);
    }

    public boolean containsItem(Collection<Integer> itemIds) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        return getItems(itemIds).size() > 0;
    }

    public boolean containsAllOf(Collection<Integer> itemIds) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        for (int item : itemIds) {
            if (!containsItem(item)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsExcept(Collection<Integer> itemIds) {
        if (client.getItemContainer(InventoryID.INVENTORY) == null) {
            return false;
        }
        Collection<WidgetItem> inventoryItems = getAllItems();
        List<Integer> depositedItems = new ArrayList<>();

        for (WidgetItem item : inventoryItems) {
            if (!itemIds.contains(item.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean runePouchContains(int id, int threshold){
        int[] varArray = {Varbits.RUNE_POUCH_RUNE1,Varbits.RUNE_POUCH_RUNE2,Varbits.RUNE_POUCH_RUNE3};
        for(int i : varArray){
            if(client.getVarbitValue(i) != 0 && Runes.getRune(client.getVarbitValue(i)).getItemId() == id){
                return threshold == -1 || getItemCount(id, true) >= threshold;
            }
        }
        return false;
    }

    public boolean runePouchContains(Collection<Integer> ids) {
        for (int runeId : ids) {
            if (!runePouchContains(runeId, -1)) {
                return false;
            }
        }
        return true;
    }

    public int runePouchQuanitity(int id) {
        int[] varArray = {Varbits.RUNE_POUCH_RUNE1,Varbits.RUNE_POUCH_RUNE2,Varbits.RUNE_POUCH_RUNE3};
        int[] varArray2 = {Varbits.RUNE_POUCH_AMOUNT1,Varbits.RUNE_POUCH_AMOUNT2,Varbits.RUNE_POUCH_AMOUNT3};
        for(int i = 0; i < varArray.length; i++){
            if(client.getVarbitValue(varArray[i]) != 0 && Runes.getRune(client.getVarbitValue(varArray[i])).getItemId() == id){
                return client.getVarbitValue(varArray2[i]);
            }
        }
        return 0;
    }



}
