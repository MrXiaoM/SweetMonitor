package top.mrxiaom.sweet.monitor.func;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.monitor.SweetMonitor;
import top.mrxiaom.sweet.monitor.data.Monitor;

import java.util.*;

@AutoRegister
public class MonitorManager extends AbstractModule implements Listener {
    private final Map<UUID, Monitor> monitors = new HashMap<>();
    private final Map<UUID, Monitor> monitorsByTarget = new HashMap<>();
    private final Set<String> blackList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private long watchMills;
    private String barTitle;
    private String barEmpty;
    private BarColor barColor;
    private BarStyle barStyle;
    private boolean barReversed;
    public MonitorManager(SweetMonitor plugin) {
        super(plugin);
        plugin.getScheduler().runTaskTimer(this::update, 20L, 5L);
        registerEvents();
    }

    private List<Player> getAvailablePlayers() {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (monitors.containsKey(player.getUniqueId())) continue;
            if (blackList.contains(player.getName())) continue;
            players.add(player);
        }
        return players;
    }

    private void update() {
        if (watchMills == 0L) return;
        long now = System.currentTimeMillis();
        for (Monitor monitor : monitors.values()) {
            long endTime = monitor.startTime + watchMills;
            if (now > endTime || monitor.target == null) {
                monitor.startTime = now;
                resetTarget(monitor);
            }
            updateBossBar(monitor);
        }
    }

    private void updateBossBar(Monitor monitor) {
        BossBar bar = monitor.bossBar;
        if (barTitle.isEmpty() || watchMills == 0L) {
            bar.setVisible(false);
            return;
        }
        if (!bar.getPlayers().contains(monitor.player)) {
            bar.addPlayer(monitor.player);
        }
        bar.setColor(barColor);
        bar.setStyle(barStyle);
        Player target = monitor.target;
        if (target == null) {
            bar.setTitle(ColorHelper.parseColor(PAPI.setPlaceholders(monitor.player, barEmpty)));
            bar.setProgress(barReversed ? 1.0 : 0.0);
        } else {
            bar.setTitle(ColorHelper.parseColor(PAPI.setPlaceholders(target, barTitle)));
            if (barReversed) {
                long endTime = monitor.startTime + watchMills;
                double lastMills = Math.max(0, endTime - System.currentTimeMillis());
                bar.setProgress(Math.min(1.0, lastMills / watchMills));
            } else {
                double mills = Math.max(0, System.currentTimeMillis() - monitor.startTime);
                bar.setProgress(Math.min(1.0, mills / watchMills));
            }
        }
        bar.setVisible(true);
    }

    private void resetTarget(Monitor monitor) {
        Player target = random(getAvailablePlayers());
        monitor.setTarget(target);
        if (target != null) {
            monitorsByTarget.put(target.getUniqueId(), monitor);
        }
    }

    private void switchNewTarget(Monitor monitor) {
        monitor.startTime = System.currentTimeMillis();
        resetTarget(monitor);
        updateBossBar(monitor);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        watchMills = (long) (config.getDouble("monitor.watch-seconds-per-player") * 1000L);
        barTitle = config.getString("monitor.camera-bossbar.title", "");
        barEmpty = config.getString("monitor.camera-bossbar.empty", "");
        barColor = Util.valueOr(BarColor.class, config.getString("monitor.camera-bossbar.color"), BarColor.WHITE);
        barStyle = Util.valueOr(BarStyle.class, config.getString("monitor.camera-bossbar.style"), BarStyle.SOLID);
        barReversed = config.getBoolean("monitor.camera-bossbar.reversed", true);

        blackList.clear();
        blackList.addAll(config.getStringList("monitor.blacklist"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Monitor byTarget = monitorsByTarget.remove(player.getUniqueId());
        if (byTarget != null) {
            switchNewTarget(byTarget);
        }
        leaveMonitor(player);
    }

    public boolean isInMonitor(Player player) {
        return monitors.containsKey(player.getUniqueId());
    }

    public void enterMonitor(Player player) {
        UUID uuid = player.getUniqueId();
        if (monitors.containsKey(uuid)) return;
        Monitor byTarget = monitorsByTarget.remove(uuid);
        if (byTarget != null) {
            switchNewTarget(byTarget);
        }
        Monitor monitor = new Monitor(player);
        monitors.put(uuid, monitor);
    }

    public void leaveMonitor(Player player) {
        Monitor monitor = monitors.remove(player.getUniqueId());
        if (monitor != null) {
            monitor.restore();
        }
    }

    public static <T> T random(List<T> list) {
        int size = list.size();
        if (size == 0) return null;
        if (size == 1) return list.get(0);
        return list.get(new Random().nextInt(size));
    }

    public static MonitorManager inst() {
        return instanceOf(MonitorManager.class);
    }
}
