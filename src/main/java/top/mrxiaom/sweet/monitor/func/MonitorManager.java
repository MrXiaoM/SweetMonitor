package top.mrxiaom.sweet.monitor.func;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.monitor.SweetMonitor;
import top.mrxiaom.sweet.monitor.data.ActiveType;
import top.mrxiaom.sweet.monitor.data.Monitor;

import java.util.*;

@AutoRegister
public class MonitorManager extends AbstractModule implements Listener {
    private final Map<UUID, Monitor> monitors = new HashMap<>();
    private final Map<UUID, Monitor> monitorsByTarget = new HashMap<>();
    private final Set<String> blackList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<UUID, Long> inactiveStartTime = new HashMap<>();
    private final Set<ActiveType> inactiveEnable = new HashSet<>();
    private long watchMills;
    private String barTitle, barLastOne, barEmpty;
    private BarColor barColor;
    private BarStyle barStyle;
    private boolean barReversed;
    private long inactiveMills;
    private IRunTask timerTask;
    private int teleportDelay;
    public MonitorManager(SweetMonitor plugin) {
        super(plugin);
        registerEvents();
        for (Player player : Bukkit.getOnlinePlayers()) {
            markActive(player);
        }
    }

    private List<Player> getAvailablePlayers() {
        long now = System.currentTimeMillis();
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (monitors.containsKey(uuid)) continue;
            if (blackList.contains(player.getName())) continue;
            if (inactiveMills != 0) {
                long lastActive = inactiveStartTime.getOrDefault(uuid, now);
                if (lastActive + inactiveMills < now) continue;
            }
            if (player.hasPermission("sweet.monitor.ignore")) continue;
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
            if (monitor.target != null && monitor.player.getGameMode().equals(GameMode.SPECTATOR)) {
                monitor.player.setSpectatorTarget(monitor.target);
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
            long endTime = monitor.startTime + watchMills;
            double lastMills = Math.max(0, endTime - System.currentTimeMillis());
            double mills = Math.max(0, System.currentTimeMillis() - monitor.startTime);
            if (monitor.lastOnePlayer) {
                bar.setProgress(barReversed ? 1.0 : 0.0);
                bar.setTitle(ColorHelper.parseColor(PAPI.setPlaceholders(target, barLastOne)));
            } else {
                double time = mills / 1000.0;
                double timeReversed = lastMills / 1000.0;
                String strTime = String.format("%.1f", time);
                String strTimeInt = String.format("%d", (int) time);
                String strTimeReversed = String.format("%.1f", timeReversed);
                String strTimeReversedInt = String.format("%d", (int) timeReversed);
                bar.setTitle(ColorHelper.parseColor(PAPI.setPlaceholders(target, barTitle
                        .replace("%time%", strTime)
                        .replace("%time_int%", strTimeInt)
                        .replace("%time_reversed%", strTimeReversed)
                        .replace("%time_reversed_int%", strTimeReversedInt))));
                if (barReversed) {
                    bar.setProgress(Math.min(1.0, lastMills / watchMills));
                } else {
                    bar.setProgress(Math.min(1.0, mills / watchMills));
                }
            }
        }
        bar.setVisible(true);
    }

    private void resetTarget(Monitor monitor) {
        List<Player> players = getAvailablePlayers();
        monitor.lastOnePlayer = players.size() <= 1;
        Player oldTarget = monitor.target;
        if (oldTarget != null) {
            players.removeIf(it -> it.getUniqueId().equals(oldTarget.getUniqueId()));
        }
        Player target = players.isEmpty() ? oldTarget : random(players);
        plugin.getFoliaScheduler().runAtEntity(monitor.player, (t) -> {
            monitor.setTarget(target);
            if (target != null) {
                monitorsByTarget.put(target.getUniqueId(), monitor);
            }
        });
    }

    private void switchNewTarget(Monitor monitor) {
        monitor.startTime = System.currentTimeMillis();
        resetTarget(monitor);
        updateBossBar(monitor);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        watchMills = (long) Math.max(0, config.getDouble("monitor.watch-seconds-per-player") * 1000L);
        barTitle = config.getString("monitor.camera-bossbar.title", "");
        barLastOne = config.getString("monitor.camera-bossbar.last-one", "");
        barEmpty = config.getString("monitor.camera-bossbar.empty", "");
        barColor = Util.valueOr(BarColor.class, config.getString("monitor.camera-bossbar.color"), BarColor.WHITE);
        barStyle = Util.valueOr(BarStyle.class, config.getString("monitor.camera-bossbar.style"), BarStyle.SOLID);
        barReversed = config.getBoolean("monitor.camera-bossbar.reversed", true);

        blackList.clear();
        blackList.addAll(config.getStringList("monitor.blacklist"));

        inactiveMills = (long) Math.max(0, config.getDouble("monitor.ignore-inactive.seconds") * 1000L);
        inactiveEnable.clear();
        if (inactiveMills != 0) {
            for (String s : config.getStringList("monitor.ignore-inactive.enable")) {
                ActiveType activeType = Util.valueOr(ActiveType.class, s, null);
                if (activeType != null) {
                    inactiveEnable.add(activeType);
                }
            }
        }

        if (timerTask != null) {
            timerTask.cancel();
        }
        int timerInterval = Math.max(1, config.getInt("monitor.timer-interval", 1));
        timerTask = plugin.getScheduler().runTaskTimer(this::update, 1L, timerInterval);

        teleportDelay = Math.max(1, config.getInt("monitor.teleport-delay"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (inactiveMills != 0) {
            markActive(e.getPlayer());
        }
        for (Monitor monitor : monitors.values()) {
            if (getAvailablePlayers().size() > 1) {
                monitor.startTime = System.currentTimeMillis();
                monitor.lastOnePlayer = false;
            } else {
                monitor.lastOnePlayer = true;
            }
        }
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent e) {
        if (inactiveMills != 0 && inactiveEnable.contains(ActiveType.ANIMATION)) {
            markActive(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if (inactiveMills != 0 && e.getEntity() instanceof Player && inactiveEnable.contains(ActiveType.ATTACK)) {
            markActive((Player) e.getEntity());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (inactiveMills != 0 && inactiveEnable.contains(ActiveType.MOVE)) {
            markActive(e.getPlayer());
        }
    }

    public void markActive(Player player) {
        inactiveStartTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Monitor byTarget = monitorsByTarget.remove(player.getUniqueId());
        if (byTarget != null) {
            switchNewTarget(byTarget);
        }
        leaveMonitor(player);
        player.removeMetadata("SWEET_MONITOR_FLAG", plugin);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        World from = e.getFrom().getWorld();
        World to = e.getTo() == null ? null : e.getTo().getWorld();
        if (from == null || to == null) return;
        if (from.getName().equals(to.getName())) return;
        Player player = e.getPlayer();
        if (monitors.containsKey(player.getUniqueId())) {
            player.setMetadata("SWEET_MONITOR_FLAG", new FixedMetadataValue(plugin, System.currentTimeMillis()));
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        Monitor byTarget = monitorsByTarget.get(player.getUniqueId());
        if (byTarget != null) {
            plugin.getFoliaScheduler().runAtEntity(byTarget.player, (t) -> byTarget.setTarget(player));
            return;
        }
        Monitor monitor = monitors.get(player.getUniqueId());
        if (monitor != null) {
            player.setGameMode(GameMode.SPECTATOR);
            plugin.getFoliaScheduler().runAtEntityLater(monitor.player, (t) -> {
                player.setSpectatorTarget(null);
                player.setSpectatorTarget(monitor.target);
            }, teleportDelay);
        }
    }

    @EventHandler
    public void onGameModeChanged(PlayerGameModeChangeEvent e) {
        if (!e.getNewGameMode().equals(GameMode.SPECTATOR)) {
            Player player = e.getPlayer();
            if (isInMonitor(player)) {
                List<MetadataValue> list = player.getMetadata("SWEET_MONITOR_FLAG");
                long time = list.isEmpty() ? 0L : list.get(0).asLong();
                if (System.currentTimeMillis() - time > 3333L) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1200, 1, false, false));
                    leaveMonitor(player, false);
                }
            }
        }
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
        Monitor monitor = new Monitor(plugin, player);
        monitors.put(uuid, monitor);
        player.setGameMode(GameMode.SPECTATOR);
    }

    public void leaveMonitor(Player player) {
        leaveMonitor(player, true);
    }

    public void leaveMonitor(Player player, boolean restore) {
        Monitor monitor = monitors.remove(player.getUniqueId());
        if (monitor != null) {
            Player target = monitor.target;
            if (target != null) {
                monitorsByTarget.remove(target.getUniqueId());
            }
            monitor.bossBar.setVisible(false);
            monitor.bossBar.removeAll();
            if (restore) {
                monitor.restore();
            } else {
                plugin.getFoliaScheduler().runAtEntity(monitor.player, (t) -> monitor.setTarget(null));
            }
        }
    }

    public static <T> T random(List<T> list) {
        int size = list.size();
        if (size == 0) return null;
        if (size == 1) return list.get(0);
        return list.get(new Random().nextInt(size));
    }

    @Override
    public void onDisable() {
        watchMills = 0L;
        for (Monitor monitor : monitors.values()) {
            monitor.bossBar.setVisible(false);
            monitor.bossBar.removeAll();
            monitor.setTarget(null);
        }
        monitors.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removeMetadata("SWEET_MONITOR_FLAG", plugin);
        }
    }

    public static MonitorManager inst() {
        return instanceOf(MonitorManager.class);
    }
}
