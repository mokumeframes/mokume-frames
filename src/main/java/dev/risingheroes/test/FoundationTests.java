package dev.risingheroes.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("mha")
@PrefixGameTestTemplate(false)
public final class FoundationTests {
    @GameTest(template = "empty")
    public static void presentationLibrariesLoadOnDedicatedServer(GameTestHelper helper) {
        for (String id : new String[]{"photon", "ldlib2", "kilagraph", "playeranimator", "geckolib"}) {
            helper.assertTrue(ModList.get().isLoaded(id), "Required library not loaded: " + id);
        }
        helper.succeed();
    }
}
