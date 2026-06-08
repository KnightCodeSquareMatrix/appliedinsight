package com.knightcode.appliedstoragesorter.client.gui.widget;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class NewDavToggleButton extends ThemedAE2Button {
    private boolean state;
    private final String translationKey;
    private final Consumer<Boolean> onToggle;

    public NewDavToggleButton(int x, int y, int width, int height, String translationKey,
            boolean initialState, Consumer<Boolean> onToggle) {
        super(x, y, width, height,
                Component.translatable(translationKey + (initialState ? ".on" : ".off")),
                btn -> {
                });
        this.state = initialState;
        this.translationKey = translationKey;
        this.onToggle = onToggle;
    }

    @Override
    public void onPress() {
        this.state = !this.state;
        this.setMessage(Component.translatable(this.translationKey + (this.state ? ".on" : ".off")));
        this.onToggle.accept(this.state);
        super.onPress();
    }

    public boolean isToggled() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
        this.setMessage(Component.translatable(this.translationKey + (this.state ? ".on" : ".off")));
    }
}
