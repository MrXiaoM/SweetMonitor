package top.mrxiaom.sweet.monitor;
        
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

public class SweetMonitor extends BukkitPlugin {
    public static SweetMonitor getInstance() {
        return (SweetMonitor) BukkitPlugin.getInstance();
    }

    FoliaLib foliaLib;
    public SweetMonitor() {
        super(options()
                .bungee(false)
                .adventure(false)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.monitor.libs")
        );
        FoliaLibScheduler scheduler = new FoliaLibScheduler(this);
        this.scheduler = scheduler;
        this.foliaLib = scheduler.getFoliaLib();
    }

    public PlatformScheduler getFoliaScheduler() {
        return foliaLib.getScheduler();
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetMonitor 加载完毕");
    }
}
