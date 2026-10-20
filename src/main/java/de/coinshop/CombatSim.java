package de.coinshop;

import de.coinshop.ExpeditionManager.Copy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Berechneter Kampf zweier Gruppen (für NPC-Duelle und Untergrund-Arenen). Wie beim Spieler-Duell kämpfen die Ersten
 * beider Seiten gegeneinander, Typen zählen, die Begleiter werden nicht verletzt.
 */
public final class CombatSim {

    private CombatSim() {
    }

    /** Ein Kämpfer mit Typ, Leben und Angriff. */
    public record Fighter(String name, CompanionTypes.MobType type, double hp, double atk, boolean defender) { }

    /** Ergebnis: Sieger (Seite A?) und Kampfbericht. */
    public record Result(boolean aWins, List<String> log) { }

    private static double traitSum(ExpeditionManager ex, UUID owner, Copy k, boolean hp) {
        double t = 1;
        for (CompanionTraits.Trait tr : ex.traits(owner, k.companion(), k.index())) {
            t += hp ? tr.hp : tr.dmg;
        }
        return Math.max(0.3, t);
    }

    /** Kämpfer aus einem echten Begleiter-Exemplar des Spielers "owner". */
    public static Fighter of(ExpeditionManager ex, UUID owner, Copy k) {
        double lb = ex.levelBonus(ex.copyLevel(owner, k.companion(), k.index()));
        double rar = ex.rarity(owner, k.companion(), k.index()).stats;
        double hp = (20 + k.companion().stars * 12.0) * lb * rar * traitSum(ex, owner, k, true) * ex.roleHpFactor(k.companion());
        double atk = (4 + k.companion().stars * 1.5) * lb * rar * traitSum(ex, owner, k, false) * ex.roleDmgFactor(k.companion());
        if (ex.isSupporter(k.companion())) {
            atk = Math.max(1.0, atk * 0.3); // Unterstützer kämpfen kaum, halten aber etwas aus
        }
        return new Fighter(ex.trips().copyName(k.companion(), k.index()), k.companion().mobType(), hp, atk, ex.isDefender(k.companion()));
    }

    /** Kämpfer eines NPC: Stufe und Sterne bestimmen die Werte wie bei echten Begleitern. */
    public static Fighter npc(ExpeditionManager ex, String name, CompanionTypes.MobType type, int level, int stars, double power) {
        double lb = ex.levelBonus(level);
        double hp = (20 + stars * 12.0) * lb * power;
        double atk = (4 + stars * 1.5) * lb * power;
        return new Fighter(name, type, hp, atk, false);
    }

    /** Gesamtstärke wie bei den Duellen (Wurzel aus der Summe von Leben mal Angriff). */
    public static long strength(List<Fighter> team) {
        double sum = 0;
        for (Fighter f : team) {
            sum += f.hp() * f.atk();
        }
        return Math.round(Math.sqrt(sum));
    }

    /** Berechnet den Kampf. Bei Gleichstand gewinnt Seite B (der Verteidiger). */
    public static Result fight(List<Fighter> a, List<Fighter> b, Random r) {
        List<String> log = new ArrayList<>();
        int ia = 0;
        int ib = 0;
        if (a.isEmpty() || b.isEmpty()) {
            return new Result(!a.isEmpty(), log);
        }
        double hpA = a.get(0).hp();
        double hpB = b.get(0).hp();
        int rounds = 0;
        while (ia < a.size() && ib < b.size() && rounds < 600) {
            rounds++;
            Fighter fa = a.get(ia);
            Fighter fb = b.get(ib);
            double dmgA = fa.atk() * CompanionTypes.factor(fa.type(), fb.type()) * (0.85 + 0.3 * r.nextDouble());
            double dmgB = fb.atk() * CompanionTypes.factor(fb.type(), fa.type()) * (0.85 + 0.3 * r.nextDouble());
            hpB -= dmgA;
            hpA -= dmgB;
            if (hpA <= 0 || hpB <= 0) {
                if (hpA <= 0 && hpB <= 0) {
                    log.add(fa.name() + " und " + fb.name() + " gehen beide zu Boden.");
                } else if (hpB <= 0) {
                    log.add(fa.name() + " besiegt " + fb.name() + ".");
                } else {
                    log.add(fb.name() + " besiegt " + fa.name() + ".");
                }
                if (hpA <= 0) {
                    ia++;
                    if (ia < a.size()) {
                        hpA = a.get(ia).hp();
                    }
                }
                if (hpB <= 0) {
                    ib++;
                    if (ib < b.size()) {
                        hpB = b.get(ib).hp();
                    }
                }
            }
        }
        boolean aWins = ia < a.size() && (ib >= b.size() || hpA > hpB);
        return new Result(aWins, log);
    }
}
