package pers.tany.electronicstockmarket.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import pers.tany.electronicstockmarket.Main;
import pers.tany.electronicstockmarket.gui.MarketInterface;
import pers.tany.yukinoaapi.interfacepart.builder.IItemBuilder;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.inventory.IInventory;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;
import pers.tany.yukinoaapi.realizationpart.builder.ItemBuilder;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("break")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§c你没有权限执行此命令");
                    return true;
                }
                Main.update();
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§c你没有权限执行此命令");
                    return true;
                }
                Main.config = IConfig.loadConfig(Main.plugin, "", "config");
                Main.data = IConfig.loadConfig(Main.plugin, "", "data");
                Main.message = IConfig.loadConfig(Main.plugin, "", "message");
                Main.stock = IConfig.loadConfig(Main.plugin, "", "stock");
                sender.sendMessage("§a重载成功");
                return true;
            }
        }
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("§6/esm break  §e手动让股市进行涨跌");
            sender.sendMessage("§6/esm reload  §e重载配置文件");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("open")) {
                IInventory.openInventory(new MarketInterface(player), player);
                return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setSkin")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§c你没有权限执行此命令");
                    return true;
                }
                if (!Main.stock.getConfigurationSection("ElectronicCurrency").contains(args[1])) {
                    sender.sendMessage("§c没有这个编号！");
                    return true;
                }
                if (IItem.isEmptyHand(player)) {
                    player.sendMessage("§c不能为空手！");
                    return true;
                }
                IItemBuilder itemBuilder = new ItemBuilder(player.getItemInHand());
                Main.data.set("Skin." + args[1], ISerializer.serializerItemStack(itemBuilder.clearLore().getItemStack(), true));
                IConfig.saveConfig(Main.plugin, Main.data, "", "data");
                sender.sendMessage("§a设置成功");
                return true;
            }
        }
        sender.sendMessage("§6/esm open  §e打开股市市场");
        sender.sendMessage("§6/esm break  §e手动让股市进行涨跌");
        sender.sendMessage("§6/esm setSkin 币编号  §e设置此币在界面显示的材质");
        sender.sendMessage("§6/esm reload  §e重载配置文件");
        return true;
    }
}
