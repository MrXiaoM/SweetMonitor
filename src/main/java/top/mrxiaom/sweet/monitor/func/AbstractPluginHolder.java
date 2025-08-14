package top.mrxiaom.sweet.monitor.func;
        
import top.mrxiaom.sweet.monitor.SweetMonitor;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetMonitor> {
    public AbstractPluginHolder(SweetMonitor plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetMonitor plugin, boolean register) {
        super(plugin, register);
    }
}
