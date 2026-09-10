# 🦜 BUDGIES!

> A Fabric mod for Minecraft 1.21.1 that adds adorable, animated Budgerigars (parakeets) to your world.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?style=flat-square)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-blue?style=flat-square)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-CC0--1.0-lightgrey?style=flat-square)](LICENSE)
[![GeckoLib](https://img.shields.io/badge/Requires-GeckoLib-orange?style=flat-square)](https://github.com/bernie-g/geckolib)

---

## ✨ Features

### 🐦 Budgie Entity
- **3 color variants** with separate **male & female** textures (6 unique looks total)
- Fully **animated** via GeckoLib — idle, fly, sleep, relax, and poop animations
- **Custom sounds** — idle chirps, calling, answering, and sleeping murmurs

### 🤝 Taming & Interaction
- Tame budgies with **Wheat Seeds** or **Melon Seeds** (1-in-3 chance per feed)
- Untamed budgies **flee from players** approaching within 4 blocks
- Tamed budgies can be ordered to **sit** or **follow**
- **Shift + Right-click** a tamed budgie to have it **ride on your shoulder**
- Shift while riding to dismount

### 💤 Smart Behaviours
| Behaviour | Trigger |
|-----------|---------|
| **Sleeping** | Night time or zero light level |
| **Relaxing** | Randomly while perched (5–10 sec) |
| **Idle chirping** | Every 4–10 seconds |
| **Calling** | 30% chance instead of chirp — signals nearby budgies |
| **Answering** | Nearby budgies respond to a call after 0.5–1.5 sec |
| **Pooping** | Every 60–120 seconds (with animation!) |

### 🥚 Breeding
- Feed two tamed budgies seeds to put them in **Love Mode**
- One must be **male**, the other **female**
- Baby inherits a parent's variant; **10% mutation chance** for a random variant
- Baby is automatically tamed to the owner

---

## 📦 Dependencies

| Dependency | Version |
|------------|---------|
| [Minecraft](https://minecraft.net) | 1.21.1 |
| [Fabric Loader](https://fabricmc.net) | ≥ 0.19.5 |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.107.0+1.21.1 |
| [GeckoLib](https://github.com/bernie-g/geckolib) | 4.9 (Fabric 1.21.1) |
| Java | ≥ 21 |

---

## 🔨 Building from Source

> **Note:** Due to the `!` character in the folder name, Gradle's ZipFileSystem has a known bug.
> You **must** pass a custom project cache directory when running Gradle commands.

```bash
# Clone the repository
git clone https://github.com/EnderkingTR/Budgies.git
cd Budgies

# Build (Windows)
.\gradlew.bat build --project-cache-dir "C:\BudgiesMod\gradle-cache"

# Build (Linux/macOS)
./gradlew build --project-cache-dir "/tmp/BudgiesMod/gradle-cache"
```

The output jar will be at:
```
C:\BudgiesMod\build\libs\budgies-1.0.0-1.21.1-fabric.jar
```

### Jar Naming Convention
Output jars follow the pattern: `{mod_name}-{version}-{minecraft_version}-{modloader}.jar`
This makes it easy to distinguish builds across future ports.

---

## 🗂️ Project Structure

```
BUDGIES!/
├── src/
│   ├── main/
│   │   ├── java/com/nadir/budgies/
│   │   │   ├── BUDGIES.java               # Mod entrypoint
│   │   │   ├── entity/BudgieEntity.java   # Core entity logic
│   │   │   └── registry/                  # Entity, sound registries
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       └── assets/budgies/
│   │           ├── textures/entity/       # 6 variant textures
│   │           ├── geo/                   # GeckoLib model
│   │           ├── animations/            # GeckoLib animations
│   │           └── sounds/                # Custom audio files
│   └── client/
│       └── java/com/nadir/budgies/client/
│           ├── BUDGIESClient.java
│           ├── model/BudgieModel.java     # GeckoLib model binding
│           └── renderer/BudgieRenderer.java
├── build.gradle
└── gradle.properties
```

---

## 🎨 Texture Variants

| Variant | Male | Female |
|---------|------|--------|
| 0 | `variant_0_male.png` | `variant_0_female.png` |
| 1 | `variant_1_male.png` | `variant_1_female.png` |
| 2 | `variant_2_male.png` | `variant_2_female.png` |

Gender and variant are assigned **randomly at spawn** and are **saved to NBT**.

---

## 🛠️ gradle.properties Reference

```properties
minecraft_version=1.21.1
loader_version=0.19.5
mod_name=budgies
mod_loader=fabric
version=1.0.0
```

When porting to a new MC version or mod loader, update these values and the output jar name will update automatically.

---

## 📄 License

This project is released under the **CC0-1.0** license — see [LICENSE](LICENSE) for details.

---

*Made with ❤️ by [nadir](https://github.com/EnderkingTR)*
