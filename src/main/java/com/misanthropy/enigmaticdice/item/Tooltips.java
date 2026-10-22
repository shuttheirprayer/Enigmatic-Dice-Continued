package com.misanthropy.enigmaticdice.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

final class Tooltips {
    private Tooltips() {
    }

    static void withDetails(List<Component> tooltip, Consumer<List<Component>> details, Component... summary) {
        if (Screen.hasShiftDown()) {
            details.accept(tooltip);
        } else {
            tooltip.addAll(List.of(summary));
            tooltip.add(Component.translatable("tooltip.enigmaticdice.holdShift"));
        }
    }

    static Component blank() {
        return Component.literal(" ");
    }

    static String percent(double fraction) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(fraction * 100) + "%";
    }
}
