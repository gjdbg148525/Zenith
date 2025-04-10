package com.tighug.zenith.mixin;

import com.tighug.zenith.world.entity.IModLivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({Mob.class})
public abstract class LivingEntityMixin extends LivingEntity implements IModLivingEntity {

    protected LivingEntityMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    public boolean zenith$hurtNoInvulnerable(DamageSource damageSource, float f) {
        this.invulnerableTime = 0;
        return super.hurt(damageSource, f);
    }
}
