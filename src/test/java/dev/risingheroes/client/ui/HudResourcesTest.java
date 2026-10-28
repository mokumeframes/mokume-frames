package dev.risingheroes.client.ui;
import dev.risingheroes.core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HudResourcesTest {
    @Test void everyKitHasReadableLabelsAndOnlyRelevantResources(){for(String id:Kits.ids()){var p=new HeroProfile();HeroRules.assign(p,id);var meters=HudResources.of(p);assertEquals("Stamina",meters.getFirst().name());assertTrue(meters.size()>=2&&meters.size()<=3);assertTrue(meters.stream().allMatch(m->m.text().contains("/")));assertEquals(id.equals("electrification")||id.equals("kinetic_reservoir")?3:2,meters.size());}}
    @Test void signedTemperatureAndKineticCapacityMatchRules(){var p=new HeroProfile();HeroRules.assign(p,"half_cold_half_hot");p.resources.put("temperature",-80.0);var t=HudResources.of(p).get(1);assertTrue(t.signed()&&t.warning());assertEquals("Temperature -80/100",t.text());HeroRules.assign(p,"kinetic_reservoir");assertEquals(60,HudResources.of(p).get(1).max());}
}
