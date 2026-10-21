#!/usr/bin/env python3
"""
Genera las texturas pixel-art del mod (128x64) con Pillow + numpy.

    python3 tools/generate_textures.py

Escribe en src/main/resources/assets/mimicrabbit/textures/entity/:
  - mimic_rabbit.png               forma conejo
  - mimic_rabbit_monster.png       forma monstruo
  - mimic_rabbit_monster_eyes.png  solo los ojos (capa que brilla en la oscuridad)

Los offsets (u, v) de cada cubo DEBEN coincidir con los texOffs de MimicRabbitModel.java.
Para editar las texturas "a mano" puedes abrirlas en Blockbench / cualquier editor de pixel-art.
"""
import os
import numpy as np
from PIL import Image

W, H = 128, 64
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources",
                   "assets", "mimicrabbit", "textures", "entity")
rng = np.random.default_rng(1337)


def new():
    return np.zeros((H, W, 4), dtype=np.uint8)


def face_rects(u, v, w, h, d):
    """Disposición estándar de un cubo de Minecraft en la textura."""
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }


def fill(img, rect, base, noise=10, shade=1.0, patches=None):
    x, y, w, h = rect
    for j in range(h):
        for i in range(w):
            n = rng.integers(-noise, noise + 1)
            col = [int(np.clip(c * shade + n, 0, 255)) for c in base]
            img[y + j, x + i] = (*col, 255)
    if patches:
        for (pc, prob) in patches:
            for j in range(h):
                for i in range(w):
                    if rng.random() < prob:
                        col = [int(np.clip(c * shade + rng.integers(-6, 7), 0, 255)) for c in pc]
                        img[y + j, x + i] = (*col, 255)


def cube(img, u, v, w, h, d, base, noise=10, patches=None, top=1.12, bottom=0.7, side=0.92, back=0.85):
    rects = face_rects(u, v, w, h, d)
    shades = {"top": top, "bottom": bottom, "right": side, "left": side, "front": 1.0, "back": back}
    for name, rect in rects.items():
        fill(img, rect, base, noise, shades[name], patches)
    return rects


def px(img, x, y, color):
    img[y, x] = (*color, 255)


def save(img, name):
    path = os.path.normpath(os.path.join(OUT, name))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    Image.fromarray(img, "RGBA").save(path)
    print("escrito", path)


# =====================================================================================
# CONEJO NORMAL
# =====================================================================================
def make_rabbit():
    img = new()
    fur = (150, 108, 66)
    light = (226, 206, 176)
    dark = (112, 78, 46)
    pink = (232, 150, 160)

    # body (0,0) 6x5x9
    r = cube(img, 0, 0, 6, 5, 9, fur, 9, [(dark, 0.12), (light, 0.05)])
    fill(img, r["bottom"], light, 6, 1.0)          # barriga clara
    for rect_name in ("right", "left"):
        x, y, w, h = r[rect_name]
        for i in range(w):
            px(img, x + i, y + h - 1, light)       # borde inferior claro

    # head (30,0) 5x4x4
    r = cube(img, 30, 0, 5, 4, 4, fur, 8, [(dark, 0.1)])
    x, y, w, h = r["front"]
    for i in range(w):
        px(img, x + i, y + h - 1, light)           # hocico claro
    px(img, x + 1, y + 1, (20, 14, 12))            # ojos al frente (pequeños)
    px(img, x + 3, y + 1, (20, 14, 12))
    # ojos laterales
    xr, yr, wr, hr = r["right"]
    px(img, xr + 2, yr + 1, (18, 12, 12))
    px(img, xr + 2, yr, (255, 255, 255))
    xl, yl, wl, hl = r["left"]
    px(img, xl + 1, yl + 1, (18, 12, 12))
    px(img, xl + 1, yl, (255, 255, 255))

    # nose (48,0) 1x1x1
    cube(img, 48, 0, 1, 1, 1, pink, 4)

    # ear (52,0) 2x5x1
    r = cube(img, 52, 0, 2, 5, 1, fur, 8, [(dark, 0.1)])
    x, y, w, h = r["front"]
    for j in range(h - 1):
        for i in range(w):
            px(img, x + i, y + j, pink)            # interior rosa
    x, y, w, h = r["back"]

    # front leg (58,0) 2x3x2
    cube(img, 58, 0, 2, 3, 2, fur, 8, [(light, 0.2)])

    # haunch (66,0) 2x4x4
    cube(img, 66, 0, 2, 4, 4, fur, 8, [(dark, 0.12)])

    # tail (78,0) 3x3x2 (cola blanca)
    cube(img, 78, 0, 3, 3, 2, (244, 240, 232), 5)

    save(img, "mimic_rabbit.png")


# =====================================================================================
# MONSTRUO
# =====================================================================================
FUR = (34, 29, 31)
FUR_PATCH_A = (58, 46, 42)      # pelo sucio / marrón
FUR_PATCH_B = (14, 12, 14)      # sombras casi negras
BLOOD = (96, 10, 12)
BONE = (214, 202, 176)
BONE_SHADE = (150, 138, 118)
FLESH = (120, 40, 48)


def monster_cube(img, u, v, w, h, d, blood=0.05, **kw):
    return cube(img, u, v, w, h, d, FUR, 7,
                [(FUR_PATCH_A, 0.14), (FUR_PATCH_B, 0.16), (BLOOD, blood)], **kw)


def make_monster():
    img = new()
    eyes = new()

    # --- cráneo (0,0) 12x7x10
    r = monster_cube(img, 0, 0, 12, 7, 10, blood=0.03)
    # techo de la boca (cara inferior del cráneo): carne roja oscura
    fill(img, r["bottom"], BLOOD, 8, 1.0, [((60, 4, 8), 0.25)])
    # cara frontal 12x7: ojos, nariz, cicatrices
    x, y, w, h = r["front"]
    # cuencas oscuras
    for (ex, ey, ew, eh) in ((1, 1, 5, 4), (6, 1, 5, 4)):
        for j in range(eh):
            for i in range(ew):
                px(img, x + ex + i, y + ey + j, (6, 4, 6))
    # ojos rojos (en la textura base y en la capa luminosa)
    eye_pixels = [
        # ojo izquierdo (inclinado hacia dentro)
        (2, 2), (3, 2), (4, 2), (3, 3), (4, 3),
        # ojo derecho (espejo)
        (7, 2), (8, 2), (9, 2), (7, 3), (8, 3),
    ]
    for (ex, ey) in eye_pixels:
        px(img, x + ex, y + ey, (230, 20, 20))
        eyes[y + ey, x + ex] = (255, 24, 24, 255)
    for (ex, ey) in ((4, 2), (7, 2)):               # núcleo blanco-rojizo muy brillante
        px(img, x + ex, y + ey, (255, 170, 160))
        eyes[y + ey, x + ex] = (255, 190, 180, 255)
    # nariz
    px(img, x + 5, y + 4, (10, 6, 8))
    px(img, x + 6, y + 4, (10, 6, 8))
    px(img, x + 5, y + 5, (70, 20, 28))
    px(img, x + 6, y + 5, (70, 20, 28))
    # cicatriz / costura vertical sobre el ojo derecho
    for j in range(0, 5):
        px(img, x + 10, y + j, BLOOD)
    px(img, x + 9, y + 1, BONE_SHADE)
    px(img, x + 11, y + 3, BONE_SHADE)
    # labio superior oscuro
    for i in range(w):
        px(img, x + i, y + h - 1, (10, 7, 9))
    # marcas de sangre goteando desde los ojos
    for (ex, ey) in ((2, 4), (9, 4)):
        px(img, x + ex, y + ey, BLOOD)
    px(img, x + 2, y + 5, BLOOD)

    # --- mandíbula (44,0) 10x3x9
    r = monster_cube(img, 44, 0, 10, 3, 9, blood=0.12)
    fill(img, r["top"], BLOOD, 8, 1.0, [((60, 4, 8), 0.25)])   # interior de la boca
    x, y, w, h = r["top"]
    for i in range(3, w - 3):                                  # lengua
        for j in range(2, h - 2):
            px(img, x + i, y + j, FLESH)

    # --- torso (82,0) 6x12x4
    r = monster_cube(img, 82, 0, 6, 12, 4, blood=0.08)
    x, y, w, h = r["front"]
    for j in range(1, h - 1, 2):                               # costillas marcadas
        for i in range(1, w - 1):
            px(img, x + i, y + j, (22, 18, 20))
    px(img, x + 2, y + 2, BLOOD)
    px(img, x + 3, y + 5, BLOOD)
    px(img, x + 2, y + 6, BLOOD)
    x, y, w, h = r["back"]
    for j in range(h):                                         # columna vertebral
        px(img, x + w // 2, y + j, BONE_SHADE if j % 2 else (60, 52, 48))

    # --- cuello (102,0) 4x4x4
    monster_cube(img, 102, 0, 4, 4, 4, blood=0.04)

    # --- diente (118,0) 1x2x1
    cube(img, 118, 0, 1, 2, 1, BONE, 6, None, top=1.0, bottom=0.8, side=0.95)

    # --- pierna (0,17) 3x13x3
    r = monster_cube(img, 0, 17, 3, 13, 3, blood=0.04)
    # --- brazo superior (12,17) 3x12x3 / antebrazo (24,17) 3x12x3
    monster_cube(img, 12, 17, 3, 12, 3, blood=0.05)
    r = monster_cube(img, 24, 17, 3, 12, 3, blood=0.08)
    # --- pie (36,17) 3x1x3
    cube(img, 36, 17, 3, 1, 3, (20, 17, 19), 5)
    # --- púas de la espalda (48,17) 1x2x2
    cube(img, 48, 17, 1, 2, 2, BONE_SHADE, 8, [(BONE, 0.3)])
    # --- garras (54,17) 1x5x1
    cube(img, 54, 17, 1, 5, 1, BONE, 6, [(BONE_SHADE, 0.35)], top=1.0, bottom=0.8, side=0.95)

    # --- orejas (58,17) 3x9x1 y puntas (66,17) 3x7x1
    for (u, v, w, h, d) in ((58, 17, 3, 9, 1), (66, 17, 3, 7, 1)):
        r = monster_cube(img, u, v, w, h, d, blood=0.1)
        x, y, ww, hh = r["front"]
        for j in range(hh):
            for i in range(ww):
                if rng.random() < 0.8:                          # interior rasgado, carne descubierta
                    px(img, x + i, y + j, FLESH if rng.random() < 0.7 else (80, 18, 26))
        for i in range(ww):
            px(img, x + i, y, (24, 18, 22))                     # borde roto

    save(img, "mimic_rabbit_monster.png")
    save(eyes, "mimic_rabbit_monster_eyes.png")


if __name__ == "__main__":
    make_rabbit()
    make_monster()
