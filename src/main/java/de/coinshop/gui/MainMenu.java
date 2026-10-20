package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ItemUtil;
import de.coinshop.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Das Hauptmenü. Die Knöpfe sind reine Wegweiser; nur Schwarzmarkt, Wettbüro und Großhandel erklären, welche Lizenz sie brauchen.
 * Schwarzmarkt und Großhandel sind immer sichtbar, lassen sich aber nur mit der jeweiligen Lizenz betreten.
 */
public final class MainMenu extends Menu {

    private static final List<Component> NONE = List.of();

    public MainMenu(CoinShopPlugin plugin) {
        super(plugin, 5, plugin.messages().get("gui.main.title"));
        Messages msg = plugin.messages();

        // Reihe 1 (am haeufigsten): Shop, Aufgaben, Expeditionen, Casino
        // Reihe 2: Investitionen, Wettbuero, Lizenzen, Grosshandel
        // Reihe 3: Schwarzmarkt, Rangliste, Titel, Sammlungen, Geheimnisse
        // Untere Reihe: symmetrisch um die Mitte: Profil, Kontostand, Schliessen, Shop-Konto, Ereignisse
        set(10, ItemUtil.icon(Material.ENDER_CHEST, msg.get("gui.main.shop-name"), NONE),
                (player, click) -> new ShopHubMenu(plugin, player).open(player));
        set(16, ItemUtil.icon(Material.NETHER_STAR, msg.get("gui.main.casino-name"), NONE),
                (player, click) -> new CasinoMenu(plugin, player).open(player));
        set(19, ItemUtil.icon(Material.GOLD_BLOCK, msg.get("gui.main.invest-name"), NONE),
                (player, click) -> new InvestHubMenu(plugin, player).open(player));
        set(28, ItemUtil.icon(Material.WITHER_SKELETON_SKULL, msg.get("gui.main.blackmarket-name"), msg.getList("gui.main.blackmarket-lore")),
                (player, click) -> {
                    if (plugin.blackMarket().unlocked(player)) {
                        new BlackMarketHubMenu(plugin, player).open(player);
                    } else {
                        msg.send(player, "bm-locked");
                    }
                });

                set(30, ItemUtil.icon(Material.GOLDEN_HELMET, msg.get("gui.main.rank-name"), NONE),
                (player, click) -> new RankingMenu(plugin, player).open(player));
        set(32, ItemUtil.icon(Material.BOOKSHELF, msg.get("gui.main.collection-name"), NONE),
                (player, click) -> new CollectionsMenu(plugin, player).open(player));
        set(4, ItemUtil.icon(Material.ENDER_EYE, msg.get("gui.main.secret-name"), NONE),
                (player, click) -> new SecretMenu(plugin, player).open(player));
        set(34, ItemUtil.icon(Material.NAME_TAG, msg.get("gui.main.title-name"), NONE),
                (player, click) -> new TitleMenu(plugin, player).open(player));

                set(12, ItemUtil.icon(Material.WRITABLE_BOOK, msg.get("gui.main.quest-name"), NONE),
                (player, click) -> new QuestMenu(plugin, player).open(player));
        set(23, ItemUtil.icon(Material.PAPER, msg.get("gui.main.license-name"), NONE),
                (player, click) -> new LicenseMenu(plugin, player).open(player));
        set(37, ItemUtil.icon(Material.PLAYER_HEAD, msg.get("gui.main.profile-name"), NONE),
                (player, click) -> new ProfileMenu(plugin, player, player.getUniqueId()).open(player));
        set(25, ItemUtil.icon(Material.BARREL, msg.get("gui.main.wholesale-name"), msg.getList("gui.main.wholesale-lore")),
                (player, click) -> plugin.wholesale().openFor(player));
        set(43, ItemUtil.icon(Material.BELL, msg.get("gui.events.button"), NONE),
                (player, click) -> new EventsMenu(plugin, player).open(player));

        // Untere Reihe: Schließen (Kontostand und Shop-Konto kommen beim Öffnen dazu)
        set(40, ItemUtil.icon(Material.BARRIER, msg.get("gui.close-name"), NONE),
                (player, click) -> player.closeInventory());

        fillEmpty();
    }

    @Override
    public void open(Player player) {
        Messages msg = plugin.messages();
        // Diese Anzeigen ändern sich ständig, deshalb erst beim Öffnen einsetzen
        set(39, balanceIcon(player), incomeClick());
        if (plugin.bets().enabled()) {
            set(21, ItemUtil.icon(Material.SPYGLASS, msg.get("gui.main.bets-name"), msg.getList("gui.main.bets-lore")),
                    (p, click) -> {
                        if (plugin.bets().allowed(p)) {
                            new BetMenu(plugin, p).open(p);
                        }
                    });
        }
        if (plugin.expeditions().enabled()) {
            set(14, ItemUtil.icon(Material.DIAMOND_SWORD, ExpeditionMenu.line("Expeditionen", net.kyori.adventure.text.format.NamedTextColor.GOLD),
                    List.of(ExpeditionMenu.line("Kämpfe in der Arena gegen Kreaturen.", net.kyori.adventure.text.format.NamedTextColor.GRAY),
                            ExpeditionMenu.line("Braucht die Expeditionslizenz.", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY))),
                    (p, click) -> {
                        if (!plugin.expeditions().hasLicense(p)) {
                            msg.send(p, "ex-locked");
                        } else {
                            new ExpeditionMenu(plugin, p).open(p);
                        }
                    });
        }
        set(41, ItemUtil.icon(Material.GOLD_INGOT, msg.get("gui.main.shop-account-name"),
                msg.getList("gui.main.shop-account-lore", "coins", plugin.database().getShopAccount())), null);
        super.open(player);
    }
}
