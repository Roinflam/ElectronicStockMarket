package pers.tany.electronicstockmarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import pers.tany.electronicstockmarket.Main;
import pers.tany.yukinoaapi.interfacepart.builder.IItemBuilder;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.inventory.IInventory;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.*;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;
import pers.tany.yukinoaapi.realizationpart.VaultUtil;
import pers.tany.yukinoaapi.realizationpart.builder.ItemBuilder;
import pers.tany.yukinoaapi.realizationpart.item.GlassPaneUtil;

import java.util.HashMap;
import java.util.List;

public class MarketInterface implements InventoryHolder, Listener {
    private final String serial;

    private final Inventory inventory;
    private final Player player;
    private final int taskID;
    private final HashMap<Integer, String> slotItem = new HashMap<>();
    private String id;
    private boolean buy = false;
    private boolean sell = false;

    public MarketInterface(Player player) {
        Inventory inventory = Bukkit.createInventory(this, 54, IString.color(Main.message.getString("Title")));

        for (int i = 45; i < 54; i++) {
            IItemBuilder itemBuilder = GlassPaneUtil.getStainedGlass(1);
            itemBuilder.setDisplayName(Main.message.getString("HelpName")).setLore(Main.message.getStringList("HelpLore"));
            inventory.setItem(i, itemBuilder.getItemStack());
        }
        int i = 0;
        for (String id : Main.stock.getConfigurationSection("ElectronicCurrency").getKeys(false)) {
            IItemBuilder itemBuilder = null;
            String url = "ElectronicCurrency." + id + ".";
            if (Main.data.getConfigurationSection("Skin").getKeys(false).contains(id)) {
                itemBuilder = new ItemBuilder(ISerializer.deserializeItemStack(Main.data.getString("Skin." + id)));
            } else {
                itemBuilder = new ItemBuilder("PAPER");
            }
            itemBuilder.setDisplayName(Main.stock.getString(url + "Name")).setLore(Main.stock.getStringList(url + "Lore"));
            List<String> lore = Main.message.getStringList("InfoLore");

            int magnification = Main.data.getInt(url + "Magnification", 100);
            String change = Main.data.getString(url + "Change", "无变化");
            double defaultMoney = Main.stock.getDouble(url + "DefaultMoney");
            double money = defaultMoney * ((double) magnification / 100);
            int surplusNumber = Main.data.getInt(url + "SurplusNumber", Main.stock.getInt(url + "MaxNumber"));
            int maxMagnification = Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)));
            int has = Main.data.getInt("Player." + player.getName() + "." + id + "." + "Has");
            double allMoney = defaultMoney * ((double) Math.min(magnification, maxMagnification) / 100) * has;

            if (Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification") > 0) {
                money *= Main.data.getDouble("Player." + player.getName() + "." + id + ".MaxMagnification") / 100.0;
            }
            if (Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification") > 0) {
                allMoney *= Main.data.getDouble("Player." + player.getName() + "." + id + ".MaxMagnification") / 100.0;
            }

            lore = IList.listReplace(lore, "[magnification]", magnification + "%");
            lore = IList.listReplace(lore, "[change]", change);
            lore = IList.listReplace(lore, "[defaultMoney]", String.valueOf(IDouble.shortenDouble(defaultMoney, 2)));
            lore = IList.listReplace(lore, "[money]", String.valueOf(IDouble.shortenDouble(money, 2)));
            lore = IList.listReplace(lore, "[surplusNumber]", String.valueOf(surplusNumber));
            lore = IList.listReplace(lore, "[number]", String.valueOf(has));
            lore = IList.listReplace(lore, "[maxMagnification]", maxMagnification + "%");
            lore = IList.listReplace(lore, "[allMoney]", String.valueOf(IDouble.shortenDouble(allMoney, 2)));

            inventory.setItem(i, itemBuilder.addLoreAll(lore).getItemStack());

            slotItem.put(i++, id);
        }

        this.inventory = inventory;
        this.player = player;
        this.serial = IRandom.createRandomString(8);

        Bukkit.getPluginManager().registerEvents(this, Main.plugin);
        taskID = new BukkitRunnable() {

            @Override
            public void run() {
                int i = 0;
                for (String id : Main.stock.getConfigurationSection("ElectronicCurrency").getKeys(false)) {
                    IItemBuilder itemBuilder = null;
                    String url = "ElectronicCurrency." + id + ".";
                    if (Main.data.getConfigurationSection("Skin").getKeys(false).contains(id)) {
                        itemBuilder = new ItemBuilder(ISerializer.deserializeItemStack(Main.data.getString("Skin." + id)));
                    } else {
                        itemBuilder = new ItemBuilder("PAPER");
                    }
                    itemBuilder.setDisplayName(Main.stock.getString(url + "Name")).setLore(Main.stock.getStringList(url + "Lore"));
                    List<String> lore = Main.message.getStringList("InfoLore");

                    int magnification = Main.data.getInt(url + "Magnification", 100);
                    String change = Main.data.getString(url + "Change", "无变化");
                    double defaultMoney = Main.stock.getDouble(url + "DefaultMoney");
                    double money = defaultMoney * ((double) magnification / 100);
                    int surplusNumber = Main.data.getInt(url + "SurplusNumber", Main.stock.getInt(url + "MaxNumber"));
                    int maxMagnification = Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)));
                    int has = Main.data.getInt("Player." + player.getName() + "." + id + "." + "Has");
                    double allMoney = defaultMoney * ((double) Math.min(magnification, maxMagnification) / 100) * has;

                    if (Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification") > 0) {
                        allMoney *= Main.data.getDouble("Player." + player.getName() + "." + id + ".MaxMagnification") / 100.0;
                    }

                    lore = IList.listReplace(lore, "[magnification]", magnification + "%");
                    lore = IList.listReplace(lore, "[change]", change);
                    lore = IList.listReplace(lore, "[defaultMoney]", String.valueOf(IDouble.shortenDouble(defaultMoney, 2)));
                    lore = IList.listReplace(lore, "[money]", String.valueOf(IDouble.shortenDouble(money, 2)));
                    lore = IList.listReplace(lore, "[surplusNumber]", String.valueOf(surplusNumber));
                    lore = IList.listReplace(lore, "[number]", String.valueOf(has));
                    lore = IList.listReplace(lore, "[maxMagnification]", maxMagnification + "%");
                    lore = IList.listReplace(lore, "[allMoney]", String.valueOf(IDouble.shortenDouble(allMoney, 2)));

                    inventory.setItem(i, itemBuilder.addLoreAll(lore).getItemStack());

                    slotItem.put(i++, id);
                }
            }

        }.runTaskTimerAsynchronously(Main.plugin, 1, 1).getTaskId();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getSerial() {
        return serial;
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player && evt.getWhoClicked().equals(player)) {
            int rawSlot = evt.getRawSlot();
            if (rawSlot != -999) {
                if (evt.getInventory().getHolder() instanceof MarketInterface) {
                    evt.setCancelled(true);
                    if (evt.getClickedInventory().getHolder() instanceof MarketInterface) {
                        if (!IItem.isEmpty(evt.getCurrentItem())) {
                            if (rawSlot < 45) {
                                String id = slotItem.get(rawSlot);
                                String url = "ElectronicCurrency." + id + ".";
                                if (evt.getClick().equals(ClickType.LEFT)) {
                                    int surplusNumber = Main.data.getInt(url + "SurplusNumber", Main.stock.getInt(url + "MaxNumber"));
                                    if (surplusNumber <= 0) {
                                        player.sendMessage("§c币的剩余数量不足！");
                                        return;
                                    }
                                    if (Main.data.getInt("Player." + player.getName() + "." + id + "." + "Coodling") > 0) {
                                        String[] s = ITime.getDay(Main.data.getInt("Player." + player.getName() + "." + id + "." + "Coodling")).split(":");
                                        player.sendMessage("§c冷静期未结束，请等待" + s[0] + "日" + s[1] + "时" + s[2] + "分");
                                        return;
                                    }
                                    buy = true;
                                    this.id = id;
                                    player.closeInventory();
                                    player.sendMessage("§a请输入需要买入币的个数");
                                } else if (evt.getClick().equals(ClickType.RIGHT)) {
                                    int has = Main.data.getInt("Player." + player.getName() + "." + id + "." + "Has");
                                    if (has <= 0) {
                                        player.sendMessage("§c币的拥有数量不足！");
                                        return;
                                    }
                                    if (Main.data.getInt("Player." + player.getName() + "." + id + "." + "Coodling") > 0) {
                                        String[] s = ITime.getDay(Main.data.getInt("Player." + player.getName() + "." + id + "." + "Coodling")).split(":");
                                        player.sendMessage("§c冷静期未结束，请等待" + s[0] + "日" + s[1] + "时" + s[2] + "分");
                                        return;
                                    }
                                    sell = true;
                                    this.id = id;
                                    player.closeInventory();
                                    player.sendMessage("§a请输入需要卖出币的个数");
                                } else if (evt.getClick().equals(ClickType.SHIFT_LEFT)) {
                                    int surplusNumber = Main.data.getInt(url + "SurplusNumber", Main.stock.getInt(url + "MaxNumber"));
                                    if (surplusNumber <= 0) {
                                        player.sendMessage("§c币的剩余数量不足！");
                                        return;
                                    }
                                    if (Main.data.getInt("Player." + player.getName() + "." + id + "." + "Coodling") > 0) {
                                        String[] s = ITime.getDay(Main.data.getInt("Player." + player.getName() + "." + id + "." + "Coodling")).split(":");
                                        player.sendMessage("§c冷静期未结束，请等待" + s[0] + "日" + s[1] + "时" + s[2] + "分");
                                        return;
                                    }
                                    double defaultMoney = Main.stock.getDouble(url + "DefaultMoney");
                                    int magnification = Main.data.getInt(url + "Magnification", 100);
                                    double money = defaultMoney * ((double) magnification / 100);

                                    int has = Main.data.getInt("Player." + player.getName() + "." + id + "." + "Has");
                                    if (Main.stock.getInt(url + "PlayerMaxNumber") > 0) {
                                        if (has >= Main.stock.getInt(url + "PlayerMaxNumber")) {
                                            player.sendMessage("§c无法再购入此币了！");
                                            return;
                                        }
                                    }
                                    int number = Math.min(surplusNumber, Main.stock.getInt(url + "PlayerMaxNumber") - has);
                                    if (VaultUtil.hasMoney(player, (int) (money * number))) {
                                        Main.data.set(url + "SurplusNumber", 0);
                                        Main.data.set("Player." + player.getName() + "." + id + "." + "Has", has + number);
                                        if (magnification < 100) {
                                            Main.data.set("Player." + player.getName() + "." + id + "." + "MaxMagnification", (int) ((double) magnification / 100 * IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)));
                                        }
                                        Main.data.set("Player." + player.getName() + "." + id + "." + "Coodling", Main.config.getInt("BuyCoolding"));
                                        Main.economy.withdrawPlayer(player, money * number);
                                        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
                                        player.sendMessage("§a购买成功！花费" + (int) (money * number) + "游戏币");
                                    } else {
                                        player.sendMessage("§c游戏币不足！需要" + (int) (money * number) + "游戏币");
                                    }
                                } else if (evt.getClick().equals(ClickType.SHIFT_RIGHT)) {
                                    int has = Main.data.getInt("Player." + player.getName() + "." + id + "." + "Has");
                                    if (has <= 0) {
                                        player.sendMessage("§c币的拥有数量不足！");
                                        return;
                                    }
                                    if (Main.data.getInt("Player." + player.getName() + "." + id + "." + "Coodling") > 0) {
                                        String[] s = ITime.getDay(Main.data.getInt("Player." + player.getName() + "." + id + "." + "Coodling")).split(":");
                                        player.sendMessage("§c冷静期未结束，请等待" + s[0] + "日" + s[1] + "时" + s[2] + "分");
                                        return;
                                    }
                                    int surplusNumber = Main.data.getInt(url + "SurplusNumber", Main.stock.getInt(url + "MaxNumber"));
                                    double defaultMoney = Main.stock.getDouble(url + "DefaultMoney");
                                    int magnification = Math.min(Main.data.getInt(url + "Magnification", 100), Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false))));
                                    double money = defaultMoney * ((double) magnification / 100);
                                    double allMoney = money * has;

                                    if (Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification") > 0) {
                                        allMoney *= Main.data.getDouble("Player." + player.getName() + "." + id + ".MaxMagnification") / 100.0;
                                    }

                                    Main.economy.depositPlayer(player, allMoney);
                                    Main.data.set(url + "SurplusNumber", surplusNumber + has);
                                    Main.data.set("Player." + player.getName() + "." + id, null);
                                    Main.data.set("Player." + player.getName() + "." + id + "." + "Coodling", Main.config.getInt("BuyCoolding"));

                                    IConfig.saveConfig(Main.plugin, Main.data, "", "data");
                                    player.sendMessage("§a卖出成功！获得" + IDouble.shortenDouble(allMoney, 2) + "游戏币");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent evt) {
        if (evt.getPlayer().equals(player)) {
            Bukkit.getScheduler().cancelTask(taskID);
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getInventory().getHolder() instanceof MarketInterface && evt.getPlayer() instanceof Player) {
            MarketInterface marketInterface = (MarketInterface) evt.getInventory().getHolder();
            if (evt.getPlayer().equals(player) && !buy && !sell && marketInterface.getSerial().equals(serial)) {
                Bukkit.getScheduler().cancelTask(taskID);
                HandlerList.unregisterAll(this);
            }
        }
    }

    @EventHandler
    private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent evt) {
        Player player = evt.getPlayer();
        if (player.getName().equals(this.player.getName())) {
            evt.setCancelled(true);
        }
    }

    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent evt) {
        Player player = evt.getPlayer();
        if (player.getName().equals(this.player.getName())) {
            if (buy) {
                evt.setCancelled(true);
                String url = "ElectronicCurrency." + id + ".";
                int surplusNumber = Main.data.getInt(url + "SurplusNumber", Main.stock.getInt(url + "MaxNumber"));
                int number = 0;
                int has = Main.data.getInt("Player." + player.getName() + "." + id + "." + "Has");
                if (Main.stock.getInt(url + "PlayerMaxNumber") > 0) {
                    if (has >= Main.stock.getInt(url + "PlayerMaxNumber")) {
                        Bukkit.getScheduler().cancelTask(taskID);
                        HandlerList.unregisterAll(this);
                        player.sendMessage("§c无法再购入此币了！");
                        return;
                    }
                }
                int quantityAvailable = Math.min(surplusNumber, Main.stock.getInt(url + "PlayerMaxNumber") - has);
                try {
                    number = Integer.parseInt(evt.getMessage());
                    if (number > quantityAvailable) {
                        throw new NumberFormatException();
                    }
                    if (number < 0) {
                        throw new NumberFormatException();
                    }
                    if (number == 0) {
                        Bukkit.getScheduler().cancelTask(taskID);
                        HandlerList.unregisterAll(this);
                        player.sendMessage("§c成功取消操作");
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§c请输入0 - " + quantityAvailable + "的数字");
                    return;
                }
                double defaultMoney = Main.stock.getDouble(url + "DefaultMoney");
                int magnification = Main.data.getInt(url + "Magnification", 100);
                double money = defaultMoney * ((double) magnification / 100);
                if (VaultUtil.hasMoney(player, (int) (money * number))) {
                    Main.data.set(url + "SurplusNumber", surplusNumber - number);
                    Main.data.set("Player." + player.getName() + "." + id + "." + "Has", has + number);
                    if (magnification < 100) {
                        Main.data.set("Player." + player.getName() + "." + id + "." + "MaxMagnification", (int) ((double) magnification / 100 * IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)));
                    }
                    Main.data.set("Player." + player.getName() + "." + id + "." + "Coodling", Main.config.getInt("BuyCoolding"));

                    IConfig.saveConfig(Main.plugin, Main.data, "", "data");
                    Main.economy.withdrawPlayer(player, money * number);
                    player.sendMessage("§a购买成功！花费" + (int) (money * number) + "游戏币");
                    buy = false;
                    IInventory.openInventory(inventory, player);
                } else {
                    player.sendMessage("§c游戏币不足！需要" + (int) (money * number) + "游戏币");
                }
            } else if (sell) {
                evt.setCancelled(true);
                String url = "ElectronicCurrency." + id + ".";
                int has = Main.data.getInt("Player." + player.getName() + "." + id + "." + "Has");
                int number = 0;
                try {
                    number = Integer.parseInt(evt.getMessage());
                    if (number > has) {
                        throw new NumberFormatException();
                    }
                    if (number < 0) {
                        throw new NumberFormatException();
                    }
                    if (number == 0) {
                        Bukkit.getScheduler().cancelTask(taskID);
                        HandlerList.unregisterAll(this);
                        player.sendMessage("§c成功取消操作");
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§c请输入0 - " + has + "的数字");
                    return;
                }
                int surplusNumber = Main.data.getInt(url + "SurplusNumber", Main.stock.getInt(url + "MaxNumber"));
                double defaultMoney = Main.stock.getDouble(url + "DefaultMoney");
                int magnification = Math.min(Main.data.getInt(url + "Magnification", 100), Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false))));
                double money = defaultMoney * ((double) magnification / 100);
                double allMoney = money * number;

                if (Main.data.getInt("Player." + player.getName() + "." + id + ".MaxMagnification") > 0) {
                    allMoney *= Main.data.getDouble("Player." + player.getName() + "." + id + ".MaxMagnification") / 100.0;
                }

                Main.economy.depositPlayer(player, allMoney);
                Main.data.set(url + "SurplusNumber", surplusNumber + number);
                if (has - number == 0) {
                    Main.data.set("Player." + player.getName() + "." + id, null);
                } else {
                    Main.data.set("Player." + player.getName() + "." + id + ".Has", has - number);
                }
                Main.data.set("Player." + player.getName() + "." + id + "." + "Coodling", Main.config.getInt("BuyCoolding"));

                IConfig.saveConfig(Main.plugin, Main.data, "", "data");
                player.sendMessage("§a卖出成功！获得" + IDouble.shortenDouble(allMoney, 2) + "游戏币");
                sell = false;
                IInventory.openInventory(inventory, player);
            }
        }
    }
}
