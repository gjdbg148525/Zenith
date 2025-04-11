package com.tighug.zenith.item;

import com.tighug.zenith.util.VariableAABB;
import com.tighug.zenith.world.entity.ZenithProjectile;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public final class ModItem {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "zenith");
    public static final RegistryObject<Item> ZENITH = ITEMS.register("zenith", Zenith::new);

    private static final class Zenith extends SwordItem {
        private Zenith() {
            super(new ForgeTier(10, 10000, 0.0F, 9.0F, 50, Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.EMPTY), 0, -2.0F, (new Item.Properties()).rarity(Rarity.EPIC));
        }

        @Override
        public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (hand == InteractionHand.MAIN_HAND && !player.getCooldowns().isOnCooldown(this)) {
                player.getCooldowns().addCooldown(this, 4);
                if (!level.isClientSide) {
                    Vec3 lookAngle = player.getLookAngle();
                    Entity entity = null;
                    {
                        double d = 0.2;
                        Vec3 vec3;
                        double length;
                        var list = level.getEntities((Entity) null, new VariableAABB(player.position(), player.position().add(lookAngle.scale(50))).inflate(1.0).toAABB(), e -> e instanceof Enemy && e.isAlive() && e.isAttackable());
                        for(Entity entity1 : list) {
                            vec3 = player.position().vectorTo(entity1.position());
                            length = vec3.length();
                            if (length < 50.0) {
                                vec3 = vec3.scale(1.0 / length);
                                length = vec3.distanceTo(lookAngle);
                                if (vec3.distanceTo(lookAngle) < d) {
                                    d = length;
                                    entity = entity1;
                                }
                            }
                        }
                    }

                    if (entity == null) {
                        level.addFreshEntity(ZenithProjectile.of(level, player));
                    } else {
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.GLOWING, 8));
                        }

                        Vec3 vec3 = entity.position();
                        level.addFreshEntity(ZenithProjectile.of(level, player, vec3));
                    }
                }
                return InteractionResultHolder.success(itemStack);
            } else {
                return InteractionResultHolder.pass(itemStack);
            }
        }

        @Override
        public boolean onEntityItemUpdate(ItemStack stack, @NotNull ItemEntity entity) {
            entity.setNoGravity(true);
            entity.setGlowingTag(true);
            return super.onEntityItemUpdate(stack, entity);
        }

        @Override
        public int getDamage(ItemStack stack) {
            return 0;
        }

        @Override
        public void setDamage(ItemStack stack, int damage) {}

        @Override
        public boolean canApplyAtEnchantingTable(ItemStack stack, @NotNull Enchantment enchantment) {
            return !enchantment.isCurse() && enchantment.category != EnchantmentCategory.BREAKABLE && super.canApplyAtEnchantingTable(stack, enchantment);
        }

        @Override
        public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
            return true;
        }

        @Override
        public boolean canBeHurtBy(@NotNull DamageSource p_41387_) {
            return p_41387_.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        }
    }
}