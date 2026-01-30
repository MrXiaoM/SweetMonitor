package top.mrxiaom.sweet.monitor;

import top.mrxiaom.pluginbase.func.language.Message;

import static top.mrxiaom.pluginbase.func.language.LanguageFieldAutoHolder.field;

public class Messages {
    public static final Message no_permission = field("&c你没有执行此操作的权限");
    public static final Message player__not_online = field("&e玩家不在线 (或不存在)");
    public static final Message player__only = field("&c只有玩家才能执行该命令");
    public static final Message command__enter__already__self = field("&e你已经在视奸了");
    public static final Message command__enter__already__other = field("&e玩家 %player% 已经在视奸了");
    public static final Message command__enter__success__self = field("&a你已进入视奸状态");
    public static final Message command__enter__success__other = field("&a玩家&e %player% &a已进入视奸状态");
    public static final Message command__leave__none__self = field("&e你没有在视奸");
    public static final Message command__leave__none__other = field("&e玩家 %player% 没有在视奸");
    public static final Message command__leave__success__self = field("&a你已离开视奸状态");
    public static final Message command__leave__success__other = field("&a玩家&e %player% &a已离开视奸状态");
    public static final Message command__reload = field("&a配置文件已重载");
}
