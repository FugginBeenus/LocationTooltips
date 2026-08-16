package com.fugginbeenus.locationtooltip.region.flag;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public static RegionFlag byId(String id) {
        return BY_ID.get(id);
    }

    public static Collection<RegionFlag> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }
}
