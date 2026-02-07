# Carpet EDTP Addition

为 Carpet Mod 提供一组简洁、实用的生存规则扩展。

## 使用方式

- 查看规则：`/carpet`
- 设置规则：`/carpet <规则名> <值>`

示例：

- `/carpet safeTeleport true`
- `/carpet beesDimCurfew nether`

## 规则列表

| 规则名 | 默认值 | 说明 | 可选值 |
| --- | --- | --- | --- |
| `softObsidian` | `false` | 设置黑曜石硬度与末地石相同 | `true` / `false` |
| `unPushableArmorStands` | `false` | 盔甲架不会被攻击、爆炸或流体推动 | `true` / `false` |
| `safeTeleport` | `false` | 阻止传送到不安全位置（虚空、窒息） | `true` / `false` |
| `tickCommandForAll` | `false` | 允许非 OP 玩家使用 `/tick` 指令 | `true` / `false` |
| `noFurnaceAsh` | `false` | 没有配方的物品瞬间通过熔炉，避免手动清理熔炉 | `true` / `false` |
| `noPlayerPortals` | `false` | 玩家无法使用末地传送门与下界传送门 | `true` / `false` |
| `strongerBundle` | `false` | 允许最多 8 个潜影盒放入收纳袋，同时禁止将收纳袋放入潜影盒 | `true` / `false` |
| `toughArmorStands` | `false` | 攻击不会使盔甲架掉落 | `true` / `false` |
| `toughSlimeBlocks` | `false` | 设置粘液块和蜂蜜块硬度与末地石相同 | `true` / `false` |
| `beesDimCurfew` | `false` | 阻止蜜蜂在下界与末地工作并立即归巢（模拟 1.21.2 以前可能出现的行为） | `false` / `nether` / `end` / `true` |
| `resonantWater` | `false` | 左右逢源：当快捷栏中水桶左右两侧都有水桶时，使用中间的水桶不会消耗 | `true` / `false` |
| `villagerMaxEnchantLevel` | `0` | 调节村民出售附魔工具的最高附魔等级 | `0` / `1` / `2` / `3` / `4` |

## 说明

- `beesDimCurfew`：
  - `nether` 仅下界生效
  - `end` 仅末地生效
  - `true` 下界和末地均生效

- `resonantWater` (左右逢源)：
  - 在快捷栏中将三个水桶排成一排：`[水桶] [水桶] [水桶]`
  - 使用中间的水桶时，水会被倒出但桶不会变空
  - 触发时会播放回响锚设置重生点的音效
  - 仅对非创造模式玩家生效

- `villagerMaxEnchantLevel` (村民附魔等级调节)：
  - `0` 关闭功能（默认，原版行为）
  - `1` 工具匠出售附魔钻石锄（附魔等级与其他工具相同，最高约 19 级）
  - `2` 最高附魔等级提升至 25（同时启用钻石锄）
  - `3` 最高附魔等级提升至 33（同时启用钻石锄）
  - `4` 最高附魔等级提升至 65（同时启用钻石锄）
  - 等级越高，村民出售的附魔工具品质越好
