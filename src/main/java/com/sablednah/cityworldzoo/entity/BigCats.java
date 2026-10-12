package com.sablednah.cityworldzoo.entity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** What the carnivores eat: raw meat, as a wolf would take it. */
final class BigCats {
    static boolean isMeat(ItemStack stack) {
        return stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.CHICKEN) || stack.is(Items.MUTTON)
                || stack.is(Items.RABBIT) || stack.is(Items.COD) || stack.is(Items.SALMON);
    }

    private BigCats() {}
}
