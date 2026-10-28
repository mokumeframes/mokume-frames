package dev.risingheroes.core;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MeleeComboTest {
    @Test void pausesResetTheChainAndTheFinisherReturnsToJab(){assertEquals(1,MeleeCombo.next(0,110,111));assertEquals(2,MeleeCombo.next(1,120,146));assertEquals(0,MeleeCombo.next(1,120,147));assertEquals(0,MeleeCombo.next(2,120,121));}
    @Test void strikeWindowsAllowVanillaHurtRecoveryAndRemainShortRange(){for(var s:MeleeCombo.STRIKES){assertTrue(s.duration()>=10);assertTrue(s.startup()>0);assertTrue(s.range()<=3);assertTrue(s.stamina()>0);assertEquals(1,s.move().active());assertEquals("RAY",s.move().shape());}}
    @Test void basicStrikesAreSeparateFromQuirkUnlocks(){for(var s:MeleeCombo.STRIKES){assertTrue(MeleeCombo.owns(s.id()));assertThrows(IllegalArgumentException.class,()->Kits.owner(s.id()));}assertFalse(MeleeCombo.owns("palm_burst"));}
}
