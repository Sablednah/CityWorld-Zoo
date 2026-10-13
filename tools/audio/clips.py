#!/usr/bin/env python3
"""Cut game-ready takes out of raw recordings: mono OGG Vorbis, trimmed, faded, levelled.

The CUTS table says, per animal, which stretch of which raw file (in art/<animal>/sounds/) becomes which take
under assets/cityworld_zoo/sounds/<animal>/. Levels are peak-normalised (dBFS) so takes of one event match;
every take gets a short fade in and out so a cut never clicks; stereo is mixed down (OpenAL only positions a
mono sound in the world). Sample rates are kept as recorded (the client resamples).

    python3 tools/audio/clips.py rhesus_macaque
    python3 tools/audio/clips.py --analyse art/rhesus_macaque/sounds   # list the calls in raw files, to choose cuts

Needs the soundfile library (pip install --user --break-system-packages soundfile).
"""
import glob, os, sys
import numpy as np
import soundfile as sf

ROOT = os.path.join(os.path.dirname(__file__), "..", "..")

# animal -> [(raw file, start s, end s, take name, peak dBFS)]
CUTS = {
    "rhesus_macaque": [
        # chatter: short calls, for the ambient event
        ("monkey-jungle.mp3", 0.18, 0.62, "ambient1", -6.0),
        ("monkey-jungle.mp3", 0.76, 1.80, "ambient2", -6.0),
        ("monkey.mp3", 0.88, 1.80, "ambient3", -6.0),
        # sharp, loud, falling: hurt
        ("monkey.mp3", 1.94, 2.18, "hurt1", -3.0),
        ("monkey.mp3", 0.46, 0.70, "hurt2", -3.0),
        # the long screeches: death
        ("monkey-noise-no-copyright.mp3", 0.12, 2.08, "death1", -1.0),
        ("monkey-sound.mp3", 0.04, 2.60, "death2", -1.0),
    ],
}

def cut(raw, start, end, peak_db, fade_in=0.01, fade_out=0.06):
    data, rate = sf.read(raw, always_2d=True)
    mono = data.mean(axis=1)
    a, b = int(start * rate), min(len(mono), int(end * rate))
    seg = mono[a:b].copy()
    fi, fo = int(fade_in * rate), int(fade_out * rate)
    seg[:fi] *= np.linspace(0, 1, fi)
    seg[-fo:] *= np.linspace(1, 0, fo)
    peak = np.abs(seg).max() or 1.0
    seg *= (10 ** (peak_db / 20)) / peak
    return seg, rate

def analyse(folder):
    for path in sorted(glob.glob(os.path.join(folder, "*.mp3")) + glob.glob(os.path.join(folder, "*.ogg")) + glob.glob(os.path.join(folder, "*.wav"))):
        data, rate = sf.read(path, always_2d=True)
        mono = data.mean(axis=1)
        hop = int(rate * 0.02)
        env = np.array([np.sqrt(np.mean(mono[i*hop:(i+1)*hop] ** 2)) for i in range(len(mono) // hop)])
        thr = env.max() * 0.12
        segs, on = [], None
        for i, e in enumerate(env):
            if e > thr and on is None: on = i
            elif e <= thr and on is not None: segs.append([on, i]); on = None
        if on is not None: segs.append([on, len(env)])
        merged = []
        for s in segs:
            if merged and s[0] - merged[-1][1] < 8: merged[-1][1] = s[1]
            else: merged.append(s)
        print(f"{os.path.basename(path)}: {len(mono)/rate:.2f}s {data.shape[1]}ch {rate}Hz")
        for a, b in [s for s in merged if s[1] - s[0] >= 6]:
            seg = mono[a*hop:b*hop]
            spec = np.abs(np.fft.rfft(seg * np.hanning(len(seg)))); freqs = np.fft.rfftfreq(len(seg), 1 / rate)
            print(f"  {a*hop/rate:6.2f}-{b*hop/rate:6.2f}s  {20*np.log10(np.sqrt(np.mean(seg**2))+1e-9):6.1f} dB  centroid {(spec*freqs).sum()/spec.sum():5.0f} Hz")

def build(animal):
    out_dir = os.path.join(ROOT, "src", "main", "resources", "assets", "cityworld_zoo", "sounds", animal)
    os.makedirs(out_dir, exist_ok=True)
    for raw, start, end, name, peak_db in CUTS[animal]:
        seg, rate = cut(os.path.join(ROOT, "art", animal, "sounds", raw), start, end, peak_db)
        out = os.path.join(out_dir, name + ".ogg")
        sf.write(out, seg, rate, format="OGG", subtype="VORBIS")
        print(f"{name}.ogg  {len(seg)/rate:.2f}s  from {raw} {start}-{end}s  {os.path.getsize(out)} bytes")

if __name__ == "__main__":
    if sys.argv[1] == "--analyse":
        analyse(sys.argv[2])
    else:
        build(sys.argv[1])
