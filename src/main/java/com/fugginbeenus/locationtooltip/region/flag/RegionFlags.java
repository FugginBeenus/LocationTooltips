package com.fugginbeenus.locationtooltip.region.flag;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of known {@link RegionFlag}s.
 *
 * Built-ins are registered statically below. Add-ons (modded protections, the
 * structure-tagging system, etc.) can register more at init time via
 * {@link #register(RegionFlag)}. Iteration order is insertion order so UIs can
 * render flags in a stable, sensible sequence.
 *
 * Values mean "allowed", and all built-ins default to {@code true}, so enabling
 * the flag system changes nothing until an admin explicitly denies an action.
 */
public final class RegionFlags {
    private RegionFlags() {}

    private static final Map<String, RegionFlag> BY_ID = new LinkedHashMap<>();

    public static RegionFlag register(RegionFlag flag) {
        BY_ID.put(flag.id, flag);
        return flag;
    }

    private static RegionFlag def(String id, String label, boolean def) {
        return register(new RegionFlag(id, label, def));
    }

    // ---- Built-in protection flags (true = action allowed) ----
    public static final RegionFlag PVP              = def("pvp",              "Allow PvP",              true);
    public static final RegionFlag MOB_SPAWNING     = def("mob-spawning",     "Allow Mob Spawning",     true);
    public static final RegionFlag BLOCK_BREAK      = def("block-break",      "Allow Block Breaking",   true);
    public static final RegionFlag BLOCK_PLACE      = def("block-place",      "Allow Block Placing",    true);
    public static final RegionFlag INTERACT         = def("interact",         "Allow Interaction",      true);
    public static final RegionFlag CONTAINER_ACCESS = def("container-access", "Allow Container Access", true);
    public static final RegionFlag ENTITY_INTERACT  = def("entity-interact",  "Allow Entity Interact",  true);
    public static final RegionFlag EXPLOSIONS       = def("explosions",       "Allow Explosions",       true);
    public static final RegionFlag FIRE_SPREAD      = def("fire-spread",      "Allow Fire Spread",      true);
    public static final RegionFlag MOB_GRIEFING     = def("mob-griefing",     "Allow Mob Griefing",     true);
    public static final RegionFlag ITEM_PICKUP      = def("item-pickup",      "Allow Item Pickup",      true);

    /** Look up a flag by id, or null if unknown (e.g. a modded flag that isn't loaded). */
    public static RegionFlag byId(String id) {
        return BY_ID.get(id);
    }

    /** All registered flags, in registration order. */
    public static Collection<RegionFlag> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }
}
