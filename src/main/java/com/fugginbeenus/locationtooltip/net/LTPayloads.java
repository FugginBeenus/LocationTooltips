package com.fugginbeenus.locationtooltip.net;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.fugginbeenus.locationtooltip.LocationTooltip.MOD_ID;

/**
 * Every packet as a typed record with explicit read/write, plus a {@link Def} describing its
 * id and codec. All buffer layout lives here; {@link LTNet} / {@link LTNetClient} carry the
 * (version-specific) registration and send glue, so porting to the 1.20.5+ CustomPayload
 * networking only touches those two files.
 */
public final class LTPayloads {
    private LTPayloads() {}

    private static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    /** A payload type: wire id + writer + reader. */
    public record Def<T>(Identifier id, BiConsumer<T, PacketByteBuf> writer, Function<PacketByteBuf, T> reader) {}

    // ===== S2C =====

    /** Open the region-naming screen for a finished wand selection. */
    public record OpenName(BlockPos a, BlockPos b) {
        void write(PacketByteBuf buf) {
            buf.writeBlockPos(a);
            buf.writeBlockPos(b);
        }
        static OpenName read(PacketByteBuf buf) {
            return new OpenName(buf.readBlockPos(), buf.readBlockPos());
        }
    }
    public static final Def<OpenName> OPEN_NAME = new Def<>(id("open_name"), OpenName::write, OpenName::read);

    /** Open the admin panel. */
    public record OpenAdminPanel() {
        void write(PacketByteBuf buf) {}
        static OpenAdminPanel read(PacketByteBuf buf) { return new OpenAdminPanel(); }
    }
    public static final Def<OpenAdminPanel> OPEN_ADMIN_PANEL = new Def<>(id("open_admin_panel"), OpenAdminPanel::write, OpenAdminPanel::read);

    /** HUD title update: the region name at the player's position. */
    public record RegionUpdate(String name) {
        void write(PacketByteBuf buf) { buf.writeString(name); }
        static RegionUpdate read(PacketByteBuf buf) { return new RegionUpdate(buf.readString(32767)); }
    }
    public static final Def<RegionUpdate> REGION_UPDATE = new Def<>(id("region_update"), RegionUpdate::write, RegionUpdate::read);

    /** Celebration effects after creating a region. */
    public record RegionCreated(String name, BlockPos min, BlockPos max) {
        void write(PacketByteBuf buf) {
            buf.writeString(name);
            buf.writeBlockPos(min);
            buf.writeBlockPos(max);
        }
        static RegionCreated read(PacketByteBuf buf) {
            return new RegionCreated(buf.readString(32767), buf.readBlockPos(), buf.readBlockPos());
        }
    }
    public static final Def<RegionCreated> REGION_CREATED = new Def<>(id("region_created_celebrate"), RegionCreated::write, RegionCreated::read);

    /** Live wand-selection box for the in-world renderer. */
    public record SelectionUpdate(BlockPos a, BlockPos b) {
        void write(PacketByteBuf buf) {
            buf.writeBlockPos(a);
            buf.writeBlockPos(b);
        }
        static SelectionUpdate read(PacketByteBuf buf) {
            return new SelectionUpdate(buf.readBlockPos(), buf.readBlockPos());
        }
    }
    public static final Def<SelectionUpdate> SELECTION_UPDATE = new Def<>(id("selection_update"), SelectionUpdate::write, SelectionUpdate::read);

    /** Clear the wand-selection box. */
    public record SelectionClear() {
        void write(PacketByteBuf buf) {}
        static SelectionClear read(PacketByteBuf buf) { return new SelectionClear(); }
    }
    public static final Def<SelectionClear> SELECTION_CLEAR = new Def<>(id("selection_clear"), SelectionClear::write, SelectionClear::read);

    /** One region row of the admin list. */
    public record RegionEntry(String id, String name, Identifier dim, BlockPos min, BlockPos max,
                              Map<String, Boolean> flags, String ownerName, String source) {
        void write(PacketByteBuf buf) {
            buf.writeString(id);
            buf.writeString(name);
            buf.writeIdentifier(dim);
            buf.writeBlockPos(min);
            buf.writeBlockPos(max);
            writeFlags(buf, flags);
            buf.writeString(ownerName);
            buf.writeString(source);
        }
        static RegionEntry read(PacketByteBuf buf) {
            return new RegionEntry(
                    buf.readString(32767), buf.readString(32767), buf.readIdentifier(),
                    buf.readBlockPos(), buf.readBlockPos(),
                    readFlags(buf), buf.readString(32767), buf.readString(32767));
        }
    }

    /** The admin region list. */
    public record AdminList(List<RegionEntry> entries) {
        void write(PacketByteBuf buf) {
            buf.writeVarInt(entries.size());
            for (RegionEntry e : entries) e.write(buf);
        }
        static AdminList read(PacketByteBuf buf) {
            int n = Math.max(0, buf.readVarInt());
            List<RegionEntry> entries = new ArrayList<>(n);
            for (int i = 0; i < n; i++) entries.add(RegionEntry.read(buf));
            return new AdminList(entries);
        }
    }
    public static final Def<AdminList> ADMIN_LIST = new Def<>(id("admin_list"), AdminList::write, AdminList::read);

    // ===== C2S =====

    /** Create a region from the naming screen. */
    public record CreateRegion(String name, BlockPos a, BlockPos b, Map<String, Boolean> flags) {
        void write(PacketByteBuf buf) {
            buf.writeString(name);
            buf.writeBlockPos(a);
            buf.writeBlockPos(b);
            writeFlags(buf, flags);
        }
        static CreateRegion read(PacketByteBuf buf) {
            return new CreateRegion(buf.readString(32767), buf.readBlockPos(), buf.readBlockPos(), readFlags(buf));
        }
    }
    public static final Def<CreateRegion> CREATE_REGION = new Def<>(id("create_region"), CreateRegion::write, CreateRegion::read);

    /** Ask for the admin region list; negative radius = all regions in all dimensions. */
    public record RequestAdminList(int radius) {
        void write(PacketByteBuf buf) { buf.writeVarInt(radius); }
        static RequestAdminList read(PacketByteBuf buf) { return new RequestAdminList(buf.readVarInt()); }
    }
    public static final Def<RequestAdminList> REQUEST_ADMIN_LIST = new Def<>(id("request_admin_list"), RequestAdminList::write, RequestAdminList::read);

    /** Rename a region and replace its flag overrides. */
    public record AdminRename(String id, String newName, Map<String, Boolean> flags) {
        void write(PacketByteBuf buf) {
            buf.writeString(id);
            buf.writeString(newName);
            writeFlags(buf, flags);
        }
        static AdminRename read(PacketByteBuf buf) {
            return new AdminRename(buf.readString(32767), buf.readString(32767), readFlags(buf));
        }
    }
    public static final Def<AdminRename> ADMIN_RENAME = new Def<>(id("admin_rename"), AdminRename::write, AdminRename::read);

    /** Delete a region. */
    public record AdminDelete(String id) {
        void write(PacketByteBuf buf) { buf.writeString(id); }
        static AdminDelete read(PacketByteBuf buf) { return new AdminDelete(buf.readString(32767)); }
    }
    public static final Def<AdminDelete> ADMIN_DELETE = new Def<>(id("admin_delete"), AdminDelete::write, AdminDelete::read);

    // ===== shared helpers =====

    static void writeFlags(PacketByteBuf buf, Map<String, Boolean> flags) {
        buf.writeVarInt(flags.size());
        for (Map.Entry<String, Boolean> e : flags.entrySet()) {
            buf.writeString(e.getKey());
            buf.writeBoolean(e.getValue());
        }
    }

    static Map<String, Boolean> readFlags(PacketByteBuf buf) {
        int n = Math.max(0, buf.readVarInt());
        Map<String, Boolean> flags = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            String id = buf.readString(32767);
            flags.put(id, buf.readBoolean());
        }
        return flags;
    }
}
