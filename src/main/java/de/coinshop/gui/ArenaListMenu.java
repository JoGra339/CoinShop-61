package de.coinshop.gui;

import de.coinshop.ArenaManager;
import de.coinshop.CoinShopPlugin;
import de.coinshop.CombatSim;
import de.coinshop.ExpeditionManager;
import de.coinshop.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Übersicht der Untergrund-Arenen aller Reviere. */
public final class ArenaListMenu extends Menu {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16};

    public ArenaListMenu(CoinShopPlugin plugin, Player viewer) {
        super(plugin, 4, Component.text("Untergrund-Arenen"));
        ExpeditionManager ex = plugin.expeditions();
        ArenaManager arenas = ex.arenas();
        UUID uuid = viewer.getUniqueId();
        boolean allowed = arenas.allowed(uuid);
        for (int site = 0; site < arenas.sites() && site < SLOTS.length; site++) {
            ArenaManager.Arena a = arenas.arena(site);
            List<CombatSim.Fighter> def = arenas.defenders(a);
            boolean mine = uuid.equals(a.owner);
            List<Component> lore = new ArrayList<>();
            lore.add(ExpeditionMenu.line(a.owner == null ? "Gehalten von: NPC" : "Besitzer: " + a.ownerName + (mine ? " (du)" : ""),
                    mine ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
            lore.add(ExpeditionMenu.line("Verteidiger: " + def.size() + ", Stärke " + CombatSim.strength(def), NamedTextColor.GRAY));
            if (a.owner != null) {
                lore.add(ExpeditionMenu.line("Abgewehrte Herausforderer: " + a.defendedTotal, NamedTextColor.GRAY));
                lore.add(ExpeditionMenu.line("Tagesertrag aktuell: " + arenas.dailyPay(a) + " Coins", NamedTextColor.GOLD));
            }
            lore.add(ExpeditionMenu.line(allowed ? "Klicken: öffnen" : "Nur mit Schwarzmarkt-Lizenz „Schmuggler“", allowed ? NamedTextColor.GREEN : NamedTextColor.RED));
            final int s = site;
            set(SLOTS[site], ItemUtil.icon(mine ? Material.NETHERITE_SWORD : a.owner == null ? Material.IRON_SWORD : Material.DIAMOND_SWORD,
                    ExpeditionMenu.line(arenas.siteName(site), mine ? NamedTextColor.GREEN : NamedTextColor.GOLD), lore),
                    allowed ? (p, c) -> new ArenaMenu(plugin, p, s).open(p) : null);
        }
        set(22, ItemUtil.icon(Material.BOOK, ExpeditionMenu.line("So funktioniert es", NamedTextColor.AQUA), List.of(
                ExpeditionMenu.line("Besiege den NPC, dann gehört dir die Arena.", NamedTextColor.GRAY),
                ExpeditionMenu.line("Stationiere 1 bis 5 Begleiter als Verteidiger.", NamedTextColor.GRAY),
                ExpeditionMenu.line("Andere Spieler können dich herausfordern; verlierst du, gehört sie ihnen.", NamedTextColor.GRAY),
                ExpeditionMenu.line("Hältst du sie bis zum Shopwechsel, bekommst du den Tagesertrag.", NamedTextColor.GRAY),
                ExpeditionMenu.line("Er steigt mit jedem abgewehrten Herausforderer.", NamedTextColor.GRAY))), null);
        set(27, backButton(), (p, c) -> new ExpeditionMenu(plugin, p).open(p));
        fillEmpty();
    }

    @Override
    public void open(Player player) {
        set(35, balanceIcon(player), incomeClick());
        super.open(player);
    }
}
