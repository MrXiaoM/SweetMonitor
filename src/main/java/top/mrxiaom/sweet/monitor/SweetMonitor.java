package top.mrxiaom.sweet.monitor;

import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

import java.io.File;
import java.net.URL;
import java.util.List;

public class SweetMonitor extends BukkitPlugin {
    public static SweetMonitor getInstance() {
        return (SweetMonitor) BukkitPlugin.getInstance();
    }

    public SweetMonitor() throws Exception {
        super(options()
                .adventure(true)
                .scanIgnore("top.mrxiaom.sweet.monitor.libs")
        );
        this.scheduler = new FoliaLibScheduler(this);

        try {
            //noinspection ResultOfMethodCallIgnored
            getDescription().getLibraries();
        } catch (LinkageError ignored) {
            info("正在检查依赖库状态");
            File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                    ? new File("libraries")
                    : new File(this.getDataFolder(), "libraries");
            DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

            YamlConfiguration overrideLibraries = ConfigUtils.load(resolve("./.override-libraries.yml"));
            for (String key : overrideLibraries.getKeys(false)) {
                resolver.getStartsReplacer().put(key, overrideLibraries.getString(key));
            }
            resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);

            List<URL> libraries = resolver.doResolve();
            info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
            for (URL library : libraries) {
                this.classLoader.addURL(library);
            }
        }
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
