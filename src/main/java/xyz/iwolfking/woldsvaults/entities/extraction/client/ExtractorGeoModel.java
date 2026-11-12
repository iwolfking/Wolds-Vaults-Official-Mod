package xyz.iwolfking.woldsvaults.entities.extraction.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;

import java.util.HashSet;
import java.util.Set;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import java.util.regex.Matcher;

public class ExtractorGeoModel extends AnimatedGeoModel<ExtractorEntity> {
    private static final Set<String> MISSING_BONES = new HashSet<>();

    private static final String CHASSIS_PREFIX = "chassis_";
    private static final String ATTACH_PREFIX = "attach_";
    private static final String MOUNT_SUFFIX = "_mount";
    private static final float[] MOUNT_DEGREES = {0.0F, 180.0F, 270.0F};

    @Override
    public ResourceLocation getModelLocation(ExtractorEntity entity) {
        return WoldsVaults.id("geo/extractor.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ExtractorEntity entity) {
        return WoldsVaults.id("textures/entity/extractor.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ExtractorEntity entity) {
        return WoldsVaults.id("animations/extractor.animation.json");
    }

    /**
     * Hides every part-variant bone, unhides the ones the entity has equipped, then folds
     * in the procedural motion.
     *
     * <p>One geo, one texture, bone visibility does the modularity. The motion runs AFTER
     * {@code super}, so it composes with whatever the animation controllers have already
     * written rather than replacing it. Bones opt into motion by NAME — see
     * {@link ExtractorMotion} — which is what lets a new part animate without any
     * animation-file work at all.
     */
    @Override
    public void setCustomAnimations(ExtractorEntity entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        for (String bone : ExtractorEntity.ALL_VARIANT_BONES) {
            this.setBoneHidden(bone, true);
        }
        String chassis = null;
        int mount = 0;
        for (String raw : entity.getEquippedVariantBones()) {
            String bone = raw.trim();
            if (bone.isEmpty()) {
                continue;
            }
            this.setBoneHidden(bone, false);
            if (bone.startsWith(CHASSIS_PREFIX)) {
                chassis = bone;
            } else if (bone.startsWith(ATTACH_PREFIX) && mount < MOUNT_DEGREES.length) {
                this.setBoneRotationY(bone, MOUNT_DEGREES[mount]);
                mount++;
            }
        }
        if (chassis != null) {
            for (int i = 0; i < MOUNT_DEGREES.length; i++) {
                this.setBoneHidden(chassis + MOUNT_SUFFIX + i, i < mount);
            }
        } else if (mount > 0) {
            WoldLogger.error("Extractor has {} attachment(s) but no chassis bone; the empty mounts will show through.", mount);
        }
        this.applyMotion((float) (animationEvent.getAnimationTick() / 20.0D), entity.getDrillRamp());
    }

    /**
     * Turns an attachment onto the hardpoint it occupies.
     *
     * <p>Every attachment is modelled once, on the +X mount, pivoted on the rig's centre
     * line — so the other two hardpoints are the same bone turned about Y. That is what
     * keeps ten attachments at fifty bones instead of a hundred and fifty. The angles here
     * must match MOUNT_ROT90 in the generator's model.py, which pre-rotates the chassis
     * blanks by the same amounts so a blank and the module replacing it land together.
     */
    private void setBoneRotationY(String name, float degrees) {
        IBone bone = this.getAnimationProcessor().getBone(name);
        if (bone == null) {
            return;
        }
        bone.setRotationY(bone.getRotationY() + (float) Math.toRadians(degrees));
    }

    private void applyMotion(float seconds, float ramp) {
        // getAnimationProcessor() is a raw type here, so the bone list erases to Object
        for (Object entry : this.getAnimationProcessor().getModelRendererList()) {
            IBone bone = (IBone) entry;
            String suffix = ExtractorMotion.suffixOf(bone.getName());
            if (suffix == null) {
                continue;
            }
            float spin = (float) Math.toRadians(ExtractorMotion.spinDegrees(seconds, ramp));
            switch (suffix) {
                case ExtractorMotion.SPIN -> bone.setRotationY(bone.getRotationY() + spin);
                case ExtractorMotion.SPIN_X -> bone.setRotationX(bone.getRotationX() + spin);
                case ExtractorMotion.SPIN_Z -> bone.setRotationZ(bone.getRotationZ() + spin);
                case ExtractorMotion.SHAKE -> {
                    bone.setPositionX(bone.getPositionX() + ExtractorMotion.shakeX(seconds, ramp));
                    bone.setPositionY(bone.getPositionY() + ExtractorMotion.shakeY(seconds, ramp));
                    bone.setPositionZ(bone.getPositionZ() + ExtractorMotion.shakeZ(seconds, ramp));
                }
                case ExtractorMotion.PULSE -> {
                    float k = ExtractorMotion.pulseScale(seconds, ramp);
                    bone.setScaleX(bone.getScaleX() * k);
                    bone.setScaleY(bone.getScaleY() * k);
                    bone.setScaleZ(bone.getScaleZ() * k);
                }
                case ExtractorMotion.BOB -> bone.setPositionY(
                        bone.getPositionY() + ExtractorMotion.bobY(seconds));
                case ExtractorMotion.PUMP -> bone.setPositionY(
                        bone.getPositionY() + ExtractorMotion.pumpY(seconds, ramp));
                case ExtractorMotion.CHASE_KEY -> {
                    Matcher m = ExtractorMotion.CHASE.matcher(bone.getName());
                    if (m.find()) {
                        float k = ExtractorMotion.chaseScale(seconds, ramp,
                                Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
                        bone.setScaleX(bone.getScaleX() * k);
                        bone.setScaleY(bone.getScaleY() * k);
                        bone.setScaleZ(bone.getScaleZ() * k);
                    }
                }
                default -> {
                }
            }
        }
    }

    private void setBoneHidden(String name, boolean hidden) {
        IBone bone = this.getAnimationProcessor().getBone(name);
        if (bone == null) {
            if (MISSING_BONES.add(name)) {
                WoldLogger.error("Extractor rig has no bone named '{}'; that part variant cannot be toggled.", name);
            }
            return;
        }
        bone.setHidden(hidden);
    }
}
