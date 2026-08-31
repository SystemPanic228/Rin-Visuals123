package ru.strange.client.ui.clickgui;

import me.x150.renderer.fontng.GlyphBuffer;
import net.minecraft.client.MinecraftClient;
import ru.strange.client.module.Theme;
import ru.strange.client.module.ThemeManager;
import ru.strange.client.module.api.Category;
import ru.strange.client.module.api.Module;
import ru.strange.client.module.api.setting.Setting;
import ru.strange.client.module.api.setting.impl.*;
import ru.strange.client.utils.math.ScrollUtil;
import ru.strange.client.utils.render.FontDraw;
import ru.strange.client.utils.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiScreen {
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static ScrollUtil scroll = new ScrollUtil();

    // ==================== Окно (шире, чем выше) ====================
    public static float x, y;
    public static float width = 440, height = 270;

    // ==================== Сайдбар ====================
    public static final float SIDEBAR_W = 122f;
    public static final float CAT_START = 52f;
    public static final float CAT_ROW_H = 21f;
    public static final float CAT_GAP = 3f;

    // ==================== Контент ====================
    public static final float CONTENT_PAD = 8f;
    public static final float HEADER_H = 28f;
    public static final float MODULE_H = 30f;
    public static final float MODULE_GAP = 5f;
    public static final float SETTING_GAP = 3f;

    // ==================== Контролы ====================
    public static final float ROW_H = 16f;
    public static final float SLIDER_H = 23f;
    public static final float BOX_H = 12f;
    public static final float MODE_BOX_W = 100f;
    public static final float STRING_BOX_W = 100f;
    public static final float BIND_BOX_W = 64f;
    public static final float DROPDOWN_ROW_H = 11f;
    public static final float HUE_EXTRA_H = 84f;
    public static final float SWITCH_W = 22f;
    public static final float SWITCH_H = 12f;
    public static final float SWITCH_KNOB = 8f;

    public static Category[] categories;
    public static List<Module> modules;
    public static Theme[] themes;
    public static Category selectedCategories = Category.World;
    public static Theme selectedTheme = Theme.WHITE;
    public static Theme preSelectedTheme;

    // ==================== Анимации ====================
    private static final Map<Module, Float> toggleAnim = new HashMap<>();
    private static final Map<Module, Float> openAnim = new HashMap<>();
    private static final Map<Setting, Float> dropAnim = new HashMap<>();
    private static final Map<Setting, Float> boolAnim = new HashMap<>();
    public static float categoryAnimY = -1;

    // ==================== Окно: открытие / закрытие ====================
    public static final float OPEN_DURATION_MS = 400f;
    public static final float CLOSE_DURATION_MS = 230f;
    private static long windowStartedAt = System.nanoTime();
    private static boolean closing = false;
    private static float windowProgress = 0f;
    public static float windowScale = 1f;
    public static float windowOffsetY = 0f;
    public static float alphaMul = 1f;

    // ==================== Появление элементов категории ====================
    public static final float ITEM_APPEAR_DURATION_MS = 300f;
    private static final float ITEM_APPEAR_STAGGER_MS = 42f;
    private static final float ITEM_APPEAR_MAX_STAGGER_MS = 320f;
    public static final float ITEM_APPEAR_X = 22f;
    public static final float ITEM_APPEAR_Y = 6f;
    public static final float ITEM_APPEAR_SCALE_FROM = 0.93f;
    public static final float ITEM_LEAVE_DURATION_MS = 120f;
    public static final float ITEM_LEAVE_Y = 8f;

    private static long categoryItemsStartedAt = System.nanoTime();
    private static long switchStartedAt = 0L;
    private static boolean switching = false;
    private static List<Module> leavingModules = null;
    private static float leavingScroll = 0f;
    private static Category leavingCategory = null;

    // ==================== Появление сайдбара (один раз на открытие) ====================
    private static final float SIDEBAR_APPEAR_DURATION_MS = 300f;
    private static final float SIDEBAR_APPEAR_STAGGER_MS = 40f;
    private static long sidebarStartedAt = System.nanoTime();

    /** Множитель прозрачности конкретного элемента (карточки) — поверх alphaMul. */
    public static float contentAlpha = 1f;

    // ==================== Базовые хелперы ====================

    public static boolean isHovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static float lerp(float from, float to, float speed) {
        return from + (to - from) * speed;
    }

    public static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    // ==================== Кривые ====================

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float f = 1f - t;
        return 1f - f * f * f;
    }

    public static float easeOutQuint(float t) {
        t = clamp01(t);
        float f = 1f - t;
        return 1f - f * f * f * f * f;
    }

    /** Мягкий «пружинный» выход с лёгким перелётом. */
    public static float easeOutBack(float t) {
        t = clamp01(t);
        float c1 = 1.25f;
        float c3 = c1 + 1f;
        float f = t - 1f;
        return 1f + c3 * f * f * f + c1 * f * f;
    }

    public static float easeInQuad(float t) {
        t = clamp01(t);
        return t * t;
    }

    private static float elapsedMs(long fromNanos) {
        return (System.nanoTime() - fromNanos) / 1_000_000f;
    }

    // ==================== Окно: управление ====================

    /** Вызывается при создании экрана — сбрасывает все анимации в стартовое состояние. */
    public static void resetWindow() {
        closing = false;
        windowProgress = 0f;
        windowStartedAt = System.nanoTime();
        windowScale = 0.9f;
        windowOffsetY = 14f;
        alphaMul = 0f;
        switching = false;
        leavingModules = null;
        leavingCategory = null;
        categoryAnimY = -1;
        sidebarStartedAt = System.nanoTime();
        categoryItemsStartedAt = System.nanoTime();
        contentAlpha = 1f;
        scroll.reset();
        GlyphBuffer.alphaMultiplier = 1f;
    }

    public static void beginClose() {
        if (!closing) {
            closing = true;
            windowStartedAt = System.nanoTime();
        }
    }

    public static boolean isClosing() {
        return closing;
    }

    /** true, когда анимация закрытия доиграла до конца и экран можно убирать. */
    public static boolean isClosed() {
        return closing && windowProgress <= 0f;
    }

    // ==================== Смена категории ====================

    public static void beginCategorySwitch(Category from) {
        leavingCategory = from;
        leavingModules = (from == Category.Theme) ? null : modules;
        leavingScroll = scroll.getScroll();
        switchStartedAt = System.nanoTime();
        switching = true;
        categoryItemsStartedAt = System.nanoTime() + (long) (ITEM_LEAVE_DURATION_MS * 0.7f * 1_000_000f);
        scroll.reset();
    }

    /** true пока старый контент доигрывает своё исчезновение — клики по контенту блокируются. */
    public static boolean isSwitchLeaving() {
        return switching;
    }

    /** Прогресс исчезновения старого контента: 1 → 0. */
    public static float itemsLeave() {
        if (!switching) return 0f;
        return 1f - clamp01(elapsedMs(switchStartedAt) / ITEM_LEAVE_DURATION_MS);
    }

    public static Category leavingCategory() {
        return leavingCategory;
    }

    public static List<Module> leavingModules() {
        return leavingModules;
    }

    public static float leavingScroll() {
        return leavingScroll;
    }

    /**
     * Прогресс появления элемента с индексом index: 0 → 1.
     * Элементы всплывают со сдвигом (stagger), срез по суммарному времени,
     * чтобы в длинных списках анимация не растягивалась слишком сильно.
     */
    public static float itemAppear(int index) {
        float delay = Math.min(index * ITEM_APPEAR_STAGGER_MS, ITEM_APPEAR_MAX_STAGGER_MS);
        float t = elapsedMs(categoryItemsStartedAt) - delay;
        if (t <= 0f) return 0f;
        return easeOutCubic(t / (ITEM_APPEAR_DURATION_MS + delay * 0.35f));
    }

    /** Прогресс появления строки сайдбара при открытии окна. */
    public static float sidebarAppear(int index) {
        float t = elapsedMs(sidebarStartedAt) - index * SIDEBAR_APPEAR_STAGGER_MS;
        if (t <= 0f) return 0f;
        return easeOutCubic(t / SIDEBAR_APPEAR_DURATION_MS);
    }

    // ==================== Трансформация окна ====================

    public static double toLocalX(double mouseX) {
        float s = windowScale < 0.001f ? 1f : windowScale;
        float cx = x + width / 2f;
        return (mouseX - cx) / s + cx;
    }

    public static double toLocalY(double mouseY) {
        float s = windowScale < 0.001f ? 1f : windowScale;
        float cy = y + height / 2f;
        return (mouseY - cy - windowOffsetY) / s + cy;
    }

    private static <K> void stepAnim(Map<K, Float> map, K key, float target, float speed, float def) {
        float v = map.getOrDefault(key, def);
        v = lerp(v, target, speed);
        if (Math.abs(v - target) < 0.005f) v = target;
        map.put(key, v);
    }

    public static void updateAnimations() {
        updateWindowAnimation();

        if (switching && itemsLeave() <= 0f) {
            switching = false;
            leavingModules = null;
        }

        if (modules != null) {
            for (Module m : modules) {
                stepAnim(toggleAnim, m, m.enable ? 1f : 0f, 0.28f, m.enable ? 1f : 0f);
                stepAnim(openAnim, m, m.open ? 1f : 0f, 0.32f, 0f);
                for (Setting s : m.getSettingsForGUI()) {
                    stepAnim(dropAnim, s, isSettingOpened(s) ? 1f : 0f, 0.32f, isSettingOpened(s) ? 1f : 0f);
                    if (s instanceof BooleanSetting b) {
                        stepAnim(boolAnim, s, b.get() ? 1f : 0f, 0.28f, b.get() ? 1f : 0f);
                    }
                    if (s instanceof MultiBooleanSetting mb) {
                        for (BooleanSetting b : mb.settings) {
                            stepAnim(boolAnim, b, b.get() ? 1f : 0f, 0.28f, b.get() ? 1f : 0f);
                        }
                    }
                }
            }
        }
        if (categories != null) {
            float target = categoryY(categoryIndex(selectedCategories));
            if (categoryAnimY < 0) categoryAnimY = target;
            categoryAnimY = lerp(categoryAnimY, target, 0.35f);
        }
    }

    private static void updateWindowAnimation() {
        float p;
        if (!closing) {
            p = clamp01(elapsedMs(windowStartedAt) / OPEN_DURATION_MS);
            if (p >= 1f) {
                // Анимация доиграла — прилипаем к точным значениям,
                // чтобы рендер был попиксельно ровным, без субпиксельного смещения.
                windowScale = 1f;
                windowOffsetY = 0f;
                alphaMul = 1f;
            } else {
                float e = easeOutBack(p);
                float a = easeOutCubic(p * 1.35f);
                windowScale = 0.9f + 0.1f * e;
                windowOffsetY = (1f - easeOutQuint(p)) * 14f;
                alphaMul = a;
            }
        } else {
            float q = clamp01(elapsedMs(windowStartedAt) / CLOSE_DURATION_MS);
            float e = easeInQuad(q);
            windowScale = 1f - 0.08f * e;
            windowOffsetY = e * 9f;
            alphaMul = 1f - clamp01(q * 1.25f);
            p = 1f - q;
        }
        windowProgress = p;
        GlyphBuffer.alphaMultiplier = alphaFactor();
    }

    private static boolean isSettingOpened(Setting s) {
        if (s instanceof ModeSetting ms) return ms.opened;
        if (s instanceof MultiBooleanSetting mb) return mb.opened;
        if (s instanceof HueSetting h) return h.opened;
        return false;
    }

    public static float toggle(Module m) {
        return toggleAnim.getOrDefault(m, m.enable ? 1f : 0f);
    }

    public static float open(Module m) {
        return openAnim.getOrDefault(m, 0f);
    }

    public static float drop(Setting s) {
        return dropAnim.getOrDefault(s, isSettingOpened(s) ? 1f : 0f);
    }

    public static float bool(Setting s) {
        if (s instanceof BooleanSetting b) return boolAnim.getOrDefault(s, b.get() ? 1f : 0f);
        return 0f;
    }

    // ==================== Раскладка ====================

    public static float contentX() {
        return x + SIDEBAR_W + CONTENT_PAD;
    }

    public static float contentY() {
        return y + CONTENT_PAD;
    }

    public static float contentW() {
        return width - SIDEBAR_W - CONTENT_PAD * 2;
    }

    public static float cardW() {
        return contentW() - 5;
    }

    public static float modulesY() {
        return contentY() + HEADER_H;
    }

    public static float modulesH() {
        return y + height - CONTENT_PAD - modulesY();
    }

    public static int categoryIndex(Category c) {
        int i = 0;
        for (Category cat : categories) {
            if (cat == c) return i;
            i++;
        }
        return 0;
    }

    public static float categoryY(int index) {
        return y + CAT_START + index * (CAT_ROW_H + CAT_GAP);
    }

    public static String categoryIcon(Category c) {
        return switch (c) {
            case Player -> "player";
            case World -> "world";
            case Utilities -> "utilities";
            case Other -> "other";
            case Interface -> "interface";
            case Theme -> "settings";
        };
    }

    // ==================== Цвета ====================

    public static boolean glass() {
        Theme t = ThemeManager.getTheme();
        return t == Theme.TRANSPARENT_WHITE || t == Theme.TRANSPARENT_BLACK || t == Theme.PURPLE || t == Theme.PINK;
    }

    // ==================== Прозрачность ====================

    /**
     * Итоговый множитель прозрачности: окно (открытие/закрытие) × элемент (появление).
     */
    public static float alphaFactor() {
        float f = alphaMul * contentAlpha;
        f = f < 0f ? 0f : (f > 1f ? 1f : f);
        // Текст рисуется собственным рендерером, который не читает альфу из цвета,
        // поэтому множитель прозрачности передаём ему отдельно.
        GlyphBuffer.alphaMultiplier = f;
        return f;
    }

    /** Применяет текущий множитель прозрачности к произвольному цвету. */
    public static int fade(int color) {
        int a = (int) Math.round(RenderUtil.ColorUtil.alpha(color) * alphaFactor());
        return RenderUtil.ColorUtil.replAlpha(color, a < 0 ? 0 : (a > 255 ? 255 : a));
    }

    /** То же самое для java.awt.Color. */
    public static Color fade(Color color) {
        int a = (int) Math.round(color.getAlpha() * alphaFactor());
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), a < 0 ? 0 : (a > 255 ? 255 : a));
    }

    /** Применяет заданную альфу с учётом текущего множителя прозрачности. */
    private static int alpha(int base) {
        int a = (int) Math.round(base * alphaFactor());
        return a < 0 ? 0 : (a > 255 ? 255 : a);
    }

    private static int withOwnAlpha(int color) {
        return RenderUtil.ColorUtil.replAlpha(color, alpha(RenderUtil.ColorUtil.alpha(color)));
    }

    public static int bg() {
        return withOwnAlpha(RenderUtil.ColorUtil.getBackGroundColor(1, 1));
    }

    public static int mainC() {
        return withOwnAlpha(RenderUtil.ColorUtil.getMainColor(1, 1));
    }

    public static int text() {
        return withOwnAlpha(RenderUtil.ColorUtil.getTextColor(1, 1));
    }

    public static int text(int a) {
        return RenderUtil.ColorUtil.replAlpha(RenderUtil.ColorUtil.getTextColor(1, 1), alpha(a));
    }

    public static int window() {
        return glass() ? RenderUtil.ColorUtil.replAlpha(bg(), alpha(145)) : bg();
    }

    public static int sidebar() {
        return glass() ? RenderUtil.ColorUtil.replAlpha(mainC(), alpha(90)) : mainC();
    }

    public static int card() {
        return glass() ? RenderUtil.ColorUtil.replAlpha(mainC(), alpha(70)) : mainC();
    }

    public static int control() {
        return glass() ? RenderUtil.ColorUtil.replAlpha(mainC(), alpha(150)) : bg();
    }

    private static final int ACCENT_RGB = new Color(0x8A7CFF).getRGB();

    public static int accent() {
        return RenderUtil.ColorUtil.replAlpha(ACCENT_RGB, alpha(255));
    }

    public static int accent(int a) {
        return RenderUtil.ColorUtil.replAlpha(ACCENT_RGB, alpha(a));
    }

    // ==================== Размеры настроек ====================

    public static List<Setting> visibleSettings(Module m) {
        List<Setting> out = new ArrayList<>();
        for (Setting s : m.getSettingsForGUI()) {
            if (!s.hidden.get()) out.add(s);
        }
        return out;
    }

    public static float settingHeight(Setting s) {
        float h = s instanceof SliderSetting ? SLIDER_H : ROW_H;
        if (s instanceof ModeSetting ms) h += ms.modes.size() * DROPDOWN_ROW_H * drop(s);
        if (s instanceof MultiBooleanSetting mb) h += mb.settings.size() * DROPDOWN_ROW_H * drop(s);
        if (s instanceof HueSetting) h += HUE_EXTRA_H * drop(s);
        return h + SETTING_GAP;
    }

    public static float settingsHeight(Module m) {
        float h = 0;
        if (!m.getSettingsForGUI().isEmpty()) {
            for (Setting s : visibleSettings(m)) h += settingHeight(s);
            if (h > 0) h += 3;
        }
        return h;
    }

    public static float moduleHeight(Module m) {
        return MODULE_H + settingsHeight(m) * open(m);
    }

    public static float contentHeight() {
        return contentHeight(modules);
    }

    public static float contentHeight(List<Module> list) {
        float h = 0;
        if (list == null) return 0;
        for (Module m : list) h += moduleHeight(m) + MODULE_GAP;
        return h;
    }

    // ==================== Текст ====================

    public static String trim(String s, float maxW, int size) {
        if (s == null) return "";
        if (FontDraw.getWidth(FontDraw.FontType.MEDIUM, s, size) <= maxW) return s;
        String out = s;
        while (!out.isEmpty() && FontDraw.getWidth(FontDraw.FontType.MEDIUM, out + "...", size) > maxW) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "...";
    }
}
