# 1.9 (Minecraft 26.2)

## 更新内容

### 新内容：

- 添加规则：

1. `naturalSkeletonTraps`：雷击生成的陷阱骷髅马必须通过自然出生位置和碰撞检查，避免在不允许自然刷怪的位置生成。
2. `soundSuppressionReintroduced`：重新引入 Minecraft 1.21 的声音抑制器相关行为，允许受更新抑制影响的幽匿感测体方块实体交换并保留。
3. `staggeredBeacons`：按坐标错开信标的底座检查、效果刷新和环境音时刻，减少大量信标在同一 tick 集中工作的峰值。

新增规则均默认关闭。

- 关于 `soundSuppressionReintroduced` 的附加说明：

> 支持讲台、蜂巢和潜影盒等对应声音抑制装置，但规则本身不提供更新抑制器。声音抑制通过异常中断事件处理；玩家操作数据包中的异常可以被原版拦截，其他触发路径仍可能导致服务器崩溃，使用前请备份存档。

- 关于 `staggeredBeacons` 的附加说明：

> 仍然每 80 tick 执行一次，不改变效果等级、范围、持续时间或逐 tick 的光柱扫描。规则减少集中工作峰值，不减少总扫描次数；切换规则会改变刷新时序。

### 其他改进：

- 整理村民交易、收纳袋、安全传送和水桶规则的代码，将主要逻辑与 Mixin 分离，保持既有规则行为。
- 补充 Fabric GameTest，覆盖陷阱骷髅马生成检查、声音抑制、玩家放置数据包异常处理，以及信标错峰。
- 添加标签触发的自动发布流程，构建和 GameTest 通过后，使用根目录的发布说明创建 GitHub Release，并上传模组和源码 JAR。

---

## Update Details

### What's New:

- Added rules:

1. `naturalSkeletonTraps`: Requires lightning-generated skeleton traps to pass natural spawn-position and collision checks, preventing them from appearing at invalid natural spawn positions.
2. `soundSuppressionReintroduced`: Reintroduces Minecraft 1.21 sound-suppressor behavior by preserving sculk sensor block entity swaps caused by update suppression.
3. `staggeredBeacons`: Distributes beacon base checks, effect refreshes, and ambient sounds across different ticks using position-based offsets, reducing spikes caused by many beacons updating together.

All new rules are disabled by default.

- Additional notes about `soundSuppressionReintroduced`:

> Supports corresponding sound suppressors using lecterns, bee nests, and shulker boxes. The rule does not provide an update suppressor itself. Sound suppression interrupts event processing through exceptions: vanilla can catch exceptions during player interaction packet handling, but other trigger paths may still crash the server. Back up your world before use.

- Additional notes about `staggeredBeacons`:

> Each beacon still updates every 80 ticks. Effect levels, range, duration, and per-tick beam scanning remain unchanged. The rule spreads workload peaks without reducing the total number of scans; toggling it changes the update schedule.

### Other Improvements:

- Separated the main logic of villager trades, bundles, safe teleportation, and bucket rules from their Mixins while preserving existing behavior.
- Expanded Fabric GameTest coverage for skeleton trap spawn checks, sound suppression, player placement packet error handling, and staggered beacon updates.
- Added tag-triggered automated releases. After the build and GameTests pass, the workflow publishes a GitHub Release using the release notes in the repository root and uploads the mod and sources JARs.
