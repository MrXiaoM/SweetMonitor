package top.mrxiaom.sweet.monitor.commands;
        
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.monitor.SweetMonitor;
import top.mrxiaom.sweet.monitor.func.AbstractModule;
import top.mrxiaom.sweet.monitor.func.MonitorManager;

import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetMonitor plugin) {
        super(plugin);
        registerCommand("sweetmonitor", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1 && "enter".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player;
            boolean self = false;
            if (args.length == 2) {
                player = Util.getOnlinePlayer(args[1]).orElse(null);
                if (player == null) {
                    return t(sender, "&e玩家不在线 (或不存在)");
                }
            } else {
                if (sender instanceof Player) {
                    player = (Player) sender;
                    self = true;
                } else {
                    return t(sender, "&c只有玩家才能执行该命令");
                }
            }
            MonitorManager manager = MonitorManager.inst();
            if (manager.isInMonitor(player)) {
                if (self) {
                    return t(sender, "&e你已经在视奸了");
                } else {
                    return t(sender, "&e玩家 " + player.getName() + " 已经在视奸了");
                }
            }
            plugin.getScheduler().runTask(() -> manager.enterMonitor(player));
            if (self) {
                return t(sender, "&a你已进入视奸状态");
            } else {
                return t(sender, "&a玩家&e " + player.getName() + " &a已进入视奸状态");
            }
        }
        if (args.length >= 1 && "leave".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player;
            boolean self = false;
            boolean keepLocation = false;
            for (String arg : args) {
                if (arg.equals("-k") || arg.equals("--keep")) {
                    keepLocation = true;
                    break;
                }
            }
            if (args.length == 2 && !args[1].equals("-k") && !args[1].equals("--k")) {
                player = Util.getOnlinePlayer(args[1]).orElse(null);
                if (player == null) {
                    return t(sender, "&e玩家不在线 (或不存在)");
                }
            } else {
                if (sender instanceof Player) {
                    player = (Player) sender;
                    self = true;
                } else {
                    return t(sender, "&c只有玩家才能执行该命令");
                }
            }
            MonitorManager manager = MonitorManager.inst();
            if (!manager.isInMonitor(player)) {
                if (self) {
                    return t(sender, "&e你没有在视奸");
                } else {
                    return t(sender, "&e玩家 " + player.getName() + " 没有在视奸");
                }
            }
            boolean restore = !keepLocation;
            plugin.getScheduler().runTask(() -> manager.leaveMonitor(player, restore));
            if (self) {
                return t(sender, "&a你已离开视奸状态");
            } else {
                return t(sender, "&a玩家&e " + player.getName() + " &a已离开视奸状态");
            }
        }
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            plugin.reloadConfig();
            return t(sender, "&a配置文件已重载");
        }
        return true;
    }

    private static final List<String> listArg0 = Lists.newArrayList();
    private static final List<String> listOpArg0 = Lists.newArrayList(
            "enter", "leave", "reload");
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(sender.isOp() ? listOpArg0 : listArg0, args[0]);
        }
        if (args.length == 2) {
            if (sender.isOp()) {
                if ("enter".equalsIgnoreCase(args[0]) || "leave".equalsIgnoreCase(args[0])) {
                    return null;
                }
            }
        }
        return Collections.emptyList();
    }

    public List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }
    public List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
