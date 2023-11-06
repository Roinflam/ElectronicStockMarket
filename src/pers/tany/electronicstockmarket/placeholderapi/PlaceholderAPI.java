package pers.tany.electronicstockmarket.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import pers.tany.electronicstockmarket.Main;
import pers.tany.yukinoaapi.interfacepart.other.IDouble;
import pers.tany.yukinoaapi.interfacepart.other.IString;

public class PlaceholderAPI extends PlaceholderExpansion {

    public PlaceholderAPI(Main main) {
        super();
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String string) {
        if (string.startsWith("Magnification.")) {
            String id = string.split("\\.")[1];
            String url = "ElectronicCurrency." + id + ".";
            return Main.data.getInt(url + "Magnification", 100) + "%";
        }
        if (string.startsWith("LastChange.")) {
            String id = string.split("\\.")[1];
            String url = "ElectronicCurrency." + id + ".";
            return IString.strip(IString.color(Main.data.getString(url + "Change", "无变化")));
        }
        if (string.startsWith("Has.")) {
            String id = string.split("\\.")[1];
            return String.valueOf(Main.data.getInt("Player." + offlinePlayer.getName() + "." + id + "." + "Has"));
        }
        if (string.startsWith("AllMoney.")) {
            String id = string.split("\\.")[1];
            String url = "ElectronicCurrency." + id + ".";
            int magnification = Main.data.getInt(url + "Magnification", 100);
            int maxMagnification = Math.max((int) IDouble.percentageNumber(Main.config.getString("MinMagnification"), false), Main.data.getInt("Player." + offlinePlayer.getName() + "." + id + ".MaxMagnification", (int) IDouble.percentageNumber(Main.config.getString("MaxMagnification"), false)));
            double defaultMoney = Main.stock.getDouble(url + "DefaultMoney");
            int has = Main.data.getInt("Player." + offlinePlayer.getName() + "." + id + "." + "Has");
            double allMoney = defaultMoney * ((double) Math.min(magnification, maxMagnification) / 100) * has;
            return String.valueOf(allMoney);
        }
        return null;
    }

    @Override
    public String getAuthor() {
        return "Tany";
    }

    @Override
    public String getIdentifier() {
        return "ElectronicStockMarket";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

}
