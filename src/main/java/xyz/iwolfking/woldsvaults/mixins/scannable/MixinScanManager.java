package xyz.iwolfking.woldsvaults.mixins.scannable;

import li.cil.scannable.api.scanning.ScanResult;
import li.cil.scannable.api.scanning.ScanResultProvider;
import li.cil.scannable.api.scanning.ScannerModule;
import li.cil.scannable.client.ScanManager;
import li.cil.scannable.common.capabilities.Capabilities;
import li.cil.scannable.common.config.CommonConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.*;

@Mixin(value = ScanManager.class, remap = false)
public abstract class MixinScanManager {
    @Shadow
    public static void cancelScan() {
    }

    @Shadow
    @Final
    private static Set<ScanResultProvider> collectingProviders;

    @Shadow
    @Nullable
    private static Vec3 lastScanCenter;
    @Shadow
    private static long currentStart;
    @Shadow
    @Final
    private static Map<ScanResultProvider, List<ScanResult>> pendingResults;
    @Shadow
    @Final
    private static Map<ScanResultProvider, List<ScanResult>> renderingResults;

    @Shadow
    private static void clear() {
    }

    @Shadow
    public static float computeRadius(long start, float duration) {
        return 0;
    }

    @Shadow
    public static int computeScanGrowthDuration() {
        return 0;
    }

    /**
     * @author iwolfking
     * @reason Hard code scan radius
     */
    @Overwrite
    public static void beginScan(Player player, List<ItemStack> stacks) {
        cancelScan();
        float scanRadius = 64;
        List<ScannerModule> modules = new ArrayList<>();

        for (ItemStack stack : stacks) {
            LazyOptional<ScannerModule> module = stack.getCapability(Capabilities.SCANNER_MODULE_CAPABILITY);
            Objects.requireNonNull(modules);
            module.ifPresent(modules::add);
        }

        for (ScannerModule module : modules) {
            ScanResultProvider provider = module.getResultProvider();
            if (provider != null) {
                collectingProviders.add(provider);
            }

            scanRadius = module.adjustGlobalRange(scanRadius);
        }

        if (!collectingProviders.isEmpty()) {
            Vec3 center = player.position();

            for (ScanResultProvider provider : collectingProviders) {
                provider.initialize(player, stacks, center, scanRadius, 40);
            }

        }
    }

    /**
     * @author iwolfking
     * @reason Adjust how long scan stays on screen
     */
    @Overwrite
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (lastScanCenter != null && currentStart >= 0L) {
                if (22000 < (int) (System.currentTimeMillis() - currentStart)) {
                    pendingResults.forEach((providerx, resultsx) -> resultsx.forEach(ScanResult::close));
                    pendingResults.clear();
                    synchronized (renderingResults) {
                        if (!renderingResults.isEmpty()) {
                            Iterator<Map.Entry<ScanResultProvider, List<ScanResult>>> iterator = renderingResults.entrySet().iterator();

                            while (iterator.hasNext()) {
                                Map.Entry<ScanResultProvider, List<ScanResult>> entry = (Map.Entry) iterator.next();
                                List<ScanResult> list = (List) entry.getValue();

                                for (int i = Mth.ceil((float) list.size() * 0.5F); i > 0; --i) {
                                    ((ScanResult) list.get(list.size() - 1)).close();
                                    list.remove(list.size() - 1);
                                }

                                if (list.isEmpty()) {
                                    iterator.remove();
                                }
                            }
                        }

                        if (renderingResults.isEmpty()) {
                            clear();
                        }

                    }
                } else if (!pendingResults.isEmpty()) {
                    float radius = computeRadius(currentStart, (float) computeScanGrowthDuration());
                    float sqRadius = radius * radius;
                    Iterator<Map.Entry<ScanResultProvider, List<ScanResult>>> iterator = pendingResults.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry<ScanResultProvider, List<ScanResult>> entry = (Map.Entry) iterator.next();
                        ScanResultProvider provider = (ScanResultProvider) entry.getKey();
                        List<ScanResult> results = (List) entry.getValue();

                        while (!results.isEmpty()) {
                            ScanResult result = (ScanResult) results.get(results.size() - 1);
                            Vec3 position = result.getPosition();
                            if (!(lastScanCenter.distanceToSqr(position) <= (double) sqRadius)) {
                                break;
                            }

                            results.remove(results.size() - 1);
                            synchronized (renderingResults) {
                                ((List) renderingResults.computeIfAbsent(provider, (p) -> new ArrayList())).add(result);
                            }
                        }

                        if (results.isEmpty()) {
                            iterator.remove();
                        }
                    }

                }
            }
        }
    }
}
