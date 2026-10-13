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
    "lion": [
        # rumbles and growls, and now and then a real roar, for the ambient event
        ("gargamel10-lion-growl-sound-effect-380314.mp3", 0.46, 2.60, "ambient1", -8.0),
        ("magiazzz-lion-552065.mp3", 0.08, 1.30, "ambient2", -8.0),
        ("magiazzz-lion-552065.mp3", 2.30, 3.50, "ambient3", -8.0),
        ("magiazzz-lion-552065.mp3", 4.20, 5.09, "ambient4", -8.0),
        ("dragon-studio-lion-roar-sound-effect-324751.mp3", 1.70, 3.10, "roar1", -4.0),
        # the warning when it turns on you
        ("dragon-studio-monster-growl-376892.mp3", 0.08, 2.30, "angry1", -2.0),
        ("dragon-studio-lion-roar-sound-effect-324751.mp3", 1.70, 3.10, "angry2", -2.0),
        # short sharp bites of roar: hurt
        ("pwlpl-powerful-lion-roar-sound-effect-high-quality-sfx-444190.mp3", 1.58, 2.00, "hurt1", -3.0),
        ("mightuser-sound-effect-of-lion-roar-ambience-hd-263684.mp3", 4.06, 4.90, "hurt2", -3.0),
        # the big roars: death
        ("mightuser-sound-effect-of-lion-roar-ambience-hd-263684.mp3", 0.02, 3.00, "death1", -1.0),
        ("pwlpl-powerful-lion-roar-sound-effect-high-quality-sfx-444190.mp3", 0.02, 1.40, "death2", -1.0),
    ],
    "tiger": [
        # chuffs and low rumbles for ambient, with a light roar now and then
        ("mollyroselee-tiger-chuffing-ai-generated-431319.mp3", 0.02, 1.60, "ambient1", -8.0),
        ("mollyroselee-tiger-chuffing-ai-generated-431319.mp3", 3.10, 3.80, "ambient2", -8.0),
        ("mollyroselee-tiger-chuffing-ai-generated-431319.mp3", 5.08, 6.60, "ambient3", -8.0),
        ("jerryblessed-deep-aggressive-tiger-growl-and-rumble-540365.mp3", 0.00, 0.84, "ambient4", -8.0),
        ("sherimfal-tiger-light-roar-t-293716.mp3", 1.40, 3.40, "roar1", -5.0),
        # the warning when it turns on you
        ("jerryblessed-deep-aggressive-tiger-growl-and-rumble-540365.mp3", 1.78, 4.90, "angry1", -2.0),
        ("freesound_community-tiger-roar-104166.mp3", 1.00, 2.30, "angry2", -2.0),
        ("jerryblessed-deep-aggressive-tiger-growl-and-rumble-540365.mp3", 10.20, 11.40, "angry3", -2.0),
        # short loud calls: hurt
        ("susan-lu4esm-tiger-439141.mp3", 1.86, 2.50, "hurt1", -3.0),
        ("susan-lu4esm-tiger-439141.mp3", 3.42, 4.24, "hurt2", -3.0),
        ("jerryblessed-deep-aggressive-tiger-growl-and-rumble-540365.mp3", 5.06, 5.54, "hurt3", -3.0),
        # long roars: death
        ("sherimfal-tiger-light-roar-t-293716.mp3", 3.76, 6.30, "death1", -1.0),
        ("susan-lu4esm-tiger-439141.mp3", 0.06, 1.16, "death2", -1.0),
    ],
    "brown_bear": [
        ("universfield-bear-growl-191995.mp3", 0.06, 1.06, "ambient1", -8.0),
    ],
    "elephant": [
        # rumbles and trumpets for ambient
        ("cartoon_music-elephant-trumpeting-amp-growlin-529233.mp3", 0.16, 2.10, "ambient1", -8.0),
        ("olenchic--154831.mp3", 0.58, 2.00, "ambient2", -8.0),
        ("olenchic--154831.mp3", 2.62, 3.72, "ambient3", -8.0),
        ("olenchic--154831.mp3", 5.00, 5.96, "ambient4", -8.0),
        # the trumpet as it turns on you
        ("freesound_community-elephant-trumpets-growls-6047.mp3", 0.08, 1.24, "angry1", -2.0),
        ("olenchic--154831.mp3", 0.58, 2.00, "angry2", -2.0),
        # screams: hurt
        ("freesound_community-elephant-scream-106989.mp3", 0.04, 1.16, "hurt1", -3.0),
        ("olenchic--154831.mp3", 5.00, 5.96, "hurt2", -3.0),
        # the long ones: death
        ("freesound_community-elephant-scream-fake-100274.mp3", 0.02, 2.40, "death1", -1.0),
        ("cartoon_music-elephant-trumpeting-amp-growlin-529233.mp3", 0.16, 2.30, "death2", -1.0),
    ],
    "deer": [
        # grunts, snorts and mews for the ambient event; the stag's roar rarely
        ("dragon-studio-deer-grunt-472371.mp3", 0.22, 1.00, "ambient1", -9.0),
        ("freesound_community-deer-meewing-01-31634.mp3", 2.88, 3.62, "ambient2", -12.0),
        ("freesound_community-deer-meewing-01-31634.mp3", 5.30, 6.26, "ambient3", -12.0),
        ("freesound_community-deer-meewing-01-31634.mp3", 11.78, 12.56, "ambient4", -12.0),
        ("nickype-the-call-of-red-stag_animal-sound-145885.mp3", 0.80, 3.20, "roar1", -5.0),
        # a fawn bleats
        ("freesound_community-fawn-bleats-98384.mp3", 0.10, 0.84, "ambient_baby1", -9.0),
        ("freesound_community-fawn-bleats-98384.mp3", 1.26, 2.06, "ambient_baby2", -9.0),
        ("freesound_community-fawn-bleats-98384.mp3", 5.54, 6.30, "ambient_baby3", -9.0),
        ("freesound_community-fawn-bleats-98384.mp3", 7.38, 7.92, "ambient_baby4", -9.0),
        # the alarm snort and a sharp mew: hurt
        ("dragon-studio-deer-snort-472365.mp3", 0.30, 0.92, "hurt1", -3.0),
        ("freesound_community-deer-meewing-01-31634.mp3", 9.82, 10.36, "hurt2", -4.0),
        # death: the stag's call, and the fawn's longest bleat
        ("nickype-the-call-of-red-stag_animal-sound-145885.mp3", 0.80, 3.30, "death1", -1.0),
        ("freesound_community-fawn-bleats-98384.mp3", 7.98, 9.04, "death2", -2.0),
    ],
    "giraffe": [
        # snorts; a giraffe says almost nothing a person can hear, so these play rarely
        ("jerryblessed-giraffe-snorting-and-chewing-leaves-540358.mp3", 1.60, 3.20, "ambient1", -10.0),
        ("jerryblessed-giraffe-snorting-and-chewing-leaves-540358.mp3", 3.26, 3.76, "ambient2", -10.0),
        ("jerryblessed-giraffe-snorting-and-chewing-leaves-540358.mp3", 6.26, 7.06, "ambient3", -10.0),
        # chewing leaves: the eating sound
        ("jerryblessed-giraffe-snorting-and-chewing-leaves-540358.mp3", 3.90, 6.06, "eat1", -10.0),
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
