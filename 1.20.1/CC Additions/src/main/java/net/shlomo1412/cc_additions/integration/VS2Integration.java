package net.shlomo1412.cc_additions.integration;

import net.minecraftforge.fml.ModList;

/**
 * Helper class to check if Valkyrien Skies 2 is installed.
 * This class does not import any VS2 classes to avoid ClassNotFoundError.
 */
public class VS2Integration {
    
    private static final String VS2_MOD_ID = "valkyrienskies";
    private static Boolean loaded = null;
    
    /**
     * Check if Valkyrien Skies 2 is loaded.
     * Result is cached after first call.
     */
    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(VS2_MOD_ID);
        }
        return loaded;
    }
}
