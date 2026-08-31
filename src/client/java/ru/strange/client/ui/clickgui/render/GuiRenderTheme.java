package ru.strange.client.ui.clickgui.render;

import me.x150.renderer.fontng.GlyphBuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import ru.strange.client.Strange;
import ru.strange.client.module.Theme;
import ru.strange.client.module.ThemeManager;
import ru.strange.client.module.api.Category;
import ru.strange.client.ui.clickgui.GuiScreen;
import ru.strange.client.utils.render.FontDraw;
import ru.strange.client.utils.render.RenderUtil;

import java.awt.*;

public class GuiRenderTheme extends GuiScreen {

    public static final float THEME_GAP = 6f;
    public static final float THEME_CARD_H = 34f;

    public static float themeCardW() {
        return (contentW() - THEME_GAP) / 2f;
    }

    public static float themeX(int index) {
        return contentX() + (index % 2) * (themeCardW() + THEME_GAP);
    }

    public static float themeY(int index) {
        return modulesY() + (index / 2) * (THEME_CARD_H + THEME_GAP);
    }

    public static void renderTheme(DrawContext ctx, int mouseX, int mouseY) {
        if (themes == null) return;

        // Старые карточки тем доигрывают исчезновение
        if (isSwitchLeaving() && leavingCategory() == Category.Theme) {
            float lv = itemsLeave();
            contentAlpha = lv;
            GlyphBuffer.alphaMultiplier = alphaFactor();
            renderCards(ctx, mouseX, mouseY, 0f, (1f - lv) * -ITEM_LEAVE_Y, false);
            contentAlpha = 1f;
            GlyphBuffer.alphaMultiplier = alphaFactor();
        }

        if (selectedCategories == Category.Theme) {
            renderCards(ctx, mouseX, mouseY, 0f, 0f, true);
        }
    }

    private static void renderCards(DrawContext ctx, double mouseX, double mouseY,
                                    float offX, float offY, boolean appear) {
        for (int index = 0; index < themes.length; index++) {
            Theme theme = themes[index];
            float drawX = themeX(index);
            float drawY = themeY(index);
            float cardW = themeCardW();
            boolean selected = ThemeManager.getTheme() == theme;

            float p = appear ? itemAppear(index) : 1f;
            if (p <= 0.002f) continue;

            float scale = appear ? ITEM_APPEAR_SCALE_FROM + (1f - ITEM_APPEAR_SCALE_FROM) * p : 1f;
            float dx = appear ? (1f - p) * ITEM_APPEAR_X * 0.6f + offX : offX;
            float dy = appear ? (1f - p) * ITEM_APPEAR_Y + offY : offY;

            if (appear) {
                contentAlpha = p;
                GlyphBuffer.alphaMultiplier = alphaFactor();
            }

            boolean identity = scale > 0.9999f && scale < 1.0001f && dx == 0f && dy == 0f;
            Matrix3x2fStack matrices = null;
            double mx = mouseX;
            double my = mouseY;

            if (!identity) {
                float ccx = drawX + cardW / 2f;
                float ccy = drawY + THEME_CARD_H / 2f;

                matrices = ctx.getMatrices();
                matrices.pushMatrix();
                matrices.translate(ccx, ccy);
                matrices.scale(scale, scale);
                matrices.translate(-ccx, -ccy);
                matrices.translate(dx, dy);

                mx = (mouseX - ccx) / scale - dx + ccx;
                my = (mouseY - ccy) / scale - dy + ccy;
            }

            boolean hover = isHovered(mx, my, drawX, drawY, cardW, THEME_CARD_H);

            RenderUtil.Round.draw(ctx, drawX, drawY, cardW, THEME_CARD_H, 7, card());
            if (hover && !selected) {
                RenderUtil.Round.draw(ctx, drawX, drawY, cardW, THEME_CARD_H, 7, text(10));
            }
            if (selected) {
                RenderUtil.Border.draw(ctx, drawX, drawY, cardW, THEME_CARD_H, 7, 0.6f, accent());
            }

            String name = theme.toString().toLowerCase();
            RenderUtil.Image.draw(
                    ctx,
                    Identifier.of(Strange.rootRes, "/textures/theme/" + name + ".png"),
                    drawX + 3,
                    drawY + 3,
                    cardW - 6,
                    15f,
                    4,
                    new Color(255, 255, 255, (int) Math.round(255 * alphaFactor()))
            );

            FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, theme.getName(), drawX + 6, drawY + 27, 6, selected ? accent() : text(220));

            if (selected) {
                RenderUtil.Round.draw(ctx, drawX + cardW - 11, drawY + 23, 5, 5, 2.5f, accent());
            }

            if (matrices != null) {
                matrices.popMatrix();
            }

            if (appear) {
                contentAlpha = 1f;
                GlyphBuffer.alphaMultiplier = alphaFactor();
            }
        }
    }
}
