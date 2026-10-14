package com.sablednah.cityworldzoo.entity;

import com.sablednah.cityworldzoo.ZooSounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** I Am Legend (the lions on the deer), 12 Monkeys (the lion on the hotel roof), Resident Evil Outbreak's Wild Things. Fights back. */
public class Lion extends ZooAnimal {

    public Lion(EntityType<? extends Lion> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D);
    }

    @Override
    protected boolean fightsBack() {
        return true;
    }

    @Override
    protected Stride stride() {
        return Stride.LION;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return BigCats.isMeat(stack);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZooSounds.LION_AMBIENT.get();
    }

    @Override
    protected SoundEvent getAngrySound() {
        return ZooSounds.LION_ANGRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZooSounds.LION_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ZooSounds.LION_DEATH.get();
    }
}
