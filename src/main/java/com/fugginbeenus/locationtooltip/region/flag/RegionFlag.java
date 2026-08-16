package com.fugginbeenus.locationtooltip.region.flag;

public final class RegionFlag {
    public final String id;
    public final String displayName;
    public final boolean defaultValue;

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
