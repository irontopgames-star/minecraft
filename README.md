# Mimic Rabbit — mod de terror para Minecraft 1.20.1 (Forge)

Un conejo aparentemente normal. Si lo golpeas, se transforma en una criatura alta, flaca y deformada
que te persigue hasta matarte.

- Minecraft Java **1.20.1** · Forge **47.3.0** · Java **17** · Gradle (wrapper 8.1.1 incluido)
- ID del mod: `mimicrabbit` · Entidad: `mimicrabbit:mimic_rabbit`

## 1. Compilar y generar el .jar

Necesitas **JDK 17** (si no lo tienes, Gradle intentará descargarlo solo) y conexión a internet la primera vez
(descarga Forge y las librerías de Minecraft; tarda unos minutos).

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

El jar queda en `build/libs/mimicrabbit-1.0.0.jar`.

### Abrirlo en un IDE
- **IntelliJ IDEA**: `File > Open` → elige `build.gradle` → "Open as Project". Cuando termine la sincronización de
  Gradle ejecuta la tarea `genIntellijRuns` (Gradle > forgegradle runs) y usa la configuración `runClient`.
- **Eclipse**: `./gradlew genEclipseRuns` e importa como proyecto Gradle.
- Probar sin instalar nada: `./gradlew runClient`.

## 2. Instalar en Minecraft

1. Instala **Forge 1.20.1-47.3.0** (o cualquier 47.x) desde files.minecraftforge.net.
2. Copia `mimicrabbit-1.0.0.jar` a la carpeta `mods` de tu instancia.
3. Inicia el perfil de Forge.

## 3. Probarlo

- **Rápido**: modo creativo → pestaña *Spawn Eggs* → "Rabbit Spawn Egg (Mimic)", o
  `/summon mimicrabbit:mimic_rabbit`. Golpéalo.
- **Natural**: aparece (junto a los conejos normales) en flower forest, taiga (todas), snowy plains, ice spikes,
  desert, meadow, grove y snowy slopes. Se configura en
  `src/main/resources/data/mimicrabbit/forge/biome_modifier/mimic_rabbit_spawns.json`
  (peso, cantidad por grupo y lista de biomas).
- Para sentir el peligro de verdad juega en **Supervivencia** y dificultad Normal o superior
  (en Pacífico el daño de los mobs es 0, como siempre en vanilla).

## 4. Cómo funciona

### Máquina de estados (`entity/MimicRabbit.java`, enum `State`)
| Estado | Qué pasa |
|---|---|
| `NORMAL` | Conejo vanilla-like: salta, merodea, huye si lo daña algo que no sea un jugador. No ataca. |
| `TRANSFORMING` | El primer golpe de un **jugador** (cuerpo a cuerpo, flecha, bola de nieve...) no hace daño y dispara 50 ticks (2,5 s) de transformación: queda inmóvil e invulnerable, mira al jugador, tiembla, se hincha, crujen huesos, partículas de tinta/humo/polvo rojo, y el jugador recibe *Oscuridad* unos segundos. En el tick 28 cambia el modelo y suena el grito. |
| `HOSTILE` | Con el jugador a la vista: lo persigue (`MeleeAttackGoal` persistente) y da saltos de acecho (`LeapAtTargetGoal`). |
| `SEARCHING` | Si pierde al objetivo (100 ticks sin verlo y a más de 16 bloques) va a su **última posición conocida** y merodea ahí 20 s; reengancha al jugador si lo ve (hasta 64 bloques) u olfatea (≤16 bloques). |
| `DYING` | Animación de muerte vanilla + partículas de alma/tinta y sonido de muerte. |

La transformación es **irreversible y de un solo uso**: el flag `Transformed` (sincronizado y guardado en NBT)
impide repetirla, y el monstruo conserva su forma tras recargar el mundo. El mob recuerda **quién** lo golpeó (UUID)
y ese jugador es siempre su objetivo principal.

### Atributos
| | Conejo | Monstruo |
|---|---|---|
| Vida | 3 | 40 |
| Velocidad | 0.22 | 0.27 (×1.15 al atacar ≈ algo más rápido que un jugador corriendo; saltar+correr lo supera) |
| Daño | — | 6 (3 corazones en Normal, escala con dificultad) |
| Hitbox | 0.4 × 0.5 | 0.8 × 2.1 |
| Rango de seguimiento | 16 | 64 |
| Resistencia a empuje | 0 | 0.4 |

Todas las constantes están al principio de `MimicRabbit.java` para equilibrar fácilmente.

Botín del monstruo: 1–3 pieles de conejo, 35 % pie de conejo (+10 % por nivel de Looting), carne cruda y 20 de XP.

## 5. Estructura del proyecto

```
build.gradle / settings.gradle / gradle.properties     ← configuración Gradle + Forge 47.3.0
gradlew, gradlew.bat, gradle/wrapper/                  ← Gradle 8.1.1
tools/generate_textures.py                             ← genera las 3 texturas (Pillow + numpy)
src/main/java/com/mimicrabbit/
  MimicRabbitMod.java                                  ← @Mod, registra los DeferredRegister
  registry/ModEntities.java | ModSounds.java | ModItems.java
  event/ModEvents.java                                 ← atributos, spawn placement, pestaña creativa
  entity/MimicRabbit.java                              ← entidad, IA, transformación, estados
  client/ClientModEvents.java                          ← registra renderer y capa de modelo
  client/MimicRabbitModel.java                         ← modelo con las 2 formas + animaciones
  client/MimicRabbitRenderer.java                      ← textura por forma, temblor e hinchazón
  client/MimicRabbitEyesLayer.java                     ← ojos rojos que brillan en la oscuridad
src/main/resources/
  META-INF/mods.toml, pack.mcmeta
  assets/mimicrabbit/{sounds.json, lang/*.json, models/item/*.json, textures/entity/*.png}
  data/mimicrabbit/forge/biome_modifier/mimic_rabbit_spawns.json   ← aparición natural
  data/mimicrabbit/loot_tables/entities/mimic_rabbit.json          ← (vacío; el botín se calcula en código)
```

## 6. Sonidos

El mod ya incluye **10 eventos de sonido propios** (`mimicrabbit:entity.mimic_rabbit.*`) definidos en
`assets/mimicrabbit/sounds.json`. Para que funcione sin descargar nada, cada uno reutiliza sonidos de vanilla con
otro tono (rugido de ravager grave, grito de enderman a media velocidad, latido de warden, etc.).

| Evento | Cuándo suena |
|---|---|
| `transform_start` | Al recibir el golpe (latido grave) |
| `transform_crack` | Crujidos durante la deformación |
| `transform_scream` + `transform_roar` | Grito + rugido al aparecer el monstruo |
| `ambient` | Gruñidos ocasionales |
| `breath` | Respiración mientras persigue |
| `attack`, `hurt`, `death`, `step` | Ataque, daño, muerte, pasos |

**Para usar tus propios audios** (formato **.ogg mono**, 44.1 kHz):
1. Colócalos en `src/main/resources/assets/mimicrabbit/sounds/entity/` con estos nombres, p. ej.
   `transform_scream.ogg`, `breath.ogg`, `attack1.ogg`, `attack2.ogg`, `hurt.ogg`, `death.ogg`, `ambient1.ogg`...
2. En `sounds.json` cambia la entrada del evento correspondiente, por ejemplo:
   ```json
   "entity.mimic_rabbit.transform_scream": {
     "subtitle": "subtitles.mimicrabbit.transform_scream",
     "sounds": [ { "name": "mimicrabbit:entity/transform_scream", "volume": 1.0 } ]
   }
   ```
   (el nombre es la ruta del archivo dentro de `sounds/`, sin la extensión).
3. Vuelve a compilar (`./gradlew build`) o ejecuta `runClient`.

## 7. Modelos y texturas

- **Modelo**: está definido en código (`MimicRabbitModel.createBodyLayer`) con cubos, como los mobs vanilla;
  una sola capa con las partes `rabbit` y `monster`, de las que se muestra una según el estado. Las animaciones
  (andar, saltos, mandíbula castañeando, orejas torcidas, zarpazo, tics de cabeza, aparición) también están en código.
- **Texturas** (128×64, estilo píxel vanilla), en `assets/mimicrabbit/textures/entity/`:
  `mimic_rabbit.png`, `mimic_rabbit_monster.png` y `mimic_rabbit_monster_eyes.png` (solo los ojos, capa luminosa).
  Se regeneran con `python3 tools/generate_textures.py`, o puedes retocarlas a mano.
- **Blockbench (opcional)**: crea un modelo *Modded Entity* (Java Block/Item no sirve), formato "Minecraft Entity / Mojang
  mappings", textura 128×64, y usa *File > Export > Export Java Entity* para ver el código de cada cubo; pega las
  partes en `createBodyLayer`. Respeta los `texOffs` o regenera las texturas con el script.

## 8. Notas

- Es normal que la primera compilación tarde (descarga y decompila Minecraft/Forge).
- El nombre de la entidad es "Conejo/Rabbit" a propósito, para que no delate al mímico al apuntarle.
- Un solo jugador es el objetivo del monstruo; otros jugadores pueden golpearlo y dañarlo normalmente.
