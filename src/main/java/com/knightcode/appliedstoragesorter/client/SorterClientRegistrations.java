package com.knightcode.appliedstoragesorter.client;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class SorterClientRegistrations {
    private SorterClientRegistrations() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        SorterInitScreens.register(event);
    }
}
