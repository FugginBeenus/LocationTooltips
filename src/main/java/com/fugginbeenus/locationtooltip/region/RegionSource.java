package com.fugginbeenus.locationtooltip.region;

/**
 * Where a region came from. Used to keep auto-generated regions (e.g. from
 * structures) from colliding with player-made ones, and to drive future
 * behaviour like HUD styling or "don't persist auto regions" policies.
 */
public enum RegionSource {
    /** Created by a player with the wand / commands. */
    PLAYER,
    /** Created by an operator/server (no individual owner). */
    SERVER,
    /** Auto-generated from a world structure (village, outpost, modded structure, ...). */
    STRUCTURE
}
