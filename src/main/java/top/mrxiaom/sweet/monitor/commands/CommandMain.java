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
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.monitor.Messages;
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
                    return Messages.player__not_online.t(sender);
                }
            } else {
                if (sender instanceof Player) {
                    player = (Player) sender;
                    self = true;
                } else {
                    return Messages.player__only.t(sender);
                }
            }
            MonitorManager manager = MonitorManager.inst();
            if (manager.isInMonitor(player)) {
                if (self) {
                    return Messages.command__enter__already__self.t(sender);
                } else {
                    return Messages.command__enter__already__other.t(sender,
                            Pair.of("%player%", player.getName()));
                }
            }
            plugin.getScheduler().runTask(() -> manager.enterMonitor(player));
            if (self) {
                return Messages.command__enter__success__self.t(sender);
            } else {
                return Messages.command__enter__success__other.t(sender,
                        Pair.of("%player%", player.getName()));
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
                    return Messages.player__not_online.t(sender);
                }
            } else {
                if (sender instanceof Player) {
                    player = (Player) sender;
                    self = true;
                } else {
                    return Messages.player__only.t(sender);
                }
            }
            MonitorManager manager = MonitorManager.inst();
            if (!manager.isInMonitor(player)) {
                if (self) {
                    return Messages.command__leave__none__self.t(sender);
                } else {
                    return Messages.command__leave__none__other.t(sender,
                            Pair.of("%player%", player.getName()));
                }
            }
            boolean restore = !keepLocation;
            plugin.getScheduler().runTask(() -> manager.leaveMonitor(player, restore));
            if (self) {
                return Messages.command__leave__success__self.t(sender);
            } else {
                return Messages.command__leave__success__other.t(sender,
                        Pair.of("%player%", player.getName()));
            }
        }
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            plugin.reloadConfig();
            return Messages.command__reload.t(sender);
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
