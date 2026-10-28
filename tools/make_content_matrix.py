from pathlib import Path
import csv
ROOT=Path(__file__).resolve().parents[1]
groups={
 'Main-series foundation':'Explosion|Half-Cold Half-Hot|Hardening|Electrification|Zero Gravity|Engine|Frog|Acid|Tape|Dark Shadow|Creation|Earphone Jack',
 'Expanded main-series':'Tail|Dupli-Arms|Navel Laser|Steel|Fierce Wings|Hellflame|Blueflame|Rabbit|Whirlwind|Brainwashing|Manifest|Wave Motion',
 'Advanced main-series':'One For All|All For One|Copy|Erasure|Permeation|Decay|Overhaul|Transform|Double|Compress|Warp Gate|Rewind',
 'Two Heroes':'Metal manipulation (Wolfram; localised name pending)',
 'Heroes Rising':'Weather Manipulation (Nine original power; collection researched separately)|Chimera|Slice|Mummification|Cell Activation|Hologram',
 "World Heroes' Mission":'Reflect|Soul|Beros ability (name pending)|Serpenters abilities (names pending)',
 "You're Next":'Alchemy|Overmodification',
 'Mod-original / non-canon':'Voidstep|Resonance|Glassweave|Vector Thread|Inkbound|Kinetic Reservoir|Aurora Forge|Verdant Pulse'
}
with (ROOT/'docs/CONTENT_MATRIX.csv').open('w',newline='',encoding='utf-8') as f:
 w=csv.writer(f);w.writerow(['Working name','Source medium / work','Continuity claim','Implemented','Tested','Polished','Source verification','Runtime token / ordinary pool'])
 for category,names in groups.items():
  for name in names.split('|'):
   current=name=='Explosion';original=category=='Mod-original / non-canon'
   w.writerow([name,category,'Mod-original / non-canon' if original else 'No additional continuity claim', 'Experimental server kit' if current else 'Planned','Core/server logic; visual/network checks pending' if current else 'No','No','Official palm-explosion principle and original user checked; episode/move localisation pending' if current else 'Original design proposal' if original else 'Research pending; working target from brief only','Development only' if current else 'Not registered'])
locations=['U.A. main building and entrance','Class 1-A and 1-B classrooms','U.A. offices and principal office','Cafeteria and infirmary','Support-course workshops','Ground Beta','Ground Gamma (distinct from Gym Gamma)','Gym Gamma','Sports Festival stadium','Heights Alliance dormitories','USJ and six disaster zones','Musutafu everyday districts and training beach',"Endeavor agency","Nighteye agency","Best Jeanist agency","Hawks agency","Fat Gum agency",'Hosu City','Kamino Ward and League hideouts','Forest training camp / Wild Wild Pussycats','Shie Hassaikai surface and underground','Deika City','Jaku Hospital and underground','Gunga Mountain Villa','Tartarus','Shiketsu High','Ketsubutsu Academy','Provisional licence examination venue (verify exact name)','Later-series arenas (spoiler tagged)','I-Island research, expo and tower','Nabu Island inhabited areas and encounters','Otheon and Humarise sites',"You're Next fortress and urban encounters"]
with (ROOT/'docs/REGION_BACKLOG.csv').open('w',newline='',encoding='utf-8') as f:
 w=csv.writer(f);w.writerow(['Requested region','Current implementation','Verification / access'])
 for name in locations:
  partial=any(s in name for s in ['main building','Class 1-A','Cafeteria','Support-course','Heights Alliance'])
  w.writerow([name,'Starter adaptation only; full canonical location unfinished' if partial else 'Planned','Exact layout/source validation pending. Only the authored starter region is currently reachable.'])
print('Preserved the complete requested quirk and region research backlog without registering unfinished content.')
