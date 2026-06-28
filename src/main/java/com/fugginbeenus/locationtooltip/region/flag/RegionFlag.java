package com.fugginbeenus.locationtooltip.region.flag;

/**
 * Definition of a single boolean region flag.
 *
 * A flag answers "is this action allowed here?" — so {@code defaultValue == true}
 * means vanilla behaviour (a fresh region changes nothing until an admin denies
 * something). Flags are identified by a stable string {@link #id} that is used in
 * both JSON storage and networking, so it must never change once shipped.
 *
 * This is intentionally tiny and string-id'd so mods and the structure-tagging
 * system can register their own flags later without touching this class.
 */
public final class RegionFlag {
    public final String id;            // stable id, e.g. "pvp" (used in storage + networking)
    public final String displayName;   // human-readable label for the UI
    public final boolean defaultValue; // value used when no containing region overrides it

    public RegionFlag(String id, String displayName, boolean defaultValue) {
        this.id = id;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "RegionFlag[" + id + "=" + defaultValue + "]";
    }
}
