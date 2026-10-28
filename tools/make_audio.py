"""Original deterministic synthesis. No samples, voices, songs or external audio inputs.

Requires numpy and soundfile (libsndfile with Vorbis). WAV masters and synthesis
parameters are retained; the normal Gradle build uses the included OGG exports.
"""
from pathlib import Path
import json, math
import numpy as np
import soundfile as sf

ROOT=Path(__file__).resolve().parents[1]
MASTER=ROOT/'art/audio'; RUNTIME=ROOT/'src/main/resources/assets/mha'
RATE=44100
SEED=271028

def noise(rng,n,width):
    raw=rng.standard_normal(n)
    return np.convolve(raw,np.ones(width)/width,'same')

def render(kind,duration,rng):
    t=np.arange(round(RATE*duration))/RATE
    n=len(t)
    if kind=='select':
        return (np.sin(2*np.pi*880*t)+.22*np.sin(2*np.pi*1320*t))*np.exp(-t*55)
    if kind in ('tell','charge'):
        progress=t/duration
        phase=2*np.pi*(190*t+650*t*t/(2*duration))
        pulse=.5+.5*np.sin(2*np.pi*(17*t+20*t*t))
        body=.5*np.sin(phase)+.15*np.sin(phase*2)+.18*noise(rng,n,7)*pulse
        return body*(.15+.85*progress**1.5)*np.minimum(1,t/.012)*np.minimum(1,(duration-t)/.04)
    if kind=='heavy':
        phase=2*np.pi*(42*t+40*.09*(1-np.exp(-t/.09)))
        return .8*np.sin(phase)*np.exp(-t*7)+noise(rng,n,15)*np.exp(-t*10)+.12*noise(rng,n,2)*np.exp(-t*40)
    if kind=='recoil':
        return (noise(rng,n,5)-.55*noise(rng,n,32))*np.exp(-t*22)+.3*np.sin(2*np.pi*(120*t+180*t*t))*np.exp(-t*26)
    phase=2*np.pi*(75*t+170*.035*(1-np.exp(-t/.035)))
    return .6*np.sin(phase)*np.exp(-t*23)+noise(rng,n,5)*np.exp(-t*30)+.12*noise(rng,n,1)*np.exp(-t*75)

def main():
    MASTER.mkdir(parents=True,exist_ok=True)
    specs=[('explosion.tell','tell',.20,-16),('explosion.charge','charge',1.35,-16),('explosion.burst','burst',.28,-10),('explosion.heavy','heavy',.72,-10),('explosion.recoil','recoil',.22,-12),('ui.select','select',.09,-20)]
    events={};report=[]
    for i,(event,kind,duration,peak_db) in enumerate(specs):
        rng=np.random.default_rng(SEED+i);samples=render(kind,duration,rng)
        # Short boundary fades avoid discontinuities. DC removal precedes peak normalisation.
        samples-=np.mean(samples);fade=min(round(.004*RATE),len(samples)//4)
        samples[:fade]*=np.linspace(0,1,fade);samples[-fade:]*=np.linspace(1,0,fade)
        samples*=10**(peak_db/20)/np.max(np.abs(samples))
        relative=event.replace('.','/');wav=MASTER/(event+'.wav');ogg=RUNTIME/'sounds'/(relative+'.ogg')
        ogg.parent.mkdir(parents=True,exist_ok=True)
        sf.write(wav,samples,RATE,subtype='PCM_16');sf.write(ogg,samples,RATE,format='OGG',subtype='VORBIS')
        decoded,rate=sf.read(ogg)
        assert rate==RATE and len(decoded)==len(samples) and np.isfinite(decoded).all() and max(abs(decoded))<1
        events[event]={'subtitle':'subtitles.mha.'+event,'sounds':[{'name':'mha:'+relative,'attenuation_distance':16 if event.startswith('explosion') else 8}]}
        report.append({'event':event,'kind':kind,'seconds':duration,'peak_dbfs_master':peak_db,'decoded_peak_dbfs':round(20*math.log10(max(abs(decoded))),2),'seed':SEED+i,'mono':True,'sample_rate':RATE,'master':str(wav.relative_to(ROOT)).replace('\\','/'),'runtime':str(ogg.relative_to(ROOT)).replace('\\','/')})
    (RUNTIME/'sounds.json').write_text(json.dumps(events,indent=2)+'\n')
    (MASTER/'synthesis-manifest.json').write_text(json.dumps(report,indent=2)+'\n')
    print('PASS: six original mono cues exported to Vorbis and decoded; duration, sample rate, finite samples and headroom checked.')
    for item in report:print(f"{item['event']}: {item['seconds']:.2f}s, decoded peak {item['decoded_peak_dbfs']} dBFS")

if __name__=='__main__':main()
