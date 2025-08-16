package top.mrxiaom.sweet.monitor;
        
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

public class SweetMonitor extends BukkitPlugin {
    public static SweetMonitor getInstance() {
        return (SweetMonitor) BukkitPlugin.getInstance();
    }

    FoliaLib foliaLib;
    public SweetMonitor() {
        super(options().scanIgnore("top.mrxiaom.sweet.monitor.libs"));
        FoliaLibScheduler scheduler = new FoliaLibScheduler(this);
        this.scheduler = scheduler;
        this.foliaLib = scheduler.getFoliaLib();
    }

    public PlatformScheduler getFoliaScheduler() {
        return foliaLib.getScheduler();
    }

    public void teleportThen(Entity entity, Location location, Runnable then) {
        getFoliaScheduler().teleportAsync(entity, location)
                .thenRun(() -> getFoliaScheduler().runAtEntity(entity, t -> then.run()));
    }

    @Override
    protected void beforeEnable() {
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Messages.class);
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetMonitor 加载完毕");
    }
}
