package pers.tany.electronicstockmarket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import pers.tany.electronicstockmarket.command.Commands;
import pers.tany.electronicstockmarket.listenevent.Events;
import pers.tany.electronicstockmarket.placeholderapi.PlaceholderAPI;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.other.IDouble;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.register.IRegister;
import pers.tany.yukinoaapi.realizationpart.VaultUtil;

public class Main extends JavaPlugin {
    public static Plugin plugin = null;
    public static Economy economy;
    public static YamlConfiguration config;
    public static YamlConfiguration data;
    public static YamlConfiguration message;
    public static YamlConfiguration stock;

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getConsoleSender().sendMessage("§6[§eElectronicStockMarket§6]§a已启用");

        IConfig.createResource(this, "", "config.yml", false);
        IConfig.createResource(this, "", "data.yml", false);
        IConfig.createResource(this, "", "message.yml", false);
        IConfig.createResource(this, "", "stock.yml", false);

        config = IConfig.loadConfig(this, "", "config");
        data = IConfig.loadConfig(this, "", "data");
        message = IConfig.loadConfig(this, "", "message");
        stock = IConfig.loadConfig(this, "", "stock");

        IRegister.registerCommands(this, "ElectronicStockMarket", new Commands());
        IRegister.registerEvents(this, new Events());
        economy = VaultUtil.getEconomy();
        new PlaceholderAPI(this).register();

        new BukkitRunnable() {

            @Override
            public void run() {
                for (String player : data.getConfigurationSection("Player").getKeys(false)) {
                    for (String id : data.getConfigurationSection("Player." + player).getKeys(false)) {
                        if (data.getInt("Player." + player + "." + id + ".Coodling") > 1) {
                            data.set("Player." + player + "." + id + ".Coodling", data.getInt("Player." + player + "." + id + ".Coodling") - 1);
                        } else {
                            data.set("Player." + player + "." + id + ".Coodling", null);
                        }
                    }
                }
                IConfig.saveConfig(Main.plugin, data, "", "data");
            }

        }.runTaskTimer(Main.plugin, 1200, 1200);
        new BukkitRunnable() {
            int minute = 0;

            @Override
            public void run() {
                if (++minute % config.getInt("BreakMarket") == 0) {
                    update();
                }
            }

        }.runTaskTimer(Main.plugin, 1200, 1200);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§6[§eElectronicStockMarket§6]§c已卸载");
    }

    public static void update() {
        for (String id : stock.getConfigurationSection("ElectronicCurrency").getKeys(false)) {
            String url = "ElectronicCurrency." + id + ".";
            int changeProbabilityNumber = Math.min(Main.data.getInt(url + "ChangeProbabilityNumber"), Main.config.getInt(url + "MaxChangeProbabilityNumber"));
            if (IRandom.percentageChance(50)) {
                double defaultRisingProbability = IDouble.percentageNumber(Main.stock.getString(url + "DefaultRisingProbability"), false);
                if (Main.stock.getBoolean(url + "Rise", false)) {
                    double decreaseIncreaseRateRisingProbability = IDouble.percentageNumber(Main.stock.getString(url + "DecreaseIncreaseRateRisingProbability"), false) * changeProbabilityNumber;
                    defaultRisingProbability += decreaseIncreaseRateRisingProbability;
                }
                if (IRandom.percentageChance(defaultRisingProbability)) {
                    String[] s = stock.getString(url + "UpRisingProbability").split("-");
                    int min = (int) IDouble.percentageNumber(s[0], false);
                    int max = (int) IDouble.percentageNumber(s[1], false);
                    int increaseRateRisingProbability = (int) (IDouble.percentageNumber(Main.stock.getString(url + "IncreaseRateRisingProbability"), false) * changeProbabilityNumber);
                    min += increaseRateRisingProbability;
                    max += increaseRateRisingProbability;

                    int magnification = IRandom.randomNumber(min, max);
                    int nowMagnification = Math.min(Main.data.getInt(url + "Magnification", 100) + magnification, (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false));

                    Main.data.set(url + "Magnification", nowMagnification);
                    Main.data.set(url + "ChangeProbabilityNumber", Main.stock.getBoolean(url + "Rise", false) ? Main.data.getInt(url + "ChangeProbabilityNumber") + 1 : 1);
                    Main.data.set(url + "Rise", true);
                    if (nowMagnification == (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)) {
                        Main.data.set(url + "Change", "&c&l涨停中");
                    } else {
                        Main.data.set(url + "Change", "&c上涨&c&l" + magnification + "&c%");
                    }
                } else {
                    double defaultDownProbability = IDouble.percentageNumber(Main.stock.getString(url + "DefaultDownProbability"), false);
                    if (!Main.stock.getBoolean(url + "Rise", true)) {
                        double increaseRateDownProbability = IDouble.percentageNumber(Main.stock.getString(url + "IncreaseRateDownProbability"), false) * changeProbabilityNumber;
                        defaultDownProbability += increaseRateDownProbability;
                    }
                    if (IRandom.percentageChance(defaultDownProbability)) {
                        String[] s = stock.getString(url + "UpDownProbability").split("-");
                        int min = (int) IDouble.percentageNumber(s[0], false);
                        int max = (int) IDouble.percentageNumber(s[1], false);
                        int decreaseIncreaseRateDownProbability = (int) (IDouble.percentageNumber(Main.stock.getString(url + "DecreaseIncreaseRateDownProbability"), false) * changeProbabilityNumber);
                        min += decreaseIncreaseRateDownProbability;
                        max += decreaseIncreaseRateDownProbability;

                        int magnification = IRandom.randomNumber(min, max);
                        int nowMagnification = Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), Main.data.getInt(url + "Magnification", 100) - magnification);

                        Main.data.set(url + "Magnification", nowMagnification);
                        Main.data.set(url + "ChangeProbabilityNumber", !Main.stock.getBoolean(url + "Rise", true) ? Main.data.getInt(url + "ChangeProbabilityNumber") + 1 : 1);
                        Main.data.set(url + "Rise", false);
                        if (nowMagnification == (int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false)) {
                            Main.data.set(url + "Change", "&a&l跌停中");
                            for (String player : data.getConfigurationSection("Player").getKeys(false)) {
                                if (data.getConfigurationSection("Player." + player).getKeys(false).contains(id)) {
                                    int maxMagnification = data.getInt("Player." + player + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false));
                                    if (maxMagnification == IDouble.percentageNumber(config.getString("MinMagnification"), false)) {
                                        data.set(url + "SurplusNumber", data.getInt(url + "SurplusNumber") + data.getInt("Player." + player + "." + id + ".Has"));
                                        data.set("Player." + player + "." + id, null);
                                    }
                                }
                            }
                            for (String player : data.getConfigurationSection("Player").getKeys(false)) {
                                if (data.getConfigurationSection("Player." + player).getKeys(false).contains(id)) {
                                    data.set("Player." + player + "." + id + ".MaxMagnification", Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), data.getInt("Player." + player + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)) - (int) IDouble.percentageNumber(stock.getString(url + "DownMax"), false)));
                                }
                            }
                        } else {
                            Main.data.set(url + "Change", "&a下跌&a&l" + magnification + "&a%");
                        }
                    } else {
                        Main.data.set(url + "ChangeProbabilityNumber", 0);
                        Main.data.set(url + "Rise", null);
                        Main.data.set(url + "Change", "无变化");
                    }
                }
            } else {
                double defaultDownProbability = IDouble.percentageNumber(Main.stock.getString(url + "DefaultDownProbability"), false);
                if (!Main.stock.getBoolean(url + "Rise", true)) {
                    double increaseRateDownProbability = IDouble.percentageNumber(Main.stock.getString(url + "IncreaseRateDownProbability"), false) * changeProbabilityNumber;
                    defaultDownProbability += increaseRateDownProbability;
                }
                if (IRandom.percentageChance(defaultDownProbability)) {
                    String[] s = stock.getString(url + "UpDownProbability").split("-");
                    int min = (int) IDouble.percentageNumber(s[0], false);
                    int max = (int) IDouble.percentageNumber(s[1], false);
                    int decreaseIncreaseRateDownProbability = (int) (IDouble.percentageNumber(Main.stock.getString(url + "DecreaseIncreaseRateDownProbability"), false) * changeProbabilityNumber);
                    min += decreaseIncreaseRateDownProbability;
                    max += decreaseIncreaseRateDownProbability;

                    int magnification = IRandom.randomNumber(min, max);
                    int nowMagnification = Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), Main.data.getInt(url + "Magnification", 100) - magnification);

                    Main.data.set(url + "Magnification", nowMagnification);
                    Main.data.set(url + "ChangeProbabilityNumber", !Main.stock.getBoolean(url + "Rise", true) ? Main.data.getInt(url + "ChangeProbabilityNumber") + 1 : 1);
                    Main.data.set(url + "Rise", false);
                    if (nowMagnification == (int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false)) {
                        Main.data.set(url + "Change", "&a&l跌停中");
                        for (String player : data.getConfigurationSection("Player").getKeys(false)) {
                            if (data.getConfigurationSection("Player." + player).getKeys(false).contains(id)) {
                                int maxMagnification = data.getInt("Player." + player + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false));
                                if (maxMagnification == IDouble.percentageNumber(config.getString("MinMagnification"), false)) {
                                    data.set(url + "SurplusNumber", data.getInt(url + "SurplusNumber") + data.getInt("Player." + player + "." + id + ".Has"));
                                    data.set("Player." + player + "." + id, null);
                                }
                            }
                        }
                        for (String player : data.getConfigurationSection("Player").getKeys(false)) {
                            if (data.getConfigurationSection("Player." + player).getKeys(false).contains(id)) {
                                data.set("Player." + player + "." + id + ".MaxMagnification", Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), data.getInt("Player." + player + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)) - (int) IDouble.percentageNumber(stock.getString(url + "DownMax"), false)));
                            }
                        }
                    } else {
                        Main.data.set(url + "Change", "&a下跌&a&l" + magnification + "&a%");
                    }
                } else {
                    double defaultRisingProbability = IDouble.percentageNumber(Main.stock.getString(url + "DefaultRisingProbability"), false);
                    if (Main.stock.getBoolean(url + "Rise", false)) {
                        double decreaseIncreaseRateRisingProbability = IDouble.percentageNumber(Main.stock.getString(url + "DecreaseIncreaseRateRisingProbability"), false) * changeProbabilityNumber;
                        defaultRisingProbability += decreaseIncreaseRateRisingProbability;
                    }
                    if (IRandom.percentageChance(defaultRisingProbability)) {
                        String[] s = stock.getString(url + "UpRisingProbability").split("-");
                        int min = (int) IDouble.percentageNumber(s[0], false);
                        int max = (int) IDouble.percentageNumber(s[1], false);
                        int increaseRateRisingProbability = (int) (IDouble.percentageNumber(Main.stock.getString(url + "IncreaseRateRisingProbability"), false) * changeProbabilityNumber);
                        min += increaseRateRisingProbability;
                        max += increaseRateRisingProbability;

                        int magnification = IRandom.randomNumber(min, max);
                        int nowMagnification = Math.min(Main.data.getInt(url + "Magnification", 100) + magnification, (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false));

                        Main.data.set(url + "Magnification", nowMagnification);
                        Main.data.set(url + "ChangeProbabilityNumber", Main.stock.getBoolean(url + "Rise", false) ? Main.data.getInt(url + "ChangeProbabilityNumber") + 1 : 1);
                        Main.data.set(url + "Rise", true);
                        if (nowMagnification == (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)) {
                            Main.data.set(url + "Change", "&c&l涨停中");
                        } else {
                            Main.data.set(url + "Change", "&c上涨&c&l" + magnification + "&c%");
                        }
                    } else {
                        Main.data.set(url + "ChangeProbabilityNumber", 0);
                        Main.data.set(url + "Rise", null);
                        Main.data.set(url + "Change", "无变化");
                    }
                }
            }
        }
        IConfig.saveConfig(Main.plugin, data, "", "data");
        Bukkit.broadcastMessage(IString.color(Main.message.getString("Break")));
    }
}
