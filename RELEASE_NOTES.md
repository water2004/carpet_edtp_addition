# 1.11 (Minecraft 26.2)

## 更新内容

### 新内容：

- 添加规则：

1. `fakePlayerConnectionLifecycle`：补全 Carpet 假人下线与服务器关闭时的 Fabric `DISCONNECT` 事件和网络会话清理，帮助 BlueMap 等依赖下线事件的模组及时更新状态。
2. `iceRailSpeed`：分档调整浮冰、蓝冰支撑的轨道限速，提供原版、两种冰均为 `1`、浮冰/蓝冰为 `1/2`，以及按原版乘客倍率预留补偿的四个档位。
3. `minecartNoPassengerSpeedPenalty`：将有乘客矿车在轨道上的位移倍率从 `0.75` 改为 `1`，可与冰基限速独立组合。

新增规则均默认关闭：`iceRailSpeed` 默认值为 `0`，其余两条规则默认值为 `false`。

- 关于 `fakePlayerConnectionLifecycle` 的附加说明：

> 1. 复用现有 Fabric 网络对象完成下线，不重复触发已有的 `INIT` 和 `JOIN` 事件。开启时建立的假人连接，即使之后关闭规则，也会完成下线清理。
> 2. 支持影子假人与同 UUID 玩家替换；保留原版假人 tick 和动作包时序，可与 Carpet TIS Addition 的 `fakePlayerTicksLikeRealPlayer` 配合使用。
> 3. 假人仍没有真实客户端，此规则不模拟客户端握手。下线监听器异常会记录日志并完成会话清理，但不能保证异常之后的其他监听器继续执行。

- 关于矿车规则的附加说明：

所有速度均为**方块/tick**，不随 TPS 换算或补偿。

| `iceRailSpeed` | 浮冰位移上限 | 蓝冰位移上限 | 蓝冰载人直线最高速度（倍率规则关闭 / 开启） |
| --- | --- | --- | --- |
| `0` | 原版 | 原版 | 原版 / 原版 |
| `1` | `1` | `1` | `1` / `1` |
| `2` | `1` | `2` | `1.5` / `2` |
| `3` | `4/3` | `8/3` | `2` / `8/3` |

> 1. 两条规则互不读取或限制对方。第 `3` 档固定按原版 `0.75` 预留补偿；同时开启倍率规则后，蓝冰载人速度仍可进一步提高到 `8/3`。
> 2. 适用于默认矿车运动，不修改“矿车改进”实验。按轨道正下方的支撑方块识别冰种；普通冰、其他路基和脱轨状态保留原版限速。倍率规则也可单独用于普通轨道。
> 3. 保留原版加速、制动、阻力和速度储备。离开冰基轨道后恢复原版限速，不额外清空内部速度；提高上限不保证所有车辆都能自行加速到该上限。动力矿车保留自身限速倍率。
> 4. 冰基上限也适用于水中轨道，额外水中阻力保留。弯坡、跨格方向修正和轨道触发顺序沿用原版，不增加自动限速或分段运动。

### 其他改进：

- 新增 19 项 Fabric GameTest：10 项假人生命周期用例覆盖连接事件、会话清理、规则切换、监听器异常、重生、重复登录、影子假人与关服；9 项矿车用例覆盖规则组合、实际 tick 位移、原版阻力、动力轨加速制动、路基变化、脱轨和参数校验。
- 生命周期、关服及矿车用例使用独立测试批次，减少全局规则和玩家连接之间的干扰；当前共 57 项测试通过。
- 为新整数规则添加严格选项校验，保留既有整数规则的取值行为。

---

## Update Details

### What's New:

- Added rules:

1. `fakePlayerConnectionLifecycle`: Completes Fabric `DISCONNECT` events and network session cleanup when Carpet fake players leave or the server shuts down, helping mods such as BlueMap update player state.
2. `iceRailSpeed`: Adds four speed-limit modes for rails supported by packed or blue ice: vanilla, `1` on both, `1/2` on packed/blue ice, and limits with compensation reserved for the vanilla passenger multiplier.
3. `minecartNoPassengerSpeedPenalty`: Changes the on-rail movement multiplier for occupied minecarts from `0.75` to `1`, independently of the ice speed rule.

All new rules are disabled by default: `iceRailSpeed` defaults to `0`, and the other two rules default to `false`.

- Additional notes about `fakePlayerConnectionLifecycle`:

> 1. Reuses the existing Fabric networking object for disconnect cleanup without repeating existing `INIT` or `JOIN` events. Connections created while enabled still receive cleanup if the rule is later disabled.
> 2. Supports shadow players and replacement by a player with the same UUID. Fake-player tick and action-pack scheduling are preserved, allowing use alongside Carpet TIS Addition's `fakePlayerTicksLikeRealPlayer`.
> 3. Fake players still have no real client, and this rule does not simulate client handshakes. Disconnect listener exceptions are logged and session cleanup still runs, but subsequent listeners are not guaranteed to execute after an exception.

- Additional notes about the minecart rules:

All speeds are in **blocks per tick**, without conversion or compensation for TPS.

| `iceRailSpeed` | Packed ice movement limit | Blue ice movement limit | Occupied blue ice straight-track maximum (passenger rule off / on) |
| --- | --- | --- | --- |
| `0` | Vanilla | Vanilla | Vanilla / Vanilla |
| `1` | `1` | `1` | `1` / `1` |
| `2` | `1` | `2` | `1.5` / `2` |
| `3` | `4/3` | `8/3` | `2` / `8/3` |

> 1. Neither rule reads or restricts the other. Mode `3` reserves fixed compensation for vanilla's `0.75` multiplier; enabling the passenger rule as well raises the occupied blue ice maximum further to `8/3`.
> 2. Applies to default minecart movement without changing the Minecart Improvements experiment. Ice type is determined by the block directly supporting the current rail. Ordinary ice, other supports, and derailed carts retain vanilla limits. The passenger rule also works independently on ordinary tracks.
> 3. Vanilla acceleration, braking, drag, and stored velocity are preserved. Leaving ice-supported rails restores vanilla limits without additionally clearing stored velocity. Higher limits do not guarantee that every vehicle can accelerate to them on its own. Furnace minecarts retain their own speed-limit multipliers.
> 4. Ice limits also apply to underwater rails while preserving additional water drag. Curves, slopes, movement direction adjustment across blocks, and rail activation order retain vanilla behavior; no automatic speed reduction or segmented movement is added.

### Other Improvements:

- Added 19 Fabric GameTests: 10 fake-player lifecycle tests cover connection events, session cleanup, rule changes, listener failures, respawn, duplicate login, shadow players, and shutdown; nine minecart tests cover rule combinations, actual tick displacement, vanilla drag, powered-rail acceleration and braking, support changes, derailment, and value validation.
- Lifecycle, shutdown, and minecart tests use separate batches to reduce interference involving global rules and player connections. All 57 current tests pass.
- Added strict option validation for the new integer rule while preserving existing integer-rule value behavior.
