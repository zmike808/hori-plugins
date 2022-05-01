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
package net.runelite.client.plugins.vorkathPlayer;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.config.*;

@ConfigGroup("VorkathPlayerConfig")
public interface VorkathPlayerConfig extends Config {

    @ConfigItem(keyName = "startVorkath", name = "Start/Stop", description = "", position = 0, title = "startVorki")
    default Button startVorkath() {
        return new Button();
    }

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
            position = 3,
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
            position = 4,
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
            position = 5,
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
            position = 6,
            section = sleepDelays
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 7,
            section = sleepDelays
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }


    @ConfigSection(
            name = "Gear Setup",
            description = "",
            position = 8,
            keyName = "weaponsSection"
    )
    String weaponsSection = "Weapons";

    @ConfigItem(keyName = "mainhandID", name = "MH", description = "Your main weapon: Use dev tools (cog on side bar) -> inventory to get item id", position = 9, section = weaponsSection)
    default int mainhand() {
        return ItemID.DRAGON_HUNTER_LANCE;
    }

    @ConfigItem(keyName = "offhandID", name = "OH", description = "Your offhand item: Use dev tools (cog on side bar) -> inventory to get item id", position = 10, section = weaponsSection)
    default int offhand() {
        return ItemID.DRAGON_DEFENDER;
    }

    @ConfigItem(keyName = "useSpec", name = "Spec", description = "Which special attack do you want to use", position = 11, section = weaponsSection)
    default Spec useSpec() {
        return Spec.BANDOS_GODSWORD;
    }

    @ConfigItem(keyName = "useStaff", name = "Use Staff for minion", description = "Equip a staff for crumble undead<br>Useful if your magic attack is too low", position = 12, section = weaponsSection)
    default boolean useStaff() {
        return true;
    }

    @ConfigItem(keyName = "staffID", name = "Staff ID", description = "The item ID of your magic weapon", position = 13, hidden = true, unhide = "useStaff", section = weaponsSection)
    default int staffID() {
        return ItemID.SLAYERS_STAFF;
    }

    @ConfigItem(keyName = "useRange", name = "Use Range", description = "Range master switch", position = 14, section = weaponsSection)
    default boolean useRange() {
        return false;
    }

    @ConfigItem(keyName = "useDragonBolts", name = "Use Dragon bolt versions", description = "Use dragon variations of bolts during the kill", position = 15, section = weaponsSection)
    default boolean useDragonBolts() {
        return false;
    }

    @ConfigItem(keyName = "useSwitches", name = "Use Bolt switches", description = "Swaps between ruby and diamond bolts during kill", position = 16, section = weaponsSection)
    default boolean useSwitches() {
        return false;
    }

    @ConfigItem(keyName = "walkMethod", name = "Acid Walk", description = "Acid walk method", position = 17, section = weaponsSection)
    default walkMethod walkMethod() { return walkMethod.WOOX_ACID_MELEE; }


    @ConfigSection(
            name = "Consumables",
            description = "",
            position = 23,
            keyName = "consumablesSection"
    )
    String consumablesSection = "Consumables";

    @ConfigItem(keyName = "foodID", name = "Food", description = "The name of your food", position = 24, section = consumablesSection)
    default Food food() {
        return Food.ANGLERFISH;
    }

    @Range(min = 1, max = 28)
    @ConfigItem(keyName = "withdrawFood", name = "Withdraw food", description = "The amount of food to bring to Vorkath", position = 25, section = consumablesSection)
    default int withdrawFood() {
        return 14;
    }

    @Range(min = 1, max = 14)
    @ConfigItem(keyName = "minFood", name = "Minimum food", description = "Minimum amount of food needed to start next Vorkath kill", position = 26, section = consumablesSection)
    default int minFood() {
        return 3;
    }

    @Range(min = 33, max = 99)
    @ConfigItem(keyName = "eatAt", name = "Eat at", description = "Eat food when under this HP", position = 27, section = consumablesSection)
    default int eatAt() {
        return 39;
    }

    @ConfigItem(keyName = "eatWoox", name = "Eat while 1 tick walking", description = "Will override the eat function to allow eating while 1 tick walking.<br>Best used in combination with invokes", position = 28, section = consumablesSection)
    default boolean eatWoox() {
        return false;
    }

    @ConfigItem(keyName = "prayerID", name = "Prayer restore", description = "Type of prayer point restore potion", position = 29, section = consumablesSection)
    default Prayer prayer() {
        return Prayer.PRAYER_POTION;
    }

    @Range(min = 1, max = 8)
    @ConfigItem(keyName = "prayerAmount", name = "Prayer pots", description = "Quantity of prayer point restores to bring", position = 30, section = consumablesSection)
    default int prayerAmount() {
        return 4;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(keyName = "restoreAt", name = "Drink prayer at", description = "Drink prayer point restores when under this amount of prayer", position = 31, section = consumablesSection)
    default int restoreAt() {
        return 15;
    }

    @Range(min = 0, max = 10)
    @ConfigItem(keyName = "minDoses", name = "Minimum doses", description = "Minimum doses to start a new kill", position = 32, section = consumablesSection)
    default int minDoses() { return 2; }

    @ConfigItem(keyName = "antifireID", name = "Antifire", description = "The name of your Antifire potion", position = 33, section = consumablesSection)
    default Antifire antifire() {
        return Antifire.EXT_SUPER_ANTIFIRE;
    }

    @ConfigItem(keyName = "antivenomID", name = "Antivenom", description = "The name of your Antivenom potion", position = 34, section = consumablesSection)
    default Antivenom antivenom() {
        return Antivenom.SERPENTINE_HELM;
    }

    @ConfigItem(keyName = "superCombatID", name = "Boost", description = "The name of your boost potion", position = 35, section = consumablesSection)
    default BoostPotion boostPotion() {
        return BoostPotion.DIVINE_SUPER_COMBAT;
    }

    @ConfigItem(keyName = "boostLevel", name = "Boost offset", description = "Level to boost at <= base level + offset", position = 36, section = consumablesSection)
    default int boostLevel() {
        return 1;
    }

    @ConfigSection(
            name = "Teleports",
            description = "",
            position = 41,
            keyName = "teleportsSection"
    )
    String teleportsSection = "Teleports";

    @ConfigItem(keyName = "houseTele", name = "PoH", description = "The name of your house teleport", position = 42, section = teleportsSection)
    default HouseTele houseTele() { return HouseTele.HOUSE_TELEPORT; }

    @ConfigItem(keyName = "pouchID", name = "Rune Pouch ID", description = "ID of rune pouch", position = 43, section = teleportsSection)
    default int pouchID() { return ItemID.RUNE_POUCH; }

    @ConfigItem(keyName = "useAltar", name = "Use PoH Altar", description = "Use POH Altar instead of pool", position = 44, section = teleportsSection)
    default boolean useAltar() {
        return false;
    }

    @ConfigItem(keyName = "usePool", name = "Use PoH pool", description = "Use POH rejuvenation pool",hidden = false, hide = "useAltar", position = 45, section = teleportsSection)
    default boolean usePool() {
        return true;
    }

    @ConfigItem(keyName = "rellekkaTele", name = "Rellekka", description = "The method of travelling to Rellekka after banking", position = 46, section = teleportsSection)
    default RellekkaTele rellekkaTeleport() { return RellekkaTele.TALK_TO_BANKER; }

    @ConfigSection(
            name = "Loot",
            description = "",
            position = 50,
            keyName = "lootSection"
    )
    String lootSection = "Loot";

    @ConfigItem(keyName = "lootBones", name = "Loot Superior dragon bones", description = "", position = 51, section = lootSection)
    default boolean lootBones() {
        return true;
    }

    @ConfigItem(keyName = "lootBonesIfRoom", name = "Loot Superior dragon bones only if you have room.", description = "", hidden = true, unhide = "lootBones", position = 52, section = lootSection)
    default boolean lootBonesIfRoom() {
        return false;
    }

    @ConfigItem(keyName = "eatLoot", name = "Loot Prioritization", description = "Prioritizes loot over food.<br>Not to be confused with automatic loot filtering by value!", position = 53, section = lootSection)
    default boolean eatLoot() {
        return true;
    }

    @ConfigItem(keyName = "lootValue", name = "Minimum value of loot", description = "Loot drops over this value.", position = 54, section = lootSection)
    default int lootValue() {
        return 25000;
    }

    @ConfigItem(keyName = "includedItems", name = "Included items", description = "Full or partial names of items to loot regardless of value<br>Separate with a comma.", position = 55, section = lootSection)
    default String includedItems() {
        return "rune longsword,wrath rune";
    }

    @ConfigItem(keyName = "excludedItems", name = "Excluded items", description = "Full or partial names of items not to loot<br>Separate with a comma.", position = 56, section = lootSection)
    default String excludedItems() {
        return "ruby bolt,diamond bolt,emerald bolt,dragonstone bolt";
    }

    @ConfigSection(
            name = "Advanced",
            description = "",
            position = 70,
            keyName = "advanced"
    )
    String advancedSection = "Advanced";

    @ConfigItem(keyName = "invokes", name = "Use invokes*", description = "Increased *speculated* risk, use at your own risk.", position = 71, section = advancedSection)
    default boolean invokes() {
        return false;
    }

    @ConfigItem(keyName = "debug", name = "Debug Messages", description = "", position = 72, section = advancedSection)
    default boolean debug() {
        return false;
    }

    enum Mainhand {
        DRAGON_HUNTER_LANCE(ItemID.DRAGON_HUNTER_LANCE, 1),
        GHRAZI_RAPIER(ItemID.GHRAZI_RAPIER, 1),
        ZAMORAKIAN_HASTA(ItemID.ZAMORAKIAN_HASTA, 1),
        ABYSSAL_DAGGER(ItemID.ABYSSAL_DAGGER, 1),
        LEAF_BLADED_SWORD(ItemID.LEAFBLADED_SWORD, 1),
        DRAGON_HUNTER_CROSSBOW(ItemID.DRAGON_HUNTER_CROSSBOW, 7),
        DRAGON_CROSSBOW(ItemID.DRAGON_CROSSBOW, 7),
        RUNE_CROSSBOW(ItemID.RUNE_CROSSBOW, 7),
        BLOWPIPE(ItemID.TOXIC_BLOWPIPE, 5);

        @Getter private final int itemId, range;
        Mainhand (int itemId, int range) {
            this.itemId = itemId;
            this.range = range;
        }
    }

    enum Offhand {
        AVERNIC_DEFENDER(ItemID.AVERNIC_DEFENDER),
        DRAGON_DEFENDER(ItemID.DRAGON_DEFENDER),
        DRAGONFIRE_SHIELD(ItemID.DRAGONFIRE_SHIELD_11284),
        TOKTZ_KET_XIL(ItemID.TOKTZKETXIL),
        RUNE_DEFENDER(ItemID.RUNE_DEFENDER),
        DRAGONFIRE_WARD(ItemID.DRAGONFIRE_WARD),
        ANTI_DRAGON_SHIELD(ItemID.ANTIDRAGON_SHIELD),
        TWISTED_BUCKLER(ItemID.TWISTED_BUCKLER),
        NONE(-1);

        @Getter private final int itemId;
        Offhand(int itemId) {
            this.itemId = itemId;
        }
    }

    enum RellekkaTele {
        TALK_TO_BANKER(0),
        FREMENNIK_BOOTS_4(1),
        RETURN_ORB(29712);

        @Getter private final int option;
        RellekkaTele(int option) { this.option = option; }
    }

    enum Spec {
        NONE(-1, 0, 0),
        BANDOS_GODSWORD(ItemID.BANDOS_GODSWORD, 50, 2),
        DRAGON_WARHAMMER(ItemID.DRAGON_WARHAMMER, 50, 1),
        DRAGON_CLAWS(ItemID.DRAGON_CLAWS, 50, 2);

        @Getter private final int itemId, specAmt, hands;
        Spec(int itemId, int specAmt, int hands) {
            this.itemId = itemId;
            this.specAmt = specAmt;
            this.hands = hands;
        }
    }

    enum Food {
        MANTA_RAY(ItemID.MANTA_RAY),
        TUNA_POTATO(ItemID.TUNA_POTATO),
        DARK_CRAB(ItemID.DARK_CRAB),
        ANGLERFISH(ItemID.ANGLERFISH),
        SEA_TURTLE(ItemID.SEA_TURTLE),
        MUSHROOM_POTATO(ItemID.MUSHROOM_POTATO),
        SHARK(ItemID.SHARK),
        COOKED_KARAMBWAN(ItemID.COOKED_KARAMBWAN);
        @Getter
        private final int id;

        Food(int id) {
            this.id = id;
        }
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

    enum Antifire {
        ANTIFIRE_POTION(ItemID.ANTIFIRE_POTION4, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION1, ItemID.ANTIFIRE_POTION4, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION1),
        EXT_ANTIFIRE_POTION(ItemID.EXTENDED_ANTIFIRE4, ItemID.EXTENDED_ANTIFIRE3, ItemID.EXTENDED_ANTIFIRE2, ItemID.EXTENDED_ANTIFIRE1, ItemID.EXTENDED_ANTIFIRE4, ItemID.EXTENDED_ANTIFIRE3, ItemID.EXTENDED_ANTIFIRE2, ItemID.EXTENDED_ANTIFIRE1),
        SUPER_ANTIFIRE(ItemID.SUPER_ANTIFIRE_POTION4, ItemID.SUPER_ANTIFIRE_POTION3, ItemID.SUPER_ANTIFIRE_POTION2, ItemID.SUPER_ANTIFIRE_POTION1, ItemID.SUPER_ANTIFIRE_POTION4, ItemID.SUPER_ANTIFIRE_POTION3, ItemID.SUPER_ANTIFIRE_POTION2, ItemID.SUPER_ANTIFIRE_POTION1),
        EXT_SUPER_ANTIFIRE(ItemID.EXTENDED_SUPER_ANTIFIRE4, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE1, ItemID.EXTENDED_SUPER_ANTIFIRE4, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE1);

        @Getter
        private final int dose4, dose3, dose2, dose1;

        @Getter
        private final int[] ids;

        Antifire(int dose4, int dose3, int dose2, int dose1, int... ids) {
            this.dose1 = dose1;
            this.dose2 = dose2;
            this.dose3 = dose3;
            this.dose4 = dose4;
            this.ids = ids;
        }
    }

    enum Antivenom {
        ANTI_VENOM(ItemID.ANTIVENOM4, ItemID.ANTIVENOM3, ItemID.ANTIVENOM2, ItemID.ANTIVENOM1, ItemID.ANTIVENOM4, ItemID.ANTIVENOM3, ItemID.ANTIVENOM2, ItemID.ANTIVENOM1),
        ANTI_VENOM_PLUS(ItemID.ANTIVENOM4_12913, ItemID.ANTIVENOM3_12915, ItemID.ANTIVENOM2_12917, ItemID.ANTIVENOM1_12919, ItemID.ANTIVENOM4_12913, ItemID.ANTIVENOM3_12915, ItemID.ANTIVENOM2_12917, ItemID.ANTIVENOM1_12919),
        SERPENTINE_HELM(ItemID.SERPENTINE_HELM, -1, -1, -1, ItemID.SERPENTINE_HELM);

        @Getter
        private final int dose4, dose3, dose2, dose1;

        @Getter
        private final int[] ids;

        Antivenom(int dose4, int dose3, int dose2, int dose1, int... ids) {
            this.dose1 = dose1;
            this.dose2 = dose2;
            this.dose3 = dose3;
            this.dose4 = dose4;
            this.ids = ids;
        }
    }

    enum HouseTele {
        CONSTRUCTION_CAPE_T(ItemID.CONSTRUCT_CAPET),
        CONSTRUCTION_CAPE(ItemID.CONSTRUCT_CAPE),
        HOUSE_TABLET(ItemID.TELEPORT_TO_HOUSE),
        HOUSE_TELEPORT(-1);

        @Getter
        private final int id;

        HouseTele(int id) {
            this.id = id;
        }
    }

    enum BoostPotion {
        DIVINE_SUPER_COMBAT(ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION1, Skill.ATTACK, ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4),
        DIVINE_BASTION(ItemID.DIVINE_BASTION_POTION4, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION1, Skill.RANGED, ItemID.DIVINE_BASTION_POTION1, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION4),
        DIVINE_RANGING(ItemID.DIVINE_RANGING_POTION4, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION1, Skill.RANGED, ItemID.DIVINE_RANGING_POTION1, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION4),
        SUPER_COMBAT(ItemID.SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION1, Skill.ATTACK, ItemID.SUPER_COMBAT_POTION1, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION4),
        BASTION(ItemID.BASTION_POTION4, ItemID.BASTION_POTION3, ItemID.BASTION_POTION2, ItemID.BASTION_POTION2, Skill.RANGED, ItemID.BASTION_POTION1, ItemID.BASTION_POTION2, ItemID.BASTION_POTION3, ItemID.BASTION_POTION4),
        RANGING(ItemID.RANGING_POTION4, ItemID.RANGING_POTION3, ItemID.RANGING_POTION2, ItemID.RANGING_POTION1, Skill.RANGED, ItemID.RANGING_POTION1, ItemID.RANGING_POTION2, ItemID.RANGING_POTION3, ItemID.RANGING_POTION4),
        NONE(-1, -1, -1, -1, Skill.RANGED, -1);

        @Getter
        private final int dose4, dose3, dose2, dose1;

        @Getter
        private final Skill skill;

        @Getter
        private final int[] ids;

        BoostPotion(int dose4, int dose3, int dose2, int dose1, Skill skill, int... ids) {
            this.dose4 = dose4;
            this.dose3 = dose3;
            this.dose2 = dose2;
            this.dose1 = dose1;
            this.skill = skill;
            this.ids = ids;
        }
    }

    enum MoonClanTele {
        PORTAL_NEXUS(-1),
        MOONCLAN_PORTAL(-1);

        @Getter
        private final int objectID;

        MoonClanTele(int objectID) {
            this.objectID = objectID;
        }
    }

    enum walkMethod {
        NONE(1),
        WALK_ACID(2),
        WOOX_ACID_MELEE(3),
        WOOX_ACID_RANGED(4),
        WOOX_ACID_BLOWPIPE(5);

        @Getter
        private final int id;

        walkMethod(int id) {
            this.id = id;
        }
    }
}