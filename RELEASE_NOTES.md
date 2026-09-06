# 1.10 (Minecraft 26.2)

## 更新内容

### 新内容：

- 添加规则：

1. `preserveLightOnUpgrade`：版本升级时保留旧区块中已标记有效的光照缓存，避免升级修复主动清除已有光照数据，默认关闭。

- 关于 `preserveLightOnUpgrade` 的附加说明：

> 1. 需在旧区块升级前启用。首次用新版打开存档前，在存档的 `carpet.conf` 中加入 `preserveLightOnUpgrade true`，然后正常加载游戏。
> 2. 不要先运行“优化世界”或 `--forceUpgrade`：离线升级发生在 Carpet 加载存档规则之前，不受此配置保护。
> 3. 不会将无效光照强制标记为有效；世界高度迁移、主动清除缓存以及正常光照更新仍遵循原版逻辑。
> 4. 旧光照错误也会被保留，不能保证兼容所有跨版本光照变化。使用前请备份存档；关闭规则不会自动重算已经升级的区块，也不能恢复已经删除的光照缓存。

### 其他改进：

- 新增 6 项 Fabric GameTest，覆盖两次光照清除升级、缓存转换器中的规则切换、无效标记、完整升级与序列化、世界高度迁移、主动清除缓存，以及服务端正常光照传播和消退。
- README 规则列表使用 Mod 版本范围标注规则可用版本，移除独立更新日志。

---

## Update Details

### What's New:

- Added rule:

1. `preserveLightOnUpgrade`: Preserves light caches marked valid in old chunks when upgrading game versions, preventing upgrade fixes from deleting existing lighting data. Disabled by default.

- Additional notes about `preserveLightOnUpgrade`:

> 1. Enable the rule before old chunks are upgraded. Before opening the world in the newer version for the first time, add `preserveLightOnUpgrade true` to the world's `carpet.conf`, then load the game normally.
> 2. Do not run Optimize World or `--forceUpgrade` first: offline upgrades run before Carpet loads the world's rule configuration and are not protected by this setting.
> 3. Invalid lighting is not forcibly marked valid. World-height migration, explicit cache erasure, and normal light updates retain vanilla behavior.
> 4. Existing lighting errors are preserved too; compatibility with every cross-version lighting change is not guaranteed. Back up your world first. Disabling the rule does not automatically relight already upgraded chunks or restore deleted light caches.

### Other Improvements:

- Added six Fabric GameTests covering both light-deletion upgrades, rule changes with cached converters, invalid markers, full upgrades and serialization, world-height migration, explicit cache erasure, and normal server-side light propagation and removal.
- Replaced the standalone README changelog with Mod version ranges in the rule table.
