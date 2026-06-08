package com.knightcode.appliedstoragesorter.ae2.dav;

import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;

public class NewDavStorageProvider implements IStorageProvider {
    public static final int DAV_PRIORITY = 100000;

    private final NewDavStorage storage;

    public NewDavStorageProvider(NewDavStorage storage) {
        this.storage = storage;
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        mounts.mount(storage, DAV_PRIORITY);
    }
}
