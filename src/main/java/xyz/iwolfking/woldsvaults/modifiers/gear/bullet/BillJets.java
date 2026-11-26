package xyz.iwolfking.woldsvaults.modifiers.gear.bullet;

import me.desht.pneumaticcraft.api.item.PNCUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.JetBootsHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/**
 * A Bullet Bill's jets, which are PneumaticCraft: Repressurized's own jet boots. Its JetBootsHandler only ever asks an
 * armor handler about the boots it's running on, so this answers for a Bill as though it wore a pair: so many jet boots
 * upgrades, always enough pressure and no air spent. The flying itself (thrust along the look, speed picked up in a dive
 * and lost in a climb, spooling up, hovering, stabilizers and smart hover, flying into walls) is then all PneumaticCraft's.
 * The boots' power setting is how a Bill cruises and dashes. PneumaticCraft isn't a required mod, so this class must only
 * be loaded with it present, which BulletBill checks before anyone can transform.
 */
final class BillJets implements ICommonArmorHandler {
    private static final int SPOOL_TICKS = 20;

    private final Player player;
    private final int upgrades;
    private final JetBootsHandler.JetBootsLocalState boots = new JetBootsHandler.JetBootsLocalState();
    private boolean firing;

    BillJets(Player player, BulletBill.Tuning tuning) {
        this.player = player;
        this.upgrades = tuning.upgrades();
        this.boots.smartHover = tuning.smartHover();
        this.boots.flightStabilizers = tuning.stabilizers();
        if (tuning.cruise() > 0.0F) {
            spoolUp();
        }
        JetBootsStateTracker.getTracker(player).setJetBootsState(player, true, false, false);
    }

    /** Sets the boots straight to full thrust, skipping their spool-up. */
    void spoolUp() {
        for (int i = 0; i < SPOOL_TICKS; i++) {
            this.boots.tickActive();
        }
    }

    /** Runs one tick of the boots, firing or not, at this power setting. */
    void tick(boolean firing, float power) {
        JetBootsHandler handler = CommonUpgradeHandlers.jetBootsHandler;
        if (firing != this.firing || firing != handler.getJetBootsSyncedState(this).isActive()) {
            this.firing = firing;
            handler.setJetBootsActive(this, firing);
        }
        this.boots.jetBootsPower = power;
        this.boots.setPrevJetBootsAirUsage(1);
        handler.tick(this, true);
    }

    void stop() {
        JetBootsHandler handler = CommonUpgradeHandlers.jetBootsHandler;
        handler.setJetBootsActive(this, false);
        handler.onToggle(this, false);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public int getUpgradeCount(EquipmentSlot slot, PNCUpgrade upgrade) {
        return slot == EquipmentSlot.FEET && upgrade == ModUpgrades.JET_BOOTS.get() ? this.upgrades : 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IArmorExtensionData> T getExtensionData(IArmorUpgradeHandler<T> handler) {
        return (T) this.boots;
    }

    @Override
    public boolean hasMinPressure(EquipmentSlot slot) {
        return true;
    }

    @Override
    public float getArmorPressure(EquipmentSlot slot) {
        return 10.0F;
    }

    @Override
    public float addAir(EquipmentSlot slot, int air) {
        return getArmorPressure(slot);
    }

    @Override
    public int getSpeedFromUpgrades(EquipmentSlot slot) {
        return 1;
    }

    @Override
    public boolean isArmorEnabled() {
        return true;
    }

    @Override
    public boolean upgradeUsable(IArmorUpgradeHandler<?> handler, boolean mustBeActive) {
        return true;
    }
}
