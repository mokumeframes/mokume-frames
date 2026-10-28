# Original sound cues

All six cues were synthesised for this project with `tools/make_audio.py` on 28 October 2026. They use authored oscillators, seeded filtered noise, frequency sweeps and envelopes. No third-party samples, official audio, music or voice recordings are inputs.

The source is the Python generator plus `synthesis-manifest.json`; mono 44.1 kHz PCM WAV masters are preserved here. Runtime exports are Ogg Vorbis. Rebuild with Python, NumPy and soundfile/libsndfile (Vorbis enabled). The normal mod build needs none of these authoring tools.

`tell` is a short precursor; `charge` covers 1.35 seconds of the capstone windup; `burst` is the light attack transient; `heavy` is the capstone impact; `recoil` is a short directional rush; `select` is a quiet menu confirmation. Attack cues use Minecraft's Players sound category and spatial attenuation; UI confirmation follows the master/interface sound path. Every event has subtitle text.

The server's cast-start notification triggers an entity-bound tell; the authoritative active frame triggers the impact. Each tell is keyed to its cast identity and stopped on activation/cancellation, entity removal/death, dimension change, logout or expiry. One player's interruption does not stop other players' tells. These are finite one-shots with a 64-tell safety bound. Final perceptual mix and multiplayer listening have not been verified in-game.

Master peaks target -16 dBFS for tells, -10 dBFS for impacts, -12 dBFS for recoil and -20 dBFS for menu selection. Decoded Vorbis headroom and durations are checked by the generator. Numerical checks do not establish listening quality or in-game loudness balance.
