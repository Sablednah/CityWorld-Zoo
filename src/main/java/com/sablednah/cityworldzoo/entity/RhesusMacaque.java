package com.sablednah.cityworldzoo.entity;

import com.sablednah.cityworldzoo.ZooSounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * The Last of Us: the lab monkeys at the University of Eastern Colorado, infected but never turned. Small,
 * quick, fond of fruit. Its calls are cut from recordings Sable found (art/rhesus_macaque/sounds, through
 * tools/audio/clips.py): chatter for ambient, a sharp falling cry for hurt, the long screeches for death.
 * A baby's voice is pitched up by vanilla ({@code getVoicePitch}).
 */
public class RhesusMacaque extends ZooAnimal {

    public RhesusMacaque(EntityType<? extends RhesusMacaque> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected double strollSpeed() {
        return 1.2D;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZooSounds.RHESUS_MACAQUE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZooSounds.RHESUS_MACAQUE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ZooSounds.RHESUS_MACAQUE_DEATH.get();
    }
}
