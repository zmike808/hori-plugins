package net.runelite.client.plugins.basicapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;
import net.runelite.client.plugins.iutils.MenuUtils;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.iUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class PrayerUtils {

    @Inject
    private Client client;

    @Inject
    private MenuUtils menu;

    @Inject
    private iUtils utils;

    @Inject
    private Game game;

    public int getRemainingPoints(){
        return client.getBoostedSkillLevel(Skill.PRAYER);
    }

    public int getTotalPoints(){
        return client.getRealSkillLevel(Skill.PRAYER);
    }

    public boolean isActive(Prayer prayer) {
        return client.getVarbitValue(prayer.getVarbit()) == 1;
    }

    public boolean isQuickPrayerActive() {
        return client.getVarbitValue(Varbits.QUICK_PRAYER) == 1;
    }

    public void toggleQuickPrayer(boolean invokes, boolean active, long timeToDelay){
        if((active && isQuickPrayerActive()) || !active && !isQuickPrayerActive()) return;

        Widget widget = client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
        if(widget != null){
            LegacyMenuEntry prayerToggle = new LegacyMenuEntry(active ? "Activate" : "Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
            Rectangle bounds = widget.getBounds();
            if(invokes){
                utils.doInvokeMsTime(prayerToggle, timeToDelay);
            }else{
                utils.doActionMsTime(prayerToggle, bounds, timeToDelay);
            }
        }
        log.info("toggleQuickPray");
    }

    public void togglePrayer(boolean invokes, boolean active, Prayer prayer, long timeToDelay){
        if((active && isActive(prayer)) || !active && !isActive(prayer)) return;
        Widget widget = client.getWidget(prayer.getWidgetInfo());
        if(widget != null){
            LegacyMenuEntry prayerToggle = new LegacyMenuEntry(active ? "Activate" : "Deactivate", "", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
            Rectangle bounds = widget.getBounds();
            if(invokes){
                utils.doInvokeMsTime(prayerToggle, timeToDelay);
            }else{
                utils.doActionMsTime(prayerToggle, bounds, timeToDelay);
            }
        }
    }

    public List<Prayer> getActivePrayers(){
        List<Prayer> values = new ArrayList<>();
        for(Prayer prayer : Prayer.values()){
            if(client.getVarbitValue(prayer.getVarbit()) == 1)
                values.add(prayer);
        }
        return values;
    }

    public boolean isUnlocked(Prayer prayer){
        switch (prayer){
            case PIETY:
                return client.getVarbitValue(Varbits.CAMELOT_TRAINING_ROOM_STATUS) == 8;
            case RIGOUR:
                return client.getVarbitValue(Varbits.RIGOUR_UNLOCKED) == 1;
            case AUGURY:
                return client.getVarbitValue(Varbits.AUGURY_UNLOCKED) == 1;
            case PRESERVE:
                return client.getVarbitValue(Varbits.PRESERVE_UNLOCKED) == 1;
        }
        return true;
    }



}
