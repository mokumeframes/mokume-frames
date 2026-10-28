package dev.risingheroes.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoadoutPresetsTest {
    HeroProfile hero(){var p=new HeroProfile();HeroRules.assign(p,"explosion");p.currentUnlocks().add("ap_shot");return p;}
    @Test void presetRestoresSlotsWithoutRefreshingCooldownOrResources(){var p=hero();HeroRules.equip(p,1,"ap_shot",100);HeroRules.savePreset(p,"1",100);p.slots.set(1,"");p.cooldowns.put("ap_shot",900L);p.stamina=21;p.strain=67;HeroRules.loadPreset(p,"1",200);assertEquals("ap_shot",p.slots.get(1));assertEquals(900L,p.cooldowns.get("ap_shot"));assertEquals(21,p.stamina);assertEquals(67,p.strain);}
    @Test void presetsAreQuirkSpecificAndArchived(){var p=hero();HeroRules.savePreset(p,"1",100);HeroRules.assign(p,"synthetic_test_quirk");assertThrows(IllegalArgumentException.class,()->HeroRules.loadPreset(p,"1",100));HeroRules.assign(p,"explosion");HeroRules.loadPreset(p,"1",100);assertEquals("palm_burst",p.slots.get(0));}
    @Test void revokedTechniqueInvalidatesPresetWithoutMutatingCurrentSlots(){var p=hero();HeroRules.equip(p,1,"ap_shot",100);HeroRules.savePreset(p,"1",100);p.slots.set(1,"");p.currentUnlocks().remove("ap_shot");assertThrows(IllegalArgumentException.class,()->HeroRules.loadPreset(p,"1",100));assertEquals("",p.slots.get(1));}
    @Test void activeTrialAndCombatBlockPresetSwapping(){var p=hero();HeroRules.savePreset(p,"1",100);p.combatUntil=300;assertThrows(IllegalArgumentException.class,()->HeroRules.loadPreset(p,"1",200));p.combatUntil=0;p.activeTrial="precision";assertThrows(IllegalArgumentException.class,()->HeroRules.loadPreset(p,"1",200));}
    @Test void presetSnapshotsDoNotAliasLiveSlotsAndSurviveSerialization(){var p=hero();HeroRules.savePreset(p,"1",100);p.slots.set(0,"");var restored=HeroProfile.fromJson(Catalog.JSON.toJson(p));assertEquals("palm_burst",restored.loadoutPresets.get("explosion#1").get(0));}
    @Test void invalidPresetSlotCannotCreateUnboundedSavedData(){var p=hero();assertThrows(IllegalArgumentException.class,()->HeroRules.savePreset(p,"9999",100));assertTrue(p.loadoutPresets.isEmpty());}
}
