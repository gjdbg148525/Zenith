package com.tighug.zenith;

import com.tighug.zenith.item.ModItem;
import com.tighug.zenith.world.entity.ZenithProjectile;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import static com.tighug.zenith.Zenith.MODID;

@Mod(MODID)
public class Zenith {
    public static final String MODID = "zenith";
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS;
    public static final RegistryObject<SoundEvent> ZENITH_ATTACK;

    public Zenith() {
        ModItem.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ZenithProjectile.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    static {
        SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
        ZENITH_ATTACK = SOUND_EVENTS.register("zenith_attack", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "zenith_attack")));
    }

    @EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
    public static class ModEvent {
        
        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void onFMLClientSetupEvent(FMLClientSetupEvent event) {
            EntityRenderers.register(ZenithProjectile.ZENITH_PROJECTILE.get(), ZenithProjectile.ZenithProjectileRenderer::new);
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void onBuildCreativeModeTabContentsEvent(@NotNull BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
                event.accept(ModItem.ZENITH);
            }
        }
    }
}
