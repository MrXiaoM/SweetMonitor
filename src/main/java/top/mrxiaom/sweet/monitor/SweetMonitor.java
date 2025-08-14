package top.mrxiaom.sweet.monitor;
        
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

public class SweetMonitor extends BukkitPlugin {
    public static SweetMonitor getInstance() {
        return (SweetMonitor) BukkitPlugin.getInstance();
    }

    public SweetMonitor() {
        super(options()
                .bungee(false)
                .adventure(false)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.monitor.libs")
        );
        this.scheduler = new FoliaLibScheduler(this);
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetMonitor 加载完毕");
    }
}
