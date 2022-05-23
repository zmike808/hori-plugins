/*
 * Copyright (c) 2018, Andrew EP | ElPinche256 <https://github.com/ElPinche256>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.cannonHelper;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.client.config.*;

@ConfigGroup("CannonHelperConfig")
public interface CannonHelperConfig extends Config {

    @ConfigSection(
            name = "Sleep Delays",
            description = "",
            position = 1,
            keyName = "sleepDelays"
    )
    String sleepDelays = "Sleep Delays";

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMin",
            name = "Sleep Min",
            description = "",
            position = 2,
            section = sleepDelays
    )
    default int sleepMin() {
        return 60;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMax",
            name = "Sleep Max",
            description = "",
            position = 3,
            section = sleepDelays
    )
    default int sleepMax() {
        return 350;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepTarget",
            name = "Sleep Target",
            description = "",
            position = 4,
            section = sleepDelays
    )
    default int sleepTarget() {
        return 100;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepDeviation",
            name = "Sleep Deviation",
            description = "",
            position = 5,
            section = sleepDelays
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 6,
            section = sleepDelays
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

    @ConfigSection(
            name = "Settings",
            description = "",
            position = 13,
            keyName = "settings"
    )
    String settings = "Settings";

    @ConfigItem(keyName = "setTile", name = "Set Cannon Location", description = "", position = 14, title = "setLocation")
    default Button setLocation() {
        return new Button();
    }

    @ConfigItem(keyName = "setSafeTile", name = "Set Safespot Location", description = "", position = 15, title = "setSafespot")
    default Button setSafespot() {
        return new Button();
    }

    @ConfigItem(keyName = "startHelper", name = "Start/Stop", description = "", position = 16, title = "startHelper")
    default Button startVorkath() {
        return new Button();
    }


    @Range(
            min = 1,
            max = 30
    )
    @ConfigItem(keyName = "minBalls", name = "Min Cannonballs", description = "Minimum cannonballs before refilling", position = 17, title =  "minimumBalls")
    default int minimumBalls() { return 5; }

    @Range(
            min = 1,
            max = 30
    )
    @ConfigItem(keyName = "maxBalls", name = "Max Cannonballs", description = "Maximum cannonballs before refilling", position = 18, title =  "maximumBalls")
    default int maximumBalls() { return 30; }

    @ConfigItem(keyName = "restorePrayer", name = "Use Prayer potions", description = "Restore prayer using potions", position = 19)
    default boolean restorePray() {
        return false;
    }

    @ConfigItem(keyName = "prayerID", name = "Prayer restore", description = "Type of prayer point restore potion", position = 20)
    default Prayer prayer() {
        return Prayer.PRAYER_POTION;
    }

    enum Prayer {
        PRAYER_POTION(ItemID.PRAYER_POTION4, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION4, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION1),
        SUPER_RESTORE(ItemID.SUPER_RESTORE4, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE4, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE1);

        @Getter
        private final int dose4, dose3, dose2, dose1;

        @Getter
        private int[] ids;

        Prayer(int dose4, int dose3, int dose2, int dose1, int... ids) {
            this.dose1 = dose1;
            this.dose2 = dose2;
            this.dose3 = dose3;
            this.dose4 = dose4;
            this.ids = ids;
        }
    }
}