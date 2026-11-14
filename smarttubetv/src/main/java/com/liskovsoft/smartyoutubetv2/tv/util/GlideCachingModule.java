package com.liskovsoft.smartyoutubetv2.tv.util;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;

/**
 * https://bumptech.github.io/glide/doc/configuration.html#disk-cache<br/>
 * https://stackoverflow.com/questions/46108915/how-to-increase-the-cache-size-in-glide-android
 */
@GlideModule
public class GlideCachingModule extends AppGlideModule {
    private final static long CACHE_SIZE = 32 * 1024 * 1024; // MyTube: 32 MB of downsampled thumbnails (~1000 cards)
    private final static long LOW_RAM_THRESHOLD = 2_500L * 1024 * 1024;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //if (MyApplication.from(context).isTest())
        //    return; // NOTE: StatFs will crash on robolectric.

        // Limit cache size
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, CACHE_SIZE));

        // MyTube: on low-RAM devices (Fire TV Stick 4K) keep Glide's in-memory pools small. Defaults on API < 26 are
        // 2 screens of memory cache + 4 screens of bitmap pool (~50MB at 1080p ARGB). Thumbnails are RGB_565 now,
        // so 1.5 screens still hold several rows of cards.
        long ram = com.liskovsoft.sharedutils.helpers.DeviceHelpers.getDeviceRam(context);
        if (ram > 0 && ram < LOW_RAM_THRESHOLD) {
            builder.setMemorySizeCalculator(new com.bumptech.glide.load.engine.cache.MemorySizeCalculator.Builder(context)
                    .setMemoryCacheScreens(1.5f)
                    .setBitmapPoolScreens(1.5f)
                    .build());
        }
    }
}
