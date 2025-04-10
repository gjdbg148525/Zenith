package com.tighug.zenith.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VariableAABB {
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public VariableAABB(double p_82295_, double p_82296_, double p_82297_, double p_82298_, double p_82299_, double p_82300_) {
        this.minX = Math.min(p_82295_, p_82298_);
        this.minY = Math.min(p_82296_, p_82299_);
        this.minZ = Math.min(p_82297_, p_82300_);
        this.maxX = Math.max(p_82295_, p_82298_);
        this.maxY = Math.max(p_82296_, p_82299_);
        this.maxZ = Math.max(p_82297_, p_82300_);
    }

    public VariableAABB(@NotNull BlockPos p_82305_) {
        this(p_82305_.getX(), p_82305_.getY(), p_82305_.getZ(), (p_82305_.getX() + 1), (p_82305_.getY() + 1), (p_82305_.getZ() + 1));
    }
//
//    public VariableAABB(@NotNull BlockPos p_82307_, @NotNull BlockPos p_82308_) {
//        this(p_82307_.getX(), p_82307_.getY(), p_82307_.getZ(), p_82308_.getX(), p_82308_.getY(), p_82308_.getZ());
//    }

    public VariableAABB(@NotNull Vec3 p_82302_, @NotNull Vec3 p_82303_) {
        this(p_82302_.x, p_82302_.y, p_82302_.z, p_82303_.x, p_82303_.y, p_82303_.z);
    }
 
    public double min(Direction.@NotNull Axis p_82341_) {
        return p_82341_.choose(this.minX, this.minY, this.minZ);
    }
 
    public double max(Direction.@NotNull Axis p_82375_) {
        return p_82375_.choose(this.maxX, this.maxY, this.maxZ);
    }
 
    public @NotNull VariableAABB contract(double p_82311_, double p_82312_, double p_82313_) {
        if (p_82311_ < 0.0D) {
            this.minX -= p_82311_;
        } else if (p_82311_ > 0.0D) {
            this.maxX -= p_82311_;
        }

        if (p_82312_ < 0.0D) {
            this.minY -= p_82312_;
        } else if (p_82312_ > 0.0D) {
            this.maxY -= p_82312_;
        }

        if (p_82313_ < 0.0D) {
            this.minZ -= p_82313_;
        } else if (p_82313_ > 0.0D) {
            this.maxZ -= p_82313_;
        }

        return this;
    }
 
    public @NotNull VariableAABB expandTowards(@NotNull Vec3 p_82370_) {
        return this.expandTowards(p_82370_.x, p_82370_.y, p_82370_.z);
    }
 
    public @NotNull VariableAABB expandTowards(double p_82364_, double p_82365_, double p_82366_) {
        if (p_82364_ < 0.0D) {
            this.minX += p_82364_;
        } else if (p_82364_ > 0.0D) {
            this.maxX += p_82364_;
        }

        if (p_82365_ < 0.0D) {
            this.minY += p_82365_;
        } else if (p_82365_ > 0.0D) {
            this.maxY += p_82365_;
        }

        if (p_82366_ < 0.0D) {
            this.minZ += p_82366_;
        } else if (p_82366_ > 0.0D) {
            this.maxZ += p_82366_;
        }
        return this;
    }
 
    public @NotNull VariableAABB inflate(double p_82378_, double p_82379_, double p_82380_) {
        this.minX -= p_82378_;
        this.maxX += p_82378_;
        this.minY -= p_82379_;
        this.maxY += p_82379_;
        this.minZ -= p_82380_;
        this.maxZ += p_82380_;
        return this;
    }
 
    public @NotNull VariableAABB inflate(double p_82401_) {
        return this.inflate(p_82401_, p_82401_, p_82401_);
    }
 
    public @NotNull VariableAABB intersect(@NotNull AABB p_82324_) {
        this.minX = Math.max(this.minX, p_82324_.minX);
        this.minY = Math.max(this.minY, p_82324_.minY);
        this.minZ = Math.max(this.minZ, p_82324_.minZ);
        this.maxX = Math.min(this.maxX, p_82324_.maxX);
        this.maxY = Math.min(this.maxY, p_82324_.maxY);
        this.maxZ = Math.min(this.maxZ, p_82324_.maxZ);
        return this;
    }
 
    public @NotNull VariableAABB minmax(@NotNull AABB p_82368_) {
        this.minX = Math.min(this.minX, p_82368_.minX);
        this.minY = Math.min(this.minY, p_82368_.minY);
        this.minZ = Math.min(this.minZ, p_82368_.minZ);
        this.maxX = Math.max(this.maxX, p_82368_.maxX);
        this.maxY = Math.max(this.maxY, p_82368_.maxY);
        this.maxZ = Math.max(this.maxZ, p_82368_.maxZ);
        return this;
    }
 
    public @NotNull VariableAABB move(double p_82387_, double p_82388_, double p_82389_) {
        this.minX += p_82387_;
        this.maxX += p_82387_;
        this.minY += p_82388_;
        this.maxY += p_82388_;
        this.minZ += p_82389_;
        this.maxZ += p_82389_;
        return this;
    }
 
    public @NotNull VariableAABB move(@NotNull BlockPos p_82339_) {
        return this.move(p_82339_.getX(), p_82339_.getY(), p_82339_.getZ());
    }
 
    public @NotNull VariableAABB move(Vec3 p_82384_) {
        return this.move(p_82384_.x, p_82384_.y, p_82384_.z);
    }
 
    public boolean intersects(@NotNull AABB p_82382_) {
        return this.intersects(p_82382_.minX, p_82382_.minY, p_82382_.minZ, p_82382_.maxX, p_82382_.maxY, p_82382_.maxZ);
    }
 
    public boolean intersects(double p_82315_, double p_82316_, double p_82317_, double p_82318_, double p_82319_, double p_82320_) {
        return this.minX < p_82318_ && this.maxX > p_82315_ && this.minY < p_82319_ && this.maxY > p_82316_ && this.minZ < p_82320_ && this.maxZ > p_82317_;
    }
 
    public boolean intersects(@NotNull Vec3 p_82336_, @NotNull Vec3 p_82337_) {
        return this.intersects(Math.min(p_82336_.x, p_82337_.x), Math.min(p_82336_.y, p_82337_.y), Math.min(p_82336_.z, p_82337_.z), Math.max(p_82336_.x, p_82337_.x), Math.max(p_82336_.y, p_82337_.y), Math.max(p_82336_.z, p_82337_.z));
    }
 
    public boolean contains(@NotNull Vec3 p_82391_) {
        return this.contains(p_82391_.x, p_82391_.y, p_82391_.z);
    }
 
    public boolean contains(double p_82394_, double p_82395_, double p_82396_) {
        return p_82394_ >= this.minX && p_82394_ < this.maxX && p_82395_ >= this.minY && p_82395_ < this.maxY && p_82396_ >= this.minZ && p_82396_ < this.maxZ;
    }
 
    public double getSize() {
        double d0 = this.getXsize();
        double d1 = this.getYsize();
        double d2 = this.getZsize();
        return (d0 + d1 + d2) / 3.0D;
    }
 
    public double getXsize() {
        return this.maxX - this.minX;
    }
 
    public double getYsize() {
        return this.maxY - this.minY;
    }
 
    public double getZsize() {
        return this.maxZ - this.minZ;
    }
 
    public @NotNull VariableAABB deflate(double p_165898_, double p_165899_, double p_165900_) {
        return this.inflate(-p_165898_, -p_165899_, -p_165900_);
    }
 
    public @NotNull VariableAABB deflate(double p_82407_) {
        return this.inflate(-p_82407_);
    }
 
    public double distanceToSqr(@NotNull Vec3 p_273572_) {
        double d0 = Math.max(Math.max(this.minX - p_273572_.x, p_273572_.x - this.maxX), 0.0D);
        double d1 = Math.max(Math.max(this.minY - p_273572_.y, p_273572_.y - this.maxY), 0.0D);
        double d2 = Math.max(Math.max(this.minZ - p_273572_.z, p_273572_.z - this.maxZ), 0.0D);
        return Mth.lengthSquared(d0, d1, d2);
    }
 
    public @NotNull String toString() {
        return toAABB().toString();
    }
 
    public boolean hasNaN() {
        return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
    }
 
    public @NotNull Vec3 getCenter() {
        return new Vec3(Mth.lerp(0.5D, this.minX, this.maxX), Mth.lerp(0.5D, this.minY, this.maxY), Mth.lerp(0.5D, this.minZ, this.maxZ));
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull AABB toAABB() {
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
