package com.fugginbeenus.locationtooltip.net;

import com.fugginbeenus.locationtooltip.util.LTId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.fugginbeenus.locationtooltip.LocationTooltip.MOD_ID;

public final class LTPayloads {
    private LTPayloads() {}

    private static ResourceLocation id(String path) {
        return LTId.of(MOD_ID, path);
    }

    public record Def<T>(ResourceLocation id, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader) {}

    public record OpenName(BlockPos a, BlockPos b) {
        void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(a);
            buf.writeBlockPos(b);
        }
        static OpenName read(FriendlyByteBuf buf) {
            return new OpenName(buf.readBlockPos(), buf.readBlockPos());
        }
    }
    public static final Def<OpenName> OPEN_NAME = new Def<>(id("open_name"), OpenName::write, OpenName::read);

    public record OpenAdminPanel() {
        void write(FriendlyByteBuf buf) {}
        static OpenAdminPanel read(FriendlyByteBuf buf) { return new OpenAdminPanel(); }
    }
    public static final Def<OpenAdminPanel> OPEN_ADMIN_PANEL = new Def<>(id("open_admin_panel"), OpenAdminPanel::write, OpenAdminPanel::read);

    public record RegionUpdate(String name) {
        void write(FriendlyByteBuf buf) { buf.writeUtf(name); }
        static RegionUpdate read(FriendlyByteBuf buf) { return new RegionUpdate(buf.readUtf(32767)); }
    }
    public static final Def<RegionUpdate> REGION_UPDATE = new Def<>(id("region_update"), RegionUpdate::write, RegionUpdate::read);

    public record RegionCreated(String name, BlockPos min, BlockPos max) {
        void write(FriendlyByteBuf buf) {
            buf.writeUtf(name);
            buf.writeBlockPos(min);
            buf.writeBlockPos(max);
        }
        static RegionCreated read(FriendlyByteBuf buf) {
            return new RegionCreated(buf.readUtf(32767), buf.readBlockPos(), buf.readBlockPos());
        }
    }
    public static final Def<RegionCreated> REGION_CREATED = new Def<>(id("region_created_celebrate"), RegionCreated::write, RegionCreated::read);

    public record SelectionUpdate(BlockPos a, BlockPos b) {
        void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(a);
            buf.writeBlockPos(b);
        }
        static SelectionUpdate read(FriendlyByteBuf buf) {
            return new SelectionUpdate(buf.readBlockPos(), buf.readBlockPos());
        }
    }
    public static final Def<SelectionUpdate> SELECTION_UPDATE = new Def<>(id("selection_update"), SelectionUpdate::write, SelectionUpdate::read);

    public record SelectionClear() {
        void write(FriendlyByteBuf buf) {}
        static SelectionClear read(FriendlyByteBuf buf) { return new SelectionClear(); }
    }
    public static final Def<SelectionClear> SELECTION_CLEAR = new Def<>(id("selection_clear"), SelectionClear::write, SelectionClear::read);

    public record RegionEntry(String id, String name, ResourceLocation dim, BlockPos min, BlockPos max,
                              Map<String, Boolean> flags, String ownerName, String source, boolean nameable) {
        void write(FriendlyByteBuf buf) {
            buf.writeUtf(id);
            buf.writeUtf(name);
            buf.writeResourceLocation(dim);
            buf.writeBlockPos(min);
            buf.writeBlockPos(max);
            writeFlags(buf, flags);
            buf.writeUtf(ownerName);
            buf.writeUtf(source);
            buf.writeBoolean(nameable);
        }
        static RegionEntry read(FriendlyByteBuf buf) {
            return new RegionEntry(
                    buf.readUtf(32767), buf.readUtf(32767), buf.readResourceLocation(),
                    buf.readBlockPos(), buf.readBlockPos(),
                    readFlags(buf), buf.readUtf(32767), buf.readUtf(32767), buf.readBoolean());
        }
    }

    public record AdminList(List<RegionEntry> entries) {
        void write(FriendlyByteBuf buf) {
            buf.writeVarInt(entries.size());
            for (RegionEntry e : entries) e.write(buf);
        }
        static AdminList read(FriendlyByteBuf buf) {
            int n = Math.max(0, buf.readVarInt());
            List<RegionEntry> entries = new ArrayList<>(n);
            for (int i = 0; i < n; i++) entries.add(RegionEntry.read(buf));
            return new AdminList(entries);
        }
    }
    public static final Def<AdminList> ADMIN_LIST = new Def<>(id("admin_list"), AdminList::write, AdminList::read);

    public record CreateRegion(String name, BlockPos a, BlockPos b, Map<String, Boolean> flags) {
        void write(FriendlyByteBuf buf) {
            buf.writeUtf(name);
            buf.writeBlockPos(a);
            buf.writeBlockPos(b);
            writeFlags(buf, flags);
        }
        static CreateRegion read(FriendlyByteBuf buf) {
            return new CreateRegion(buf.readUtf(32767), buf.readBlockPos(), buf.readBlockPos(), readFlags(buf));
        }
    }
    public static final Def<CreateRegion> CREATE_REGION = new Def<>(id("create_region"), CreateRegion::write, CreateRegion::read);

    public record RequestAdminList(int radius) {
        void write(FriendlyByteBuf buf) { buf.writeVarInt(radius); }
        static RequestAdminList read(FriendlyByteBuf buf) { return new RequestAdminList(buf.readVarInt()); }
    }
    public static final Def<RequestAdminList> REQUEST_ADMIN_LIST = new Def<>(id("request_admin_list"), RequestAdminList::write, RequestAdminList::read);

    public record AdminRename(String id, String newName, Map<String, Boolean> flags) {
        void write(FriendlyByteBuf buf) {
            buf.writeUtf(id);
            buf.writeUtf(newName);
            writeFlags(buf, flags);
        }
        static AdminRename read(FriendlyByteBuf buf) {
            return new AdminRename(buf.readUtf(32767), buf.readUtf(32767), readFlags(buf));
        }
    }
    public static final Def<AdminRename> ADMIN_RENAME = new Def<>(id("admin_rename"), AdminRename::write, AdminRename::read);

    public record PlayerNameRegion(String id, String newName) {
        void write(FriendlyByteBuf buf) {
            buf.writeUtf(id);
            buf.writeUtf(newName);
        }
        static PlayerNameRegion read(FriendlyByteBuf buf) {
            return new PlayerNameRegion(buf.readUtf(32767), buf.readUtf(32767));
        }
    }
    public static final Def<PlayerNameRegion> PLAYER_NAME_REGION =
            new Def<>(id("player_name_region"), PlayerNameRegion::write, PlayerNameRegion::read);

    public record AdminDelete(String id) {
        void write(FriendlyByteBuf buf) { buf.writeUtf(id); }
        static AdminDelete read(FriendlyByteBuf buf) { return new AdminDelete(buf.readUtf(32767)); }
    }
    public static final Def<AdminDelete> ADMIN_DELETE = new Def<>(id("admin_delete"), AdminDelete::write, AdminDelete::read);

    static void writeFlags(FriendlyByteBuf buf, Map<String, Boolean> flags) {
        buf.writeVarInt(flags.size());
        for (Map.Entry<String, Boolean> e : flags.entrySet()) {
            buf.writeUtf(e.getKey());
            buf.writeBoolean(e.getValue());
        }
    }

    static Map<String, Boolean> readFlags(FriendlyByteBuf buf) {
        int n = Math.max(0, buf.readVarInt());
        Map<String, Boolean> flags = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            String id = buf.readUtf(32767);
            flags.put(id, buf.readBoolean());
        }
        return flags;
    }
}
