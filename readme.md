# Carpet EDTP Addition

为 Carpet Mod 提供一组简洁、实用的生存规则扩展。

## 支持版本

- Mod：`1.9`
- Minecraft：`26.2`
- Fabric Loader：`0.18.0` 或更高版本（构建使用 `0.19.3`）
- Carpet：`26.2+v260616` 或更高版本

## 使用方式

- 查看规则：`/carpet`
- 设置规则：`/carpet <规则名> <值>`

示例：

- `/carpet safeTeleport true`
- `/carpet beesDimCurfew nether`

## 规则列表

| 规则名 | 默认值 | 说明 | 可选值 | Mod 版本范围 |
| --- | --- | --- | --- | --- |
| `softObsidian` | `false` | 设置黑曜石硬度与末地石相同 | `true` / `false` | `1.0-SNAPSHOT+` |
| `unPushableArmorStands` | `false` | 盔甲架不会被攻击、爆炸或流体推动 | `true` / `false` | `1.0-SNAPSHOT+` |
| `safeTeleport` | `false` | 阻止传送到不安全位置（虚空、窒息） | `true` / `false` | `1.1+` |
| `tickCommandForAll` | `false` | 允许非 OP 玩家使用 `/tick` 指令 | `true` / `false` | `1.1+` |
| `noFurnaceAsh` | `false` | 没有配方的物品瞬间通过熔炉，避免手动清理熔炉 | `true` / `false` | `1.1+` |
| `noPlayerPortals` | `false` | 玩家无法使用末地传送门与下界传送门 | `true` / `false` | `1.1+` |
| `strongerBundle` | `false` | 允许最多 8 个潜影盒放入收纳袋，同时禁止将收纳袋放入潜影盒 | `true` / `false` | `1.1+` |
| `toughArmorStands` | `false` | 攻击不会使盔甲架掉落 | `true` / `false` | `1.1+` |
| `toughSlimeBlocks` | `false` | 设置粘液块和蜂蜜块硬度与末地石相同 | `true` / `false` | `1.1+` |
| `beesDimCurfew` | `false` | 阻止蜜蜂在下界与末地工作并立即归巢（模拟 1.21.2 以前可能出现的行为） | `false` / `nether` / `end` / `true` | `1.2+` |
| `resonantWater` | `false` | 左右逢源：当快捷栏中水桶左右两侧都有水桶时，使用中间的水桶不会消耗 | `true` / `false` | `1.3+` |
| `villagerMaxEnchantLevel` | `0` | 调节村民出售附魔工具的最高附魔等级 | `0` / `1` / `2` / `3` / `4` | `1.3+` |
| `dispenserFillsCauldron` | `false` | 发射器可以填充/取出炼药锅中的水/岩浆/细雪（和玩家的交互行为一样） | `true` / `false` | `1.3+` |
| `tntBreaksWithoutDrops` | `false` | TNT 爆炸破坏方块时不会掉落任何物品 | `true` / `false` | `1.4.x` / `1.7+` |
| `noMobFarmlandTrampling` | `false` | 阻止玩家以外的生物踩踏并破坏耕地 | `true` / `false` | `1.7+` |
| `featherFallingPreventsFarmlandTrampling` | `false` | 穿着带有摔落保护附魔的靴子时，玩家不会踩坏耕地 | `true` / `false` | `1.7+` |
| `snowlessDepths` | `false` | 无雪深处：Y=0 以下无法自然生成或增厚雪层 | `true` / `false` | `1.8+` |
| `noChickenJockeysOnMagmaBlocks` | `false` | 阻止鸡骑士在岩浆块上生成 | `true` / `false` | `1.8+` |
| `naturalSkeletonTraps` | `false` | 雷击生成的陷阱骷髅马必须位于合法的自然出生位置 | `true` / `false` | `1.9+` |
| `soundSuppressionReintroduced` | `false` | 重新引入 Minecraft 1.21 的声音抑制器相关行为 | `true` / `false` | `1.9+` |
| `staggeredBeacons` | `false` | 信标按坐标错峰检查底座、刷新效果，减少集中在同一 tick 的工作量 | `true` / `false` | `1.9+` |

## 说明

- `soundSuppressionReintroduced`（重新引入声音抑制）：

  - 允许受更新抑制影响的幽匿感测体方块实体交换并保留，恢复 Minecraft 1.21 的相关声音抑制行为。
  - 支持讲台、蜂巢和潜影盒等对应装置；规则本身不提供更新抑制器。
  - 声音抑制会通过异常中断事件处理；玩家操作数据包中的异常可以被原版拦截，但其他触发路径仍可能使服务器崩溃，使用前请备份存档。
- `beesDimCurfew`：

  - `nether` 仅下界生效
  - `end` 仅末地生效
  - `true` 下界和末地均生效
- `villagerMaxEnchantLevel` (村民附魔等级调节)：

  - `0` 关闭功能（默认，原版行为）
  - `1` 工具匠出售附魔钻石锄（附魔等级与其他工具相同，最高约 19 级）
  - `2` 最高附魔等级提升至 25（同时启用钻石锄）
  - `3` 最高附魔等级提升至 33（同时启用钻石锄）
  - `4` 最高附魔等级提升至 65（同时启用钻石锄）
  - 等级越高，村民出售的附魔工具品质越好
- `dispenserFillsCauldron` (发射器填充炼药锅)：

  - 启用后，发射器可以对炼药锅进行以下操作：
    - **填充内容物**：用水桶/岩浆桶/细雪桶向炼药锅发射，将炼药锅填满对应内容物，并返回空桶（和玩家交互逻辑相同）
    - **取出内容物**：用空桶向含有内容物（水/岩浆/细雪）的炼药锅发射，将内容物吸入桶中
  - 使用场景：自动化液体处理系统

## 发布流程

- 普通 `main` 推送和 Pull Request 运行 CI，不会创建 Release。
- 发布前更新 `gradle.properties` 的 `mod_version` 和根目录 `RELEASE_NOTES.md`；发布说明的首行使用 `# <Mod 版本> (Minecraft <游戏版本>)`。
- 将发布提交推送到 `main`，再推送格式为 `v<Mod 版本>-<游戏版本>` 的标签，例如 `v1.9-26.2`。
- CD 会检查标签与版本配置一致、标签提交属于 `main`、发布说明版本匹配；构建及 Fabric GameTest 全部通过后，使用该标签提交中的 `RELEASE_NOTES.md` 发布 GitHub Release，并上传模组和源码 JAR。
- 发布使用 GitHub Actions 自带的 `GITHUB_TOKEN`，不需要额外配置发布密钥；失败时不会发布，已有 Release 不会被覆盖。
