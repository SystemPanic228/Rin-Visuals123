package ru.strange.client.ui.clickgui.render;

import me.x150.renderer.fontng.GlyphBuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import ru.strange.client.Strange;
import ru.strange.client.module.api.Category;
import ru.strange.client.module.api.Module;
import ru.strange.client.ui.clickgui.GuiScreen;
import ru.strange.client.utils.other.KeyUtil;
import ru.strange.client.utils.render.FontDraw;
import ru.strange.client.utils.render.RenderUtil;

import java.awt.*;
import java.util.List;

public class GuiRenderModule extends GuiScreen {
    public static void renderModule(DrawContext ctx, int mouseX, int mouseY) {
        float cx = contentX();
        float cw = contentW();
        float my = modulesY();
        float mh = modulesH();

        // Шапка контента
        float headIn = itemAppear(0);
        float headOut = itemsLeave();
        Category leavingCat = leavingCategory();

        if (headOut > 0.01f && leavingCat != null) {
            renderHeader(ctx, leavingCat, headOut, (1f - headOut) * ITEM_LEAVE_Y);
        }
        if (headIn > 0.01f) {
            renderHeader(ctx, selectedCategories, headIn, (1f - headIn) * ITEM_APPEAR_Y * 0.5f);
        }

        ctx.enableScissor(
                (int) cx,
                (int) my,
                (int) (cx + cw),
                (int) (my + mh)
        );

        scroll.update();

        // Старый контент доигрывает исчезновение
        if (isSwitchLeaving() && leavingCat != null && leavingCat != Category.Theme && leavingModules() != null) {
            float lv = itemsLeave();
            contentAlpha = lv;
            GlyphBuffer.alphaMultiplier = alphaFactor();
            renderList(ctx, leavingModules(), cx, my, mh, mouseX, mouseY, leavingScroll(),
                    0f, (1f - lv) * -ITEM_LEAVE_Y, false);
            contentAlpha = 1f;
            GlyphBuffer.alphaMultiplier = alphaFactor();
        }

        if (selectedCategories != Category.Theme && modules != null) {
            renderList(ctx, modules, cx, my, mh, mouseX, mouseY, scroll.getScroll(), 0f, 0f, true);
        }

        scroll.setMax(contentHeight(), mh);

        ctx.disableScissor();

        // Скроллбар
        float ch = contentHeight();
        if (ch > mh) {
            float barH = Math.max(20, mh * (mh / ch));
            float progress = mh - barH > 0 ? (-scroll.getScroll() / (ch - mh)) : 0;
            progress = Math.max(0, Math.min(1, progress));
            float barY = my + progress * (mh - barH);
            RenderUtil.Round.draw(ctx, cx + cw - 2.5f, barY, 2, barH, 1, text(70));
        }
    }

    private static void renderHeader(DrawContext ctx, Category cat, float alpha, float offsetY) {
        contentAlpha = alpha;
        GlyphBuffer.alphaMultiplier = alphaFactor();
        float cx = contentX();
        FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, cat.getName(), cx + 2, y + 19 + offsetY, 10, text(245));
        String sub;
        if (cat == Category.Theme) {
            sub = "Тем: " + (themes != null ? themes.length : 0);
        } else {
            List<Module> list = Strange.get != null ? Strange.get.manager.getType(cat) : null;
            sub = "Модулей: " + (list != null ? list.size() : 0);
        }
        FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, sub, cx + 2, y + 27 + offsetY, 5, text(125));
        contentAlpha = 1f;
        GlyphBuffer.alphaMultiplier = alphaFactor();
    }

    private static void renderList(DrawContext ctx, List<Module> list, float cx, float my, float mh,
                                   double mouseX, double mouseY, float scrollY,
                                   float offX, float offY, boolean appear) {
        float cardW = cardW();
        float yDown = 0f;

        for (int i = 0; i < list.size(); i++) {
            Module module = list.get(i);
            float cardH = moduleHeight(module);
            float drawY = my + yDown + scrollY;

            float p = appear ? itemAppear(i) : 1f;
            if (p > 0.002f && drawY + cardH > my - 40 && drawY < my + mh + 40) {
                float scale = appear ? ITEM_APPEAR_SCALE_FROM + (1f - ITEM_APPEAR_SCALE_FROM) * p : 1f;
                float dx = appear ? (1f - p) * ITEM_APPEAR_X + offX : offX;
                float dy = appear ? (1f - p) * ITEM_APPEAR_Y + offY : offY;

                if (appear) {
                    contentAlpha = p;
                    GlyphBuffer.alphaMultiplier = alphaFactor();
                }

                if (scale > 0.9999f && scale < 1.0001f && dx == 0f && dy == 0f) {
                    // Конечное состояние — рисуем без трансформации, попиксельно ровно
                    renderCard(ctx, module, cx, drawY, cardW, cardH, mouseX, mouseY);
                } else {
                    float ccx = cx + cardW / 2f;
                    float ccy = drawY + cardH / 2f;

                    // Мышь в систему координат карточки (с учётом её собственной анимации)
                    double lmx = (mouseX - ccx) / scale - dx + ccx;
                    double lmy = (mouseY - ccy) / scale - dy + ccy;

                    Matrix3x2fStack matrices = ctx.getMatrices();
                    matrices.pushMatrix();
                    matrices.translate(ccx, ccy);
                    matrices.scale(scale, scale);
                    matrices.translate(-ccx, -ccy);
                    matrices.translate(dx, dy);

                    renderCard(ctx, module, cx, drawY, cardW, cardH, lmx, lmy);

                    matrices.popMatrix();
                }

                if (appear) {
                    contentAlpha = 1f;
                    GlyphBuffer.alphaMultiplier = alphaFactor();
                }
            }

            yDown += cardH + MODULE_GAP;
        }
    }

    private static void renderCard(DrawContext ctx, Module module, float cardX, float cardY, float cardW, float cardH, double mouseX, double mouseY) {
        float t = toggle(module);
        boolean hoverHeader = isHovered(mouseX, mouseY, cardX, cardY, cardW, MODULE_H);

        // Карточка
        RenderUtil.Round.draw(ctx, cardX, cardY, cardW, cardH, 7, card());
        if (hoverHeader) {
            RenderUtil.Round.draw(ctx, cardX, cardY, cardW, MODULE_H, 7, text(10));
        }

        // Акцентная полоска слева при включении
        if (t > 0.02f) {
            RenderUtil.Round.draw(ctx, cardX, cardY + 6, 2.5f, MODULE_H - 12, 1.2f, accent((int) (210 * t)));
        }

        // Иконка
        RenderUtil.Image.draw(
                ctx,
                Identifier.of(Strange.rootRes, "/icons/gui/" + categoryIcon(selectedCategories) + ".png"),
                cardX + 9,
                cardY + 9.5f,
                11,
                11,
                text(205)
        );

        // Название + бинд
        String displayText = module.getDisplayName();
        if (!module.binding) {
            String bindText = KeyUtil.getKey(module.bind);
            if (!bindText.equals("null")) {
                displayText += " [" + bindText + "]";
            }
        }
        float nameMaxW = cardW - 25 - 42 - (module.getSettingsForGUI().isEmpty() ? 0 : 12);
        FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(displayText, nameMaxW, 7), cardX + 25, cardY + 12, 7, module.binding ? accent() : text(240));

        // Описание
        if (module.description != null && !module.description.isEmpty()) {
            FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(module.description, nameMaxW, 5), cardX + 25, cardY + 20.5f, 5, text(125));
        }

        // Свитч
        float swX = cardX + cardW - SWITCH_W - 9;
        float swY = cardY + (MODULE_H - SWITCH_H) / 2f;
        drawSwitch(ctx, swX, swY, t);

        // Индикатор настроек (три точки)
        if (!module.getSettingsForGUI().isEmpty()) {
            float dx = swX - 11;
            int dotsColor = module.open ? accent() : text(135);
            RenderUtil.Round.draw(ctx, dx, cardY + 10.5f, 2.5f, 2.5f, 1.25f, dotsColor);
            RenderUtil.Round.draw(ctx, dx, cardY + 14.25f, 2.5f, 2.5f, 1.25f, dotsColor);
            RenderUtil.Round.draw(ctx, dx, cardY + 18, 2.5f, 2.5f, 1.25f, dotsColor);
        }

        // Настройки (с обрезкой по мере раскрытия карточки)
        float oa = open(module);
        if (!module.getSettingsForGUI().isEmpty() && oa > 0.01f) {
            ctx.enableScissor(
                    (int) cardX,
                    (int) (cardY + MODULE_H),
                    (int) (cardX + cardW),
                    (int) (cardY + cardH)
            );
            GuiRenderSettings.renderSettings(ctx, visibleSettings(module), cardX + 6, cardY + MODULE_H + 2, cardW - 12, mouseX, mouseY);
            ctx.disableScissor();
        }
    }

    public static void drawSwitch(DrawContext ctx, float swX, float swY, float t) {
        // Трек-капсула
        int track = RenderUtil.ColorUtil.interpolate(control(), accent(), t);
        RenderUtil.Round.draw(ctx, swX, swY, SWITCH_W, SWITCH_H, SWITCH_H / 2f, track);
        // Тонкая обводка, чтобы тумблер читался и на прозрачных темах
        RenderUtil.Border.draw(ctx, swX, swY, SWITCH_W, SWITCH_H, SWITCH_H / 2f, 0.5f, text(28));

        // Ручка: 8×8, ровно по центру трека, ход 10px
        float knobSize = SWITCH_KNOB;
        float knobX = swX + 2 + t * (SWITCH_W - knobSize - 4);
        float knobY = swY + (SWITCH_H - knobSize) / 2f;
        int knob = RenderUtil.ColorUtil.interpolate(text(225), fade(new Color(0xFFFFFF).getRGB()), t);
        RenderUtil.Round.draw(ctx, knobX, knobY, knobSize, knobSize, knobSize / 2f, knob);
    }
}
