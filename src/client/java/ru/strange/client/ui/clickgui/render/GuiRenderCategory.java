package ru.strange.client.ui.clickgui.render;

import me.x150.renderer.fontng.GlyphBuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import ru.strange.client.Strange;
import ru.strange.client.module.api.Category;
import ru.strange.client.ui.clickgui.GuiScreen;
import ru.strange.client.utils.render.FontDraw;
import ru.strange.client.utils.render.RenderUtil;

public class GuiRenderCategory extends GuiScreen {
    public static void renderCategory(DrawContext ctx, int mouseX, int mouseY) {
        float rowX = x + 6;
        float rowW = SIDEBAR_W - 12;

        // Плавающая подсветка выбранной категории
        float pill = sidebarAppear(0);
        if (pill > 0.01f) {
            contentAlpha = pill;
            float py = categoryAnimY + (1f - pill) * 4f;
            RenderUtil.Round.draw(ctx, rowX, py, rowW, CAT_ROW_H, 6, accent(45));
            RenderUtil.Round.draw(ctx, rowX + 3, py + 5.5f, 2.5f, CAT_ROW_H - 11, 1.2f, accent());
            contentAlpha = 1f;
        }

        int i = 0;
        for (Category category : categories) {
            float rowY = categoryY(i);
            boolean selected = category == selectedCategories;

            float p = sidebarAppear(i + 1);
            if (p <= 0.002f) {
                i++;
                continue;
            }

            float scale = 0.94f + 0.06f * p;
            float dx = (1f - p) * -14f;
            boolean identity = p >= 1f;

            double lmx = mouseX;
            double lmy = mouseY;

            contentAlpha = p;

            Matrix3x2fStack matrices = null;
            if (!identity) {
                float ccx = rowX + rowW / 2f;
                float ccy = rowY + CAT_ROW_H / 2f;

                lmx = (mouseX - ccx) / scale - dx + ccx;
                lmy = (mouseY - ccy) / scale + ccy;

                matrices = ctx.getMatrices();
                matrices.pushMatrix();
                matrices.translate(ccx, ccy);
                matrices.scale(scale, scale);
                matrices.translate(-ccx, -ccy);
                matrices.translate(dx, 0);
            }

            if (!selected && isHovered(lmx, lmy, rowX, rowY, rowW, CAT_ROW_H)) {
                RenderUtil.Round.draw(ctx, rowX, rowY, rowW, CAT_ROW_H, 6, text(14));
            }

            int iconColor = selected ? accent() : text(165);
            int nameColor = selected ? text(250) : text(175);

            RenderUtil.Image.draw(
                    ctx,
                    Identifier.of(Strange.rootRes, "/icons/gui/" + categoryIcon(category) + ".png"),
                    rowX + 10,
                    rowY + 5.5f,
                    10,
                    10,
                    iconColor
            );

            FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, category.getName(), rowX + 25, rowY + 13f, 6, nameColor);

            if (matrices != null) {
                matrices.popMatrix();
            }

            contentAlpha = 1f;
            i++;
        }
    }
}
