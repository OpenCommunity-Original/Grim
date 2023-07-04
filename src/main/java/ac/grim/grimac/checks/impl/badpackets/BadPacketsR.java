package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEditBook;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@CheckData(name = "BadPacketsR", description = "Prevents players from sending malformed book edits")
public class BadPacketsR extends Check implements PacketCheck {
    public BadPacketsR(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // This check hasn't been tested on versions older before 1.17
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_17)) return;

        if (event.getPacketType() == Client.EDIT_BOOK) {
            WrapperPlayClientEditBook wrapper = new WrapperPlayClientEditBook(event);
            if (!checkBookPacket(wrapper)) {
                event.setCancelled(true); // Always cancel, too dangerous not to.
                player.onPacketCancel();
            }
        }
    }

    private boolean checkBookPacket(WrapperPlayClientEditBook packet) {
        @Nullable String title = packet.getTitle();
        List<String> pages = packet.getPages();

        if (pages.size() > 100) { // impossible
            flag(true, false, "pages=" + packet.getPages().size());
            return false;
        } else if (title != null) {
            if (title.length() > 15) { // impossible
                flag(true, false, "titleLen=" + title.length());
                return false;
            }
            if (title.contains(ChatColor.COLOR_CHAR + "")) { // colored book names exploit
                flag(true, false, "color code in title");
            }
        }
        for (int i = 0; i < pages.size(); i++) {
            String page = pages.get(i);
            if (page.length() > 800) { // impossible
                flag(true, false, "pageLen=" + page.length() + " index=" + i);
                return false;
            }
        }
        return true;
    }
}