#!/usr/bin/env bash
# Build CityWorld: Zoo and copy the jar into a CurseForge instance.
#
# The target instance is chosen from the jar's Minecraft version, so it follows whichever version branch
# is checked out: cityworld_zoo-0.1.0+mc26.2.jar -> the "26.2" instance. Every other instance on the same
# line that already carries the mod gets it too. An instance whose root holds .sablecraft-no-deploy (the
# ZARP pack) is never deployed to, even when named outright: a modpack ships released jars only.
#
# Usage:  ./deploy.sh
#         ZOO_INSTANCE="/path/to/instance" ./deploy.sh    # override the target
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
INSTANCES="/mnt/c/Users/darre/curseforge/minecraft/Instances"

# Any JDK that can launch gradle will do; the toolchain provisions the rest.
for candidate in "/home/sable/.gradle/jdks/eclipse_adoptium-25-amd64-linux.2" \
                 "/home/sable/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2" \
                 "$ROOT/../MobHealth-Forge/tools/jdk21"; do
    if [ -x "$candidate/bin/java" ]; then export JAVA_HOME="$candidate"; break; fi
done
if [ -z "${JAVA_HOME:-}" ]; then
    echo "!! No JDK found. There is no system Java on this machine." >&2
    exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"

echo ">> Building (JAVA_HOME=$JAVA_HOME)..."
"$ROOT/gradlew" build --console=plain -q

# The exact jar for this checkout's Minecraft version and mod version, never a glob (a glob with head -1
# sorts alphabetically and Chronicler deployed a stale 0.1.0 over a fresh 1.0.0 that way).
MC_BUILD="$(sed -n 's/^minecraft_version=//p' "$ROOT/gradle.properties" | tr -d '\r')"
MOD_VERSION="$(sed -n 's/^mod_version=//p' "$ROOT/gradle.properties" | tr -d '\r')"
JAR="$ROOT/build/libs/cityworld_zoo-${MOD_VERSION}+mc${MC_BUILD}.jar"
[ -f "$JAR" ] || { echo "!! No $(basename "$JAR") in build/libs" >&2; exit 1; }
JARNAME="$(basename "$JAR")"
unzip -l "$JAR" | grep -q "META-INF/neoforge.mods.toml" || { echo "!! $JARNAME has no META-INF/neoforge.mods.toml" >&2; exit 1; }

MC_TAG=""
[[ "$JARNAME" =~ \+mc([0-9.]+)\.jar$ ]] && MC_TAG="${BASH_REMATCH[1]}"
case "$MC_TAG" in
    ""|1.21.11) TARGET="$INSTANCES/MobHealth - Forge" ;;  # the 1.21.11 test instance
    *)          TARGET="$INSTANCES/$MC_TAG" ;;
esac
TARGETS=()
if [ -n "${ZOO_INSTANCE:-}" ]; then
    TARGETS+=("$ZOO_INSTANCE")
else
    TARGETS+=("$TARGET")
    WANT="${MC_TAG:-1.21.11}"
    for d in "$INSTANCES"/*/; do
        d="${d%/}"
        [ "$d" = "$TARGET" ] && continue
        ls "$d/mods"/cityworld_zoo-*.jar >/dev/null 2>&1 || continue
        ver="$(python3 -c "import json,sys; d=json.load(open(sys.argv[1],encoding='utf-8')); print(d.get('gameVersion') or d.get('baseModLoader',{}).get('minecraftVersion',''))" "$d/minecraftinstance.json" 2>/dev/null || true)"
        [ "$ver" = "$WANT" ] && TARGETS+=("$d")
    done
fi

# REFUSE if an instance is running. Windows does NOT lock the jar, so a copy silently succeeds and the live
# JVM dies the moment it lazily loads a class it had not touched. The instance name runs up to the next
# backslash, quote or " --" (the launcher passes --gameDir unquoted), not the next space.
RUNNING="$(powershell.exe -NoProfile -Command \
  "Get-CimInstance Win32_Process | Where-Object { \$_.Name -like 'java*' } | ForEach-Object { \
   \$m=[regex]::Match(\$_.CommandLine,'Instances\\\\([^\\\\\"]+?)(?= --|\\\\|\"|$)'); if (\$m.Success) { \$m.Groups[1].Value } }" \
  2>/dev/null | tr -d '\r' | sort -u || true)"

for INSTANCE in "${TARGETS[@]}"; do
    MODS="$INSTANCE/mods"
    NAME="$(basename "$INSTANCE")"
    if [ -e "$INSTANCE/.sablecraft-no-deploy" ]; then
        echo ">> Skipping '$NAME': it is marked .sablecraft-no-deploy (released jars only)."
        continue
    fi
    [ -d "$MODS" ] || { echo "!! Instance mods folder not found: $MODS" >&2; exit 1; }
    if echo "$RUNNING" | grep -qxF "$NAME"; then
        echo "!! '$NAME' is RUNNING. Refusing to overwrite a jar underneath a live game." >&2
        echo "!! Close Minecraft and run this again." >&2
        exit 1
    fi
    echo ">> Removing previous CityWorld: Zoo jars from '$NAME'..."
    rm -f "$MODS"/cityworld_zoo-*.jar
    # Copy to .new, verify, then rename: a reader never sees a half-written jar.
    cp "$JAR" "$MODS/$JARNAME.new"
    cmp -s "$JAR" "$MODS/$JARNAME.new" || { echo "!! Deployed jar does not match the build." >&2; exit 1; }
    unzip -t "$MODS/$JARNAME.new" >/dev/null 2>&1 || { echo "!! Deployed jar is not a valid zip." >&2; exit 1; }
    mv "$MODS/$JARNAME.new" "$MODS/$JARNAME"
    echo ">> Deployed $JARNAME ($(stat -c%s "$JAR") bytes) to '$NAME': $(unzip -p "$MODS/$JARNAME" META-INF/MANIFEST.MF | grep -E '^Build-Commit' | tr -d '\r')"
done
echo ">> Launch an instance in CurseForge to test."
