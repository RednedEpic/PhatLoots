package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.commands.LootCommand;
import com.codisimus.plugins.phatloots.listeners.MobListener;
import com.codisimus.plugins.phatloots.listeners.PhatLootsListener;
import com.codisimus.plugins.phatloots.loot.Item;
import com.codisimus.plugins.phatloots.loot.LootCollection;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Loads Config settings for the PhatLoots plugin
 *
 * @author Cody
 */
public class PhatLootsConfig {
    static int defaultDays; //Default cooldown time
    static int defaultHours;
    static int defaultMinutes;
    static int defaultSeconds;
    public static int defaultLowerNumberOfLoots; //Default collection range
    public static int defaultUpperNumberOfLoots;
    static boolean defaultGlobal;
    static boolean defaultRound;
    static boolean defaultAutoLoot;
    static boolean defaultBreakAndRespawn;
    static boolean restrictAll; //True if all PhatLoots should require permission
    static HashSet<String> restricted = new HashSet();
    public static String lootBagKey;
    public static String permission;
    public static String moneyLooted;
    public static String moneyCharged;
    public static String insufficientFunds;
    public static String experienceLooted;
    public static String autoLoot;
    public static String chestTimeRemaining;
    public static String commandTimeRemaining;
    public static String dispenserTimeRemaining;
    public static String overflow;
    public static String mobTimeRemaining;
    public static String mobDroppedMoney;
    public static String mobDroppedItem;
    public static String mobDroppedExperience;
    public static String lootMessage;
    public static String lootBroadcast;

    public static void load() {
        FileConfiguration config = PhatLoots.plugin.getConfig();

        //Check for an outdated config.yml file
        if (config.get("LootBagKey", null) == null) {
            PhatLoots.logger.warning("Your config.yml file is outdated! To get the most out of this plugin please (re)move the old file so a new one can be generated.");
        }


        /* LINKABLES */

        for (String string : config.getStringList("Blocks")) {
            Material mat = Material.matchMaterial(string);
            if (mat != null) {
                PhatLootsListener.types.put(mat, null);
            }
        }
        ConfigurationSection section = config.getConfigurationSection("AutoLink");
        if (section != null) {
            for (String world : section.getKeys(false)) {
                ConfigurationSection worldSection = section.getConfigurationSection(world);
                for (String string : worldSection.getKeys(false)) {
                    Material mat = Material.matchMaterial(string);
                    if (mat != null) {
                        if (PhatLootsListener.types.get(mat) == null) {
                            PhatLootsListener.types.put(mat, new HashMap());
                        }
                        PhatLootsListener.types.get(mat).put(world, worldSection.getString(string));
                    }
                }
            }
        }


        /* MOB LOOTS */

        PhatLoot.replaceMobLoot = config.getBoolean("ReplaceMobLoot");
        PhatLoot.onlyDropLootOnPlayerKill = config.getBoolean("OnlyDropLootWhenKilledByPlayer");
        PhatLoot.onlyDropExpOnPlayerKill = config.getBoolean("OnlyDropExpWhenKilledByPlayer");
        PhatLoot.chanceOfDrop = (float) (config.getDouble("MobLootDropPercentage") / 100.0D);
        PhatLoot.lootingBonusPerLvl = config.getDouble("LootingBonusPerLevel");
        MobListener.mobTypes = config.getBoolean("MobTypes");
        MobListener.namedMobs = config.getBoolean("NamedMobs");


        /* MESSAGES */

        section = config.getConfigurationSection("Messages");
        permission = getString(section, "Permission");
        experienceLooted = getString(section, "ExperienceLooted");
        moneyLooted = getString(section, "MoneyLooted");
        moneyCharged = getString(section, "MoneyCharged");
        insufficientFunds = getString(section, "InsufficientFunds");
        autoLoot = getString(section, "AutoLoot");
        overflow = getString(section, "Overflow");
        chestTimeRemaining = getString(section, "ChestTimeRemaining");
        commandTimeRemaining = getString(section, "CommandTimeRemaining");
        dispenserTimeRemaining = getString(section, "DispenserTimeRemaining");
        mobTimeRemaining = getString(section, "MobTimeRemaining");
        mobDroppedMoney = getString(section, "MobDroppedMoney");
        mobDroppedItem = getString(section, "MobDroppedItem");
        mobDroppedExperience = getString(section, "MobDroppedExperience");
        lootMessage = getString(section, "LootMessage");
        lootBroadcast = getString(section, "LootBroadcast");

        PhatLootChest.chestName = getString(config, "ChestName");


        /* TAGS */

        Item.damageTags = config.getBoolean("UseDamageTags");
        if (Item.damageTags) {
            Item.damageString = getString(config, "<dam>");
            Item.holyString = getString(config, "<holy>");
            Item.fireString = getString(config, "<fire>");
            Item.bugString = getString(config, "<bug>");
            Item.thornsString = getString(config, "<thorns>");
            Item.defenseString = getString(config, "<def>");
            Item.fireDefenseString = getString(config, "<firedef>");
            Item.rangeDefenseString = getString(config, "<rangedef>");
            Item.blastDefenseString = getString(config, "<blastdef>");
            Item.fallDefenseString = getString(config, "<falldef>");
        }


        /* DEFAULTS */

        section = config.getConfigurationSection("Defaults");
        defaultGlobal = section.getBoolean("GlobalReset");
        defaultRound = section.getBoolean("RoundDownTime");
        String itemsPerColl = section.getString("ItemsPerColl");
        if (itemsPerColl != null) {
            //This amount may be set as a range
            int index = itemsPerColl.indexOf('-');
            if (index == -1) { //Single number
                int numberOfLoots = Integer.parseInt(itemsPerColl);
                defaultLowerNumberOfLoots = numberOfLoots;
                defaultUpperNumberOfLoots = numberOfLoots;
            } else { //Range
                defaultLowerNumberOfLoots = Integer.parseInt(itemsPerColl.substring(0, index));
                defaultUpperNumberOfLoots = Integer.parseInt(itemsPerColl.substring(index + 1));
            }
        }
        defaultAutoLoot = section.getBoolean("AutoLoot");
        defaultBreakAndRespawn = section.getBoolean("BreakAndRespawn");

        section = section.getConfigurationSection("ResetTime");
        defaultDays = section.getInt("Days");
        defaultHours = section.getInt("Hours");
        defaultMinutes = section.getInt("Minutes");
        defaultSeconds = section.getInt("Seconds");


        /* OTHER */

        restrictAll = config.getBoolean("RestrictAll");
        restricted.addAll(config.getStringList("RestrictedPhatLoots"));
        for (String string : restricted) {
            string = ChatColor.translateAlternateColorCodes('&', string);
        }
        lootBagKey = ChatColor.translateAlternateColorCodes('&', config.getString("LootBagKey"));
        Item.tierNotify = config.getInt("MinimumTierNotification");
        LootCommand.setUnlockable = config.getBoolean("SetChestsAsUnlockable");
        PhatLoot.decimals = config.getBoolean("DivideMoneyAmountBy100");
        PhatLoot.soundOnAutoLoot = config.getBoolean("PlaySoundOnAutoLoot");
        PhatLootChest.useBreakAndRepawn = config.getBoolean("UseBreakAndRespawn");
        PhatLootChest.soundOnBreak = config.getBoolean("PlaySoundOnChestBreak");
        PhatLootChest.shuffleLoot = config.getBoolean("ShuffleLoot");
        LootCollection.allowDuplicates = config.getBoolean("AllowDuplicateItemsFromCollections");
        ForgettableInventory.delay = config.getInt("ForgetInventoryTime") * 20L;
        PhatLoot.unlink = config.getBoolean("UnlinkGlobalChestsThatNeverReset");
        PhatLoot.commandCooldown = config.getBoolean("ApplyCooldownToCommandLoot");

        /* LORES.YML */

        String fileName = "lores.yml";
        try {
            File file = new File(PhatLoots.dataFolder + File.separator + fileName);
            if (!file.exists()) {
                PhatLoots.plugin.saveResource(fileName, true);
            }

            Item.loreConfig = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            PhatLoots.logger.log(Level.SEVERE, "Failed to load " + fileName, ex);
        }


        /* TIERS.YML */

        fileName = "tiers.yml";
        try {
            File file = new File(PhatLoots.dataFolder + File.separator + fileName);
            if (!file.exists()) {
                PhatLoots.plugin.saveResource(fileName, true);
            }

            Item.tiersConfig = YamlConfiguration.loadConfiguration(file).getConfigurationSection("TIERS");
        } catch (Exception ex) {
            PhatLoots.logger.log(Level.SEVERE, "Failed to load " + fileName, ex);
        }


        /* ENCHANTMENTS.YML */

        fileName = "enchantments.yml";
        try {
            File file = new File(PhatLoots.dataFolder + File.separator + fileName);
            if (!file.exists()) {
                PhatLoots.plugin.saveResource(fileName, true);
            }

            Item.enchantmentConfig = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            PhatLoots.logger.log(Level.SEVERE, "Failed to load " + fileName, ex);
        }
    }

    /**
     * Returns the converted string that is loaded from the given configuration.
     * & will be converted to § where color codes are used
     *
     * @param config The given ConfigurationSection
     * @param key The key that leads to the requested string
     * @return The String or null if the string was not found or empty
     */
    private static String getString(ConfigurationSection config, String key) {
        String string = ChatColor.translateAlternateColorCodes('&', config.getString(key));
        return string.isEmpty() ? null : string;
    }
}
