package net.runelite.client.plugins.basicapi.utils;


import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.basicapi.BasicApiPlugin;
import net.runelite.client.plugins.basicapi.Runes;
import net.runelite.client.plugins.basicapi.api.Items;
import net.runelite.client.plugins.basicapi.api.LegacyInventoryAssistant;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;
import net.runelite.client.plugins.iutils.iUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class Inventory extends Items {

    @Inject
    private Client client;

    @Inject
    private LegacyInventoryAssistant legacyInventoryAssistant;

    @Inject
    private iUtils utils;

    @Inject
    private BasicApiPlugin basicApiPlugin;


    protected List<WidgetItem> all(Predicate<WidgetItem> filter){
        List<WidgetItem> items = new ArrayList<>();

        for(WidgetItem item : legacyInventoryAssistant.getWidgetItems()){
            if(filter.test(item)){
                items.add(item);
            }
        }
        return items;
    }

    public List<WidgetItem> getAll(Predicate<WidgetItem> filter){
        return all(filter);
    }

    public List<WidgetItem> getAll(){
        return all(x -> true);
    }

    public List<WidgetItem> getAll(int... ids){
        return all(client, ids);
    }

    public List<WidgetItem> getAll(String... names){
        return all(client, names);
    }

    public WidgetItem getFirst(Predicate<WidgetItem> filter){
        return first(filter);
    }

    public WidgetItem getFirst(int... ids)
    {
        return first(client, ids);
    }

    public WidgetItem getFirst(String... names)
    {
        return first(client, names);
    }

    public boolean contains(Predicate<WidgetItem> filter)
    {
        return exists(filter);
    }

    public boolean contains(int... id)
    {
        return exists(client, id);
    }

    public boolean contains(String... name)
    {
        return exists(client, name);
    }

    public int getCount(boolean stacks, Predicate<WidgetItem> filter)
    {
        return count(stacks, filter);
    }

    public int getCount(boolean stacks, int... ids)
    {
        return count(client, stacks, ids);
    }

    public int getCount(boolean stacks, String... names)
    {
        return count(client, stacks, names);
    }

    public int getCount(Predicate<WidgetItem> filter)
    {
        return count(false, filter);
    }

    public int getCount(int... ids)
    {
        return count(client, false, ids);
    }

    public int getCount(String... names)
    {
        return count(client, false, names);
    }

    public boolean isFull()
    {
        return getFreeSlots() == 0;
    }

    public boolean isEmpty()
    {
        return getFreeSlots() == 28;
    }

    public int getFreeSlots()
    {
        return 28 - getAll().size();
    }

    public boolean runePouchContains(int id, int threshold){
        int[] varArray = {Varbits.RUNE_POUCH_RUNE1,Varbits.RUNE_POUCH_RUNE2,Varbits.RUNE_POUCH_RUNE3};
        for(int i : varArray){
            if(client.getVarbitValue(i) != 0 && Runes.getRune(client.getVarbitValue(i)).getItemId() == id){
                return threshold == -1 || getCount(true, id) >= threshold;
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

    public void interactWithItem(int itemID, long delay, String... option) {
        interactWithItem(itemID, false, delay, option);
    }

    public void interactWithItem(int itemID, boolean forceLeftClick, long delay, String... option) {
        interactWithItem(new int[] {itemID}, forceLeftClick, delay, option);
    }


    public void interactWithItem(int[] itemID, long delay, String... option) {
        interactWithItem(itemID, false, delay, option);
    }

    public void interactWithItem(int[] itemID, boolean forceLeftClick, long delay, String... option) {
        List<Integer> boxedIds = Arrays.stream(itemID).boxed().collect(Collectors.toList());
        LegacyMenuEntry entry = legacyInventoryAssistant.getLegacyMenuEntry(boxedIds, Arrays.asList(option), forceLeftClick);
        if (entry != null) {
            WidgetItem wi = legacyInventoryAssistant.getWidgetItem(boxedIds);
            if (wi != null)
                if(basicApiPlugin.useInvokes())
                    utils.doInvokeMsTime(entry, delay);
                else
                    utils.doActionMsTime(entry, wi.getCanvasBounds(), delay);
        }
    }

    public void useItem(String option, String target, int itemId, MenuAction menu, int index, long delay){
        LegacyMenuEntry entry = new LegacyMenuEntry(option, target, 0, menu, index, WidgetInfo.INVENTORY.getId(), false);

        if(basicApiPlugin.useInvokes())
            utils.doInvokeMsTime(entry, delay);
        else
            utils.doActionMsTime(entry, legacyInventoryAssistant.getWidgetItem(Arrays.asList(itemId)).getCanvasBounds(), delay);
    }

    public void useItemOnItem(int firstItem, int secondItem, long delay){
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(legacyInventoryAssistant.getWidgetItem(Arrays.asList(firstItem)).getIndex());
        client.setSelectedSpellItemId(legacyInventoryAssistant.getWidgetItem(Arrays.asList(firstItem)).getWidget().getItemId());

        useItem("", "", secondItem, MenuAction.WIDGET_TARGET_ON_WIDGET,  legacyInventoryAssistant.getWidgetItem(Arrays.asList(secondItem)).getIndex(),delay);
    }

    public boolean containsAllOf(Collection<Integer> itemIds) {
        return itemIds.stream().allMatch(this::contains);
    }

    public boolean containsExcept(Collection<Integer> itemIds) {
        return getAll().stream().anyMatch(a -> !itemIds.contains(a.getId()));
    }


}
