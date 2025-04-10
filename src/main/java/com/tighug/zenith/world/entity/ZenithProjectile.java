package com.tighug.zenith.world.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tighug.zenith.item.ModItem;

import java.lang.Math;
import java.util.Map;

import com.tighug.zenith.util.VariableAABB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import static com.tighug.zenith.Zenith.MODID;

@SuppressWarnings("resource")
public final class ZenithProjectile extends Projectile implements IEntityAdditionalSpawnData {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "zenith");
    public static final RegistryObject<EntityType<ZenithProjectile>> ZENITH_PROJECTILE = ENTITIES.register("zenith_projectile", () -> Builder.of(ZenithProjectile::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(32).updateInterval(-1).build("zenith_projectile"));
    public static final float[] SCALE = new float[20];
    private static final byte LIFE = 20;
    private @NotNull Map<Enchantment, Integer> enchantments;
    private float acceleration;
    private @NotNull Vec3 ODirection;
    private byte tickCount;
    private Vec3 OPosition;
    private float curvature;
    private float radius;
    private float yVelocity;

    static {
        for (byte b = 0; b < 20; ++b) {
            SCALE[b] = (float) Math.sin(Math.toRadians(18 * b));
        }
    }

    private ZenithProjectile(EntityType<? extends ZenithProjectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
        this.enchantments = Map.of();
        this.acceleration = 1.0F;
        this.ODirection = Vec3.ZERO;
        this.OPosition = Vec3.ZERO;
        this.yVelocity = 0.0F;
        this.curvature = 0.5F + random.nextFloat() * 2.0F;
        this.radius = (float) ((this.LIFE * this.curvature) / Math.PI / 2.0);
        this.noPhysics = true;
    }

    private static @NotNull ZenithProjectile newZenithProjectile(Level level , @NotNull Player player) {
        var p = new ZenithProjectile(ZENITH_PROJECTILE.get(), level);
        p.setEnchantments(EnchantmentHelper.getEnchantments(player.getMainHandItem()));
        p.setOwner(player);
        p.setPos(player.position().add(0.0, 0.8, 0.0).add(p.calculateViewVector(0.0F, player.getYRot()).scale(-1.0)));
        p.OPosition = p.position();
        p.setRot(player.getYRot() + 90.0F, 0.0F);
        p.yRotO = p.getYRot();
        return p;
    }

    private void setCurvature(float f) {
        this.curvature = f;
        this.radius = (float)((double)((float)this.LIFE * this.curvature) / Math.PI / (double)2.0F);
    }

    public static @NotNull ZenithProjectile of(Level level, @NotNull Player player) {
        ZenithProjectile zenithProjectile = newZenithProjectile(level, player);
        zenithProjectile.setODirection(zenithProjectile.calculateViewVector(0.0F, player.getYRot()));
        zenithProjectile.acceleration = (float)(20.0 * Math.cos(Math.toRadians(player.getXRot())) / zenithProjectile.radius);
        zenithProjectile.yVelocity = (float)(-(Math.sin(Math.toRadians(player.getXRot())) * 4.0));
        zenithProjectile.xRotO = zenithProjectile.getXRot();
        return zenithProjectile;
    }

    public static @NotNull ZenithProjectile of(Level level, @NotNull Player player, Vec3 vec3) {
        ZenithProjectile zenithProjectile = newZenithProjectile(level, player);
        vec3 = player.position().vectorTo(vec3).add(0.0, -0.8, 0.0);
        Vec3 vec31 = new Vec3(vec3.x, 0.0F, vec3.z);
        zenithProjectile.setODirection(vec31);
        double length = vec31.length();
        if (length > (double)(2.0F * zenithProjectile.radius)) {
            zenithProjectile.acceleration = (float)(length / zenithProjectile.radius / 2.0);
        }

        zenithProjectile.yVelocity = (float)(vec3.y / 10.0);
        return zenithProjectile;
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (!this.enchantments.isEmpty()) {
            ListTag listTag = new ListTag();
            this.enchantments.forEach((enchantment, integer) -> {
                CompoundTag tag = new CompoundTag();
                tag.putString("id", String.valueOf(EnchantmentHelper.getEnchantmentId(enchantment)));
                tag.putInt("lvl", integer);
                listTag.add(tag);
            });
            compoundTag.put("enchantments", listTag);
        }

        compoundTag.putDouble("OX", this.ODirection.x);
        compoundTag.putDouble("OY", this.ODirection.y);
        compoundTag.putDouble("OZ", this.ODirection.z);
        compoundTag.putByte("tickCount", this.tickCount);
        compoundTag.putDouble("OPX", this.OPosition.x);
        compoundTag.putDouble("OPY", this.OPosition.y);
        compoundTag.putDouble("OPZ", this.OPosition.z);
        compoundTag.putFloat("curvature", this.curvature);
        compoundTag.putFloat("acceleration", this.acceleration);
        compoundTag.putFloat("yVelocity", this.yVelocity);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (!compoundTag.getList("enchantments", 10).isEmpty()) {
            this.enchantments = EnchantmentHelper.deserializeEnchantments(compoundTag.getList("enchantments", 10));
        }

        this.ODirection = new Vec3(compoundTag.getDouble("OX"), compoundTag.getDouble("OY"), compoundTag.getDouble("OZ"));
        this.OPosition = new Vec3(compoundTag.getDouble("OPX"), compoundTag.getDouble("OPY"), compoundTag.getDouble("OPZ"));
        this.tickCount = compoundTag.getByte("tickCount");
        this.setCurvature(compoundTag.getFloat("curvature"));
        this.acceleration = compoundTag.getFloat("acceleration");
        this.yVelocity = compoundTag.getFloat("yVelocity");
    }

    public void setODirection(@NotNull Vec3 vec3) {
        double length = vec3.length();
        if (Math.abs(1.0 - length) > 0.01) {
            vec3 = vec3.scale(1.0 / length);
        }

        this.ODirection = vec3;
    }

    public void setEnchantments(@NotNull Map<Enchantment, Integer> map) {
        this.enchantments = map;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount == this.LIFE / 2) {
            this.yVelocity *= -1.0F;
        }

        Level level = this.level();
        if (!level.isClientSide) {
            Entity v3 = this.getOwner();
            if (v3 instanceof Player entity) {
                if (entity.getMainHandItem().is(ModItem.ZENITH.get())) {
                    AABB aabb = new VariableAABB(this.blockPosition()).inflate(2.5).toAABB();
                    float f = (float)entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    DamageSource ds = this.damageSources().playerAttack(entity);
                    level.getEntities((Entity)null, aabb, e -> e.isAttackable() && !e.equals(entity) && e.isAlive()).forEach((e) -> {
                        float f1 = f;
                        int p_44688_;
                        for(Enchantment enchantment : this.enchantments.keySet()) {
                            p_44688_ = this.enchantments.get(enchantment);
                            f1 += enchantment.getDamageBonus(p_44688_, e instanceof LivingEntity ? ((LivingEntity)e).getMobType() : MobType.UNDEFINED, entity.getMainHandItem());
                            enchantment.doPostHurt(entity, e, p_44688_);
                            if (enchantment == Enchantments.FIRE_ASPECT && e instanceof LivingEntity && !e.isOnFire()) {
                                e.setSecondsOnFire(4 * this.enchantments.get(enchantment));
                            }
                        }

                        if (e instanceof IModLivingEntity mob) {
                            mob.zenith$hurtNoInvulnerable(ds, f1);
                        } else {
                            e.invulnerableTime = 0;
                            e.hurt(ds, f1);
                        }
                    });
                }
            }
        }

        if (this.tickCount < this.LIFE && this.tickCount > -1) {
            float f1 = (float) (-this.getYRot() * (Math.PI / 180));
            Vector3d v3d = new Vector3d(Mth.sin(f1), 0, Mth.cos(f1)).mul(this.curvature);
            f1 = (float) (SCALE[tickCount] * (this.acceleration - 1.0) * this.curvature);
            v3d.add(ODirection.x * f1, 0, ODirection.z * f1).y += this.yVelocity;
            if (this.tickCount > this.LIFE / 2 && this.getOwner() != null) {
                var v3 = this.OPosition.vectorTo(this.getOwner().position().add(0.0, 0.8, 0.0).add(this.calculateViewVector(0.0F, this.getOwner().getYRot()).scale(-1.0))).toVector3f();
                if (v3.lengthSquared() > 6400) {
                    this.discard();
                    return;
                }
                v3 = v3.mul(1.0f / (this.LIFE - this.tickCount));
                this.OPosition = this.OPosition.add(v3.x, v3.y, v3.z);
                v3d.add(v3);
            }
            Vec3 vec3 = new Vec3(v3d.x, v3d.y, v3d.z);
            this.setDeltaMovement(vec3);
            this.move(MoverType.SELF, vec3);
            this.yRotO = this.getYRot();
            this.setRot(this.getYRot() - 360.0f / this.LIFE, 0.0F);
        } else if (this.tickCount > this.LIFE + 1) {
            this.discard();
            return;
        }

        ++this.tickCount;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void move(@NotNull MoverType moverType, @NotNull Vec3 vec3) {
        if (moverType == MoverType.SELF) {
            super.move(moverType, vec3);
        }

    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.getUUID());
        buffer.writeInt(this.getId());
        buffer.writeInt(this.getOwner() == null ? 0 : this.getOwner().getId());
        buffer.writeDouble(this.ODirection.x);
        buffer.writeDouble(this.ODirection.y);
        buffer.writeDouble(this.ODirection.z);
        buffer.writeFloat(this.curvature);
        buffer.writeFloat(this.acceleration);
        buffer.writeFloat(this.getYRot());
        buffer.writeFloat(this.getXRot());
        buffer.writeFloat(this.yVelocity);
        buffer.writeByte(this.tickCount);
    }

    @OnlyIn(Dist.CLIENT)
    public void readSpawnData(@NotNull FriendlyByteBuf additionalData) {
        this.setUUID(additionalData.readUUID());
        this.setId(additionalData.readInt());
        this.setOwner(this.level().getEntity(additionalData.readInt()));
        if (this.getOwner() instanceof Player player) {
            player.playSound(com.tighug.zenith.Zenith.ZENITH_ATTACK.get(), 1.0F, 1.0F);
        }
        this.ODirection = new Vec3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        this.setCurvature(additionalData.readFloat());
        this.acceleration = additionalData.readFloat();
        this.setYRot(additionalData.readFloat());
        this.setXRot(additionalData.readFloat());
        this.OPosition = this.position();
        this.yVelocity = additionalData.readFloat();
        this.tickCount = additionalData.readByte();
    }

    @OnlyIn(Dist.CLIENT)
    public static final class ZenithProjectileRenderer extends EntityRenderer<ZenithProjectile> {
        public static final ResourceLocation ENTITY = new ResourceLocation(MODID, "textures/entity/zenith_projectile.png");
        private final ItemStack defaultInstance;

        @Override
        public void render(@NotNull ZenithProjectile zenithProjectile, float p_113840_, float p_113841_, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i) {
            VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(zenithProjectile)));
            this.renderEntity(vertexconsumer, zenithProjectile, poseStack);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-zenithProjectile.getYRot() - 45.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(2.0F, 2.0F, 2.0F);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BakedModel bakedmodel = itemRenderer.getModel(this.defaultInstance, zenithProjectile.level(), null, zenithProjectile.getId());
            itemRenderer.render(this.defaultInstance, ItemDisplayContext.FIXED, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedmodel);
            poseStack.popPose();
            super.render(zenithProjectile, p_113840_, p_113841_, poseStack, multiBufferSource, i);
        }

        private void renderEntity(VertexConsumer vertexconsumer, @NotNull ZenithProjectile zenithProjectile, @NotNull PoseStack poseStack) {
            double v = 360.0F / (float)zenithProjectile.LIFE;
            double currentAngle = v * (double)zenithProjectile.tickCount;
            if (currentAngle > 359.0) {
                currentAngle = 0.0;
            }

            int s = Math.min(45, (int)currentAngle);
            if (s > 0) {
                poseStack.pushPose();
                PoseStack.Pose posestack$pose = poseStack.last();
                Matrix4f matrix4f = posestack$pose.pose();
                Matrix3f matrix3f = posestack$pose.normal();
                poseStack.mulPose(Axis.YP.rotationDegrees(-zenithProjectile.getYRot() - 90.0F));
                poseStack.translate(0.0, 0.0, 1.0);
                double r = zenithProjectile.curvature + (Math.PI / 10D);
                double dr = 1.0F;
                int l = 15728880;
                float y_s = 0.0F;
                float y_e = 0.4F;
                float x_s = 0.0F;
                float u_s = 0.0F;
                float v_s = 1.0F;
                float v_e = 0.0F;
                double a = Math.toRadians(zenithProjectile.getYRot());
                var v2d = this.multiply(Math.sin(a), Math.cos(a), zenithProjectile.ODirection.x, zenithProjectile.ODirection.z);
                double du = (double)1.0F / Math.sin(Math.toRadians(s));
                float radians;
                double cos;
                double sin;
                float u_e;
                Vector2d add = new Vector2d();
                for(double i1 = 0.0; i1 < s; i1 += dr) {
                    radians = (float) Math.toRadians(i1);
                    cos = Mth.cos(radians);
                    sin = Mth.sin(radians);
                    u_e = (float)(sin * du);
                    add.set(v2d);
                    add = add.mul(Mth.sin((float) Math.toRadians(currentAngle - i1)) * (zenithProjectile.acceleration - 1.0)).add(-cos, -sin).mul(dr * r / v);
                    float x_e = (float)((double)x_s + add.x);
                    this.vertex(matrix4f, matrix3f, vertexconsumer, x_e, y_s, u_e, v_s, l);
                    this.vertex(matrix4f, matrix3f, vertexconsumer, x_s, y_s, u_s, v_s, l);
                    this.vertex(matrix4f, matrix3f, vertexconsumer, x_s, y_e, u_s, v_e, l);
                    this.vertex(matrix4f, matrix3f, vertexconsumer, x_e, y_e, u_e, v_e, l);
                    poseStack.translate(0.0, 0.0, add.y);
                    u_s = u_e;
                    x_s = x_e;
                }

                poseStack.popPose();
            }
        }

        private void vertex(Matrix4f matrix4f, Matrix3f matrix3f, @NotNull VertexConsumer vertexConsumer, float x, float z, float u, float v, int p_85970_) {
            vertexConsumer.vertex(matrix4f, x, (float) 0.0, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_85970_).normal(matrix3f, 1.0F, 1.0F, -1.0F).endVertex();
        }

        @Contract(pure = true)
        private @NotNull Vector2d multiply(double w1, double i1, double w2, double i2) {
            return new Vector2d(w1 * w2 - i1 * i2, w1 * i2 + w2 * i1);
        }

        public ZenithProjectileRenderer(EntityRendererProvider.Context p_174008_) {
            super(p_174008_);
            this.defaultInstance = ModItem.ZENITH.get().getDefaultInstance();
        }

        public @NotNull ResourceLocation getTextureLocation(@NotNull ZenithProjectile p_114482_) {
            return ENTITY;
        }
    }
}
