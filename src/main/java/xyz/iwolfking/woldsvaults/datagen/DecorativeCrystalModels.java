package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.core.Direction;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import xyz.iwolfking.woldsvaults.blocks.DecorativeCrystalBlock;
import xyz.iwolfking.woldsvaults.blocks.DecorativeCrystalBlock.Shape;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Generates the decorative crystal's models: plain boxes in 22.5 and 45 degree tilts, the same vocabulary as amethyst and
 * the real Simon's Set crystals. Each shape and variant lays out its pieces once at full Geode size from its own seed, and
 * each stage scales that layout down and leaves off the pieces it's too small for, so a crystal keeps its pose as it grows.
 * Original is the real crystal's plain column with rock chips at its foot. Cathedral packs parallel columns up to one tall
 * centre. Phantom is a see-through shell with smaller solid crystals inside. Every crystal, upright or tilted, is one box
 * ending in one bevel: four slopes rising from its very top edge to a flat centre.
 * The slopes are trapezoids, which only {@link xyz.iwolfking.woldsvaults.client.simon.FacetedModel} can hold.
 */
final class DecorativeCrystalModels {
    private static final float[] HEIGHTS = {4.5F, 9.0F, 19.0F, 32.0F};
    private static final float[] WIDTHS = {0.4F, 0.6F, 0.8F, 1.0F};
    private static final float CENTRE = 8.0F;
    private static final float MODEL_TOP = 32.0F;
    private static final float NUDGE = 0.03125F;
    private static final float TEXELS = 4.0F;
    private static final float BODY_WIDTH = 4.0F;
    private static final float BODY_BOTTOM = 8.0F;
    private static final float REGION = 4.0F;
    private static final float BEVEL_SHARE = 0.25F;
    private static final float BEVEL_ANGLE = 45.0F;

    private enum Skin {
        BODY, ROCK, SHELL
    }

    private record Piece(int minStage, boolean chip, float x, float y, float z, float dx, float dz, float length, Direction lean, float angle, Skin skin, float u) {
    }

    private DecorativeCrystalModels() {
    }

    static String name(String look, Shape shape, int stage, int variant) {
        return "block/decorative_crystal/" + look + "_" + shape.getSerializedName() + "_" + SimonCrystalEntity.STAGE_NAMES[stage] + "_" + variant;
    }

    static void generate(BlockStateProvider provider) {
        for (Shape shape : Shape.values()) {
            for (int variant = 0; variant < DecorativeCrystalBlock.VARIANTS; variant++) {
                List<Piece> layout = layout(shape, variant);
                for (int stage = SimonCrystalEntity.SEED; stage <= SimonCrystalEntity.GEODE; stage++) {
                    BlockModelBuilder geometry = provider.models().getBuilder(name("base", shape, stage, variant)).ao(false).texture("particle", "#crystal");
                    FacetedModelBuilder<BlockModelBuilder> facets = geometry.customLoader(FacetedModelBuilder::begin);
                    BlockModelBuilder boxes = new BlockModelBuilder(provider.modLoc("decorative_crystal_boxes"), provider.models().existingFileHelper);
                    new Drawing(boxes, facets, stage).draw(layout);
                    facets.elements(boxes.toJson().get("elements"));
                    for (SimonCrystalEntity.Color color : SimonCrystalEntity.Color.values()) {
                        textured(provider, geometry, color.id, shape, stage, variant);
                    }
                    textured(provider, geometry, "prismatic", shape, stage, variant);
                }
            }
        }
    }

    private static void textured(BlockStateProvider provider, BlockModelBuilder geometry, String look, Shape shape, int stage, int variant) {
        BlockModelBuilder model = provider.models().getBuilder(name(look, shape, stage, variant)).parent(geometry).texture("crystal", provider.modLoc("block/simon_crystal/" + look));
        if (shape == Shape.PHANTOM) {
            model.texture("shell", provider.modLoc("block/simon_crystal/" + look + "_shell"));
        }
    }

    private static List<Piece> layout(Shape shape, int variant) {
        Random random = new Random((shape.ordinal() + 1) * 341873128712L + (variant + 1) * 132897987541L);
        Layout layout = new Layout(random);
        switch (shape) {
            case ORIGINAL -> layout.original();
            case CATHEDRAL -> layout.cathedral();
            case PHANTOM -> layout.phantom();
        }
        return layout.pieces;
    }

    private record Layout(Random random, List<Piece> pieces) {
        Layout(Random random) {
            this(random, new ArrayList<>());
        }

        private float between(float low, float high) {
            return low + this.random.nextFloat() * (high - low);
        }

        private Direction side() {
            return Direction.from2DDataValue(this.random.nextInt(4));
        }

        private float textureColumn(float width) {
            return this.random.nextInt((int) ((BODY_WIDTH - width / TEXELS) * TEXELS) + 1) / TEXELS;
        }

        private void spike(int minStage, float x, float y, float z, float dx, float dz, float length, Direction lean, float angle, Skin skin) {
            this.pieces.add(new Piece(minStage, false, x, y, z, dx, dz, Math.min(length, MODEL_TOP - y), lean, angle, skin, this.textureColumn(Math.max(dx, dz))));
        }

        private void column(int minStage, float x, float z, float width, float height, Skin skin) {
            this.spike(minStage, x, 0.0F, z, width, width, height, null, 0.0F, skin);
        }

        private void chips(int count) {
            float start = between(0.0F, 6.28F);
            for (int i = 0; i < count; i++) {
                float around = start + i * 6.28F / count + between(-0.4F, 0.4F);
                float distance = between(7.0F, 9.0F);
                float size = between(1.5F, 2.5F);
                this.pieces.add(new Piece(i == 0 ? 1 : i < 3 ? 2 : 3, true, CENTRE + (float) Math.cos(around) * distance, 0.0F, CENTRE + (float) Math.sin(around) * distance,
                        size, size, size * 0.8F, null, i % 2 == 0 ? 22.5F : 0.0F, Skin.ROCK, 0.0F));
            }
        }

        void original() {
            column(0, CENTRE, CENTRE, 10.0F, MODEL_TOP, Skin.BODY);
            chips(3 + this.random.nextInt(3));
        }

        void cathedral() {
            float core = 6 + this.random.nextInt(2);
            column(0, CENTRE + this.random.nextInt(3) - 1, CENTRE + this.random.nextInt(3) - 1, core, 30 + this.random.nextInt(3), Skin.BODY);
            int around = 6 + this.random.nextInt(3);
            float start = between(0.0F, 6.28F);
            for (int i = 0; i < around; i++) {
                float angle = start + i * 6.28F / around + between(-0.2F, 0.2F);
                float distance = between(5.5F, 8.0F);
                float width = 3.5F + this.random.nextInt(3);
                column(i == 0 ? 0 : i == 1 ? 1 : i <= 3 ? 2 : 3, CENTRE + (float) Math.cos(angle) * distance, CENTRE + (float) Math.sin(angle) * distance, width, 11 + this.random.nextInt(15), Skin.BODY);
            }
            if (this.random.nextBoolean()) {
                Direction lean = side();
                float length = 12 + this.random.nextInt(7);
                spike(2, CENTRE + lean.getStepX() * 5.0F, 0.0F, CENTRE + lean.getStepZ() * 5.0F, 3.0F, 3.0F, length, lean, 22.5F, Skin.BODY);
            }
            chips(3 + this.random.nextInt(3));
        }

        void phantom() {
            float shell = 10 + this.random.nextInt(2);
            float height = 29 + this.random.nextInt(4);
            column(0, CENTRE, CENTRE, shell, height, Skin.SHELL);
            column(2, CENTRE, CENTRE, shell - 3.0F, height * between(0.72F, 0.84F), Skin.SHELL);

            float ghost = 4 + this.random.nextInt(2);
            Direction lean = this.random.nextBoolean() ? side() : null;
            float x = CENTRE + (lean == null ? between(-1.0F, 1.0F) : -lean.getStepX() * 2.5F);
            float z = CENTRE + (lean == null ? between(-1.0F, 1.0F) : -lean.getStepZ() * 2.5F);
            spike(0, x, 0.0F, z, ghost, ghost, lean == null ? 16 + this.random.nextInt(6) : 12 + this.random.nextInt(5), lean, 22.5F, Skin.BODY);
            if (this.random.nextBoolean()) {
                Direction beside = side();
                column(1, CENTRE + beside.getStepX() * 2.5F, CENTRE + beside.getStepZ() * 2.5F, 2.5F, 8 + this.random.nextInt(5), Skin.BODY);
            }

            Direction first = side();
            for (int i = 0, count = 1 + this.random.nextInt(3); i < count; i++) {
                Direction out = Direction.from2DDataValue(first.get2DDataValue() + i);
                float length = 11 + this.random.nextInt(6);
                float inset = shell / 2.0F;
                float along = between(-2.5F, 2.5F);
                spike(i == 0 ? 2 : 3, CENTRE + out.getStepX() * inset + out.getClockWise().getStepX() * along, 0.0F, CENTRE + out.getStepZ() * inset + out.getClockWise().getStepZ() * along,
                        3.0F, 3.0F, length, out, 22.5F, Skin.SHELL);
            }
            chips(3 + this.random.nextInt(3));
        }
    }

    private static final class Drawing {
        private final BlockModelBuilder model;
        private final FacetedModelBuilder<BlockModelBuilder> facets;
        private final int stage;
        private final float across;
        private final float up;
        private final Set<Float> planesX = new HashSet<>();
        private final Set<Float> planesY = new HashSet<>();
        private final Set<Float> planesBottom = new HashSet<>();
        private final Set<Float> planesZ = new HashSet<>();

        Drawing(BlockModelBuilder model, FacetedModelBuilder<BlockModelBuilder> facets, int stage) {
            this.model = model;
            this.facets = facets;
            this.stage = stage;
            this.across = WIDTHS[stage];
            this.up = HEIGHTS[stage] / MODEL_TOP;
        }

        void draw(List<Piece> layout) {
            for (Piece piece : layout) {
                if (piece.minStage <= this.stage) {
                    this.piece(piece);
                }
            }
        }

        private static float half(float value) {
            return Math.round(value * 2.0F) / 2.0F;
        }

        private static float quarter(float value) {
            return Math.round(value * 4.0F) / 4.0F;
        }

        private float distinct(Set<Float> planes, float value) {
            float plane = value;
            while (!planes.add(plane)) {
                plane += NUDGE;
            }
            return plane;
        }

        private void piece(Piece piece) {
            float x = quarter(CENTRE + (piece.x - CENTRE) * this.across);
            float z = quarter(CENTRE + (piece.z - CENTRE) * this.across);
            float foot = half(piece.y * this.up);
            float dx = Math.max(0.5F, half(piece.dx * this.across));
            float dz = Math.max(0.5F, half(piece.dz * this.across));
            float height = Math.max(0.5F, half(piece.length * this.up));
            if (piece.chip) {
                this.box(x, foot, z, dx, height, dz, piece, true);
                return;
            }
            float band = Math.min(dx, dz) * BEVEL_SHARE;
            float rise = band * (float) Math.tan(Math.toRadians(BEVEL_ANGLE));
            float[] top = this.box(x, foot, z, dx, Math.max(0.5F, height - rise), dz, piece, false);
            this.bevel(top, band, rise, piece, x, foot, z);
        }

        private void bevel(float[] top, float band, float rise, Piece piece, float originX, float originY, float originZ) {
            float x0 = top[0];
            float z0 = top[1];
            float x1 = top[2];
            float z1 = top[3];
            float low = top[4];
            float high = low + rise;
            float[] outerNW = tilted(piece, originX, originY, originZ, x0, low, z0);
            float[] outerNE = tilted(piece, originX, originY, originZ, x1, low, z0);
            float[] outerSE = tilted(piece, originX, originY, originZ, x1, low, z1);
            float[] outerSW = tilted(piece, originX, originY, originZ, x0, low, z1);
            float[] innerNW = tilted(piece, originX, originY, originZ, x0 + band, high, z0 + band);
            float[] innerNE = tilted(piece, originX, originY, originZ, x1 - band, high, z0 + band);
            float[] innerSE = tilted(piece, originX, originY, originZ, x1 - band, high, z1 - band);
            float[] innerSW = tilted(piece, originX, originY, originZ, x0 + band, high, z1 - band);
            String texture = piece.skin == Skin.SHELL ? "#shell" : "#crystal";

            float inner = 4.0F;
            float outer = inner + band / (float) Math.cos(Math.toRadians(BEVEL_ANGLE)) / TEXELS;
            float[] u = stops(x1 - x0, band);
            float[] v = stops(z1 - z0, band);
            this.facets.facet(join(outerNW, innerNW, innerNE, outerNE), new float[] {u[0], outer, u[1], inner, u[2], inner, u[3], outer}, texture);
            this.facets.facet(join(innerSW, outerSW, outerSE, innerSE), new float[] {u[1], inner, u[0], outer, u[3], outer, u[2], inner}, texture);
            this.facets.facet(join(outerNW, outerSW, innerSW, innerNW), new float[] {v[0], outer, v[3], outer, v[2], inner, v[1], inner}, texture);
            this.facets.facet(join(innerNE, innerSE, outerSE, outerNE), new float[] {v[1], inner, v[2], inner, v[3], outer, v[0], outer}, texture);
            this.facets.facet(join(innerNW, innerSW, innerSE, innerNE), new float[] {4.0F, 0.0F, 4.0F, 4.0F, 8.0F, 4.0F, 8.0F, 0.0F}, texture);
        }

        /**
         * Returns the positions along a slope's outer edge where it starts, where its inner edge starts and ends, and
         * where it ends.
         */
        private static float[] stops(float length, float band) {
            float left = 4.0F;
            return new float[] {left, left + band / TEXELS, left + (length - band) / TEXELS, left + length / TEXELS};
        }

        private static float[] join(float[]... corners) {
            float[] joined = new float[corners.length * 3];
            for (int i = 0; i < corners.length; i++) {
                System.arraycopy(corners[i], 0, joined, i * 3, 3);
            }
            return joined;
        }

        private static float signedAngle(Piece piece) {
            return piece.lean == Direction.WEST || piece.lean == Direction.SOUTH ? piece.angle : -piece.angle;
        }

        /** Returns where a point on an upright piece ends up once the piece has tilted about its foot. */
        private static float[] tilted(Piece piece, float originX, float originY, float originZ, float x, float y, float z) {
            if (piece.lean == null) {
                return new float[] {x, y, z};
            }
            double radians = Math.toRadians(signedAngle(piece));
            float cos = (float) Math.cos(radians);
            float sin = (float) Math.sin(radians);
            float px = x - originX;
            float py = y - originY;
            float pz = z - originZ;
            if (piece.lean.getAxis() == Direction.Axis.X) {
                return new float[] {originX + px * cos - py * sin, originY + px * sin + py * cos, z};
            }
            return new float[] {x, originY + py * cos - pz * sin, originZ + py * sin + pz * cos};
        }

        /** Adds a box and returns where it ended up, as x0, z0, x1, z1 and its top. */
        private float[] box(float x, float foot, float z, float dx, float dy, float dz, Piece piece, boolean capped) {
            boolean tilted = piece.lean != null || piece.angle != 0.0F;
            float x0 = x - dx / 2.0F;
            float x1 = x + dx / 2.0F;
            float z0 = z - dz / 2.0F;
            float z1 = z + dz / 2.0F;
            float y1 = foot + dy;
            boolean standing = !tilted && foot == 0.0F;
            float bottom = standing ? this.distinct(this.planesBottom, foot) : foot;
            if (!tilted) {
                x0 = this.distinct(this.planesX, x0);
                x1 = this.distinct(this.planesX, x1);
                z0 = this.distinct(this.planesZ, z0);
                z1 = this.distinct(this.planesZ, z1);
                y1 = this.distinct(this.planesY, y1);
            }
            ModelBuilder<BlockModelBuilder>.ElementBuilder element = this.model.element().from(x0, bottom, z0).to(x1, y1, z1);
            if (piece.lean != null) {
                element.rotation().origin(x, foot, z).axis(piece.lean.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X).angle(signedAngle(piece)).end();
            } else if (tilted) {
                element.rotation().origin(x, foot, z).axis(Direction.Axis.Y).angle(piece.angle).end();
            }
            for (Direction face : Direction.values()) {
                if ((face != Direction.DOWN || standing) && (face != Direction.UP || capped)) {
                    float[] uv = uv(face, piece.skin, dx, dy, dz, piece.u);
                    element.face(face).uvs(uv[0], uv[1], uv[2], uv[3]).texture(piece.skin == Skin.SHELL ? "#shell" : "#crystal").end();
                }
            }
            element.end();
            return new float[] {x0, z0, x1, z1, y1};
        }

        private static float[] uv(Direction face, Skin skin, float dx, float dy, float dz, float column) {
            boolean flat = face.getAxis() == Direction.Axis.Y;
            float width = Math.min((face.getAxis() == Direction.Axis.X ? dz : dx) / TEXELS, REGION);
            float height = (flat ? dz : dy) / TEXELS;
            float capped = Math.min(height, REGION);
            if (skin == Skin.ROCK) {
                return new float[] {8.0F, 4.0F, 8.0F + width, 4.0F + capped};
            }
            return flat ? new float[] {0.0F, 0.0F, width, capped} : new float[] {column, BODY_BOTTOM - height, column + width, BODY_BOTTOM};
        }
    }
}
