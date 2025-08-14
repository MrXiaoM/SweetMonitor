package top.mrxiaom.sweet.monitor.data;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.monitor.SweetMonitor;

public class Monitor {
    public final SweetMonitor plugin;
    public final Location oldLocation;
    public final GameMode oldGameMode;
    public final boolean oldFlying;
    public final Player player;
    public final BossBar bossBar;
    public @Nullable Player target;
    public long startTime = System.currentTimeMillis();

    public Monitor(SweetMonitor plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.oldLocation = player.getLocation();
        this.oldGameMode = player.getGameMode();
        this.oldFlying = player.isFlying();
        this.bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        this.bossBar.addPlayer(player);
        this.bossBar.setVisible(false);
    }

    public void restore() {
        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            player.setSpectatorTarget(null);
        }
        plugin.getFoliaScheduler().teleportAsync(player, oldLocation);
        player.setGameMode(oldGameMode);
        player.setFlying(oldFlying);
    }

    public void setTarget(Player target) {
        if (this.target == null && target == null) return;
        this.target = target;
        if (target != null) {
            World world = target.getWorld();
            if (!player.getWorld().getName().equals(world.getName())) {
                plugin.getFoliaScheduler().teleportAsync(player, target.getLocation());
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.SPECTATOR);
                player.setSpectatorTarget(null);
                player.setSpectatorTarget(target);
            }
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(null);
        }
    }
}
