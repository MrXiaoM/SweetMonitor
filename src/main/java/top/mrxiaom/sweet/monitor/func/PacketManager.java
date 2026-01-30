package top.mrxiaom.sweet.monitor.func;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCamera;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.monitor.SweetMonitor;

@AutoRegister(requirePlugins = "packetevents", priority = 999)
public class PacketManager extends AbstractModule {
    public PacketManager(SweetMonitor plugin) {
        super(plugin);
    }

    public void setCamera(Player player, int entityId) {
        PlayerManager manager = PacketEvents.getAPI().getPlayerManager();
        PacketWrapper<?> packet = new WrapperPlayServerCamera(entityId);
        manager.sendPacket(player, packet);
    }
}
