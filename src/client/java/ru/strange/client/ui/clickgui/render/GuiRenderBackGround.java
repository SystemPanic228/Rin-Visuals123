package ru.strange.client.ui.clickgui.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import ru.strange.client.Strange;
import ru.strange.client.ui.clickgui.GuiScreen;
import ru.strange.client.utils.render.FontDraw;
import ru.strange.client.utils.render.RenderUtil;

import java.awt.*;

public class GuiRenderBackGround extends GuiScreen {
    public static void renderBackGround(DrawContext ctx, int mouseX, int mouseY) {
        // Тень и фон окна
        RenderUtil.Shadow.draw(ctx, x - 2, y - 2, width + 4, height + 4, 10, 14, fade(new Color(0x40000000, true).getRGB()));
        if (glass()) {
            RenderUtil.Blur.draw(ctx, x, y, width, height, 10, 20, fade(new Color(255, 255, 255).getRGB()));
        }
        RenderUtil.Round.draw(ctx, x, y, width, height, 10, window());

        // Сайдбар (скругление только слева; через ножницы, чтобы не было наложения альфы)
        ctx.enableScissor((int) x, (int) y, (int) (x + SIDEBAR_W - 10), (int) (y + height));
        RenderUtil.Round.draw(ctx, x, y, SIDEBAR_W, height, 10, sidebar());
        ctx.disableScissor();
        RenderUtil.Rect.draw(ctx, x + SIDEBAR_W - 10, y, 10, height, sidebar());

        // Разделитель сайдбара и контента
        RenderUtil.Rect.draw(ctx, x + SIDEBAR_W, y + 12, 1, height - 24, text(22));

        // Логотип и название
        RenderUtil.Image.draw(ctx, Identifier.of(Strange.rootRes, "/icons/gui/logo.png"), x + 13, y + 13, 19, 19, text(225));
        FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, Strange.name, x + 38, y + 21, 9, text(240));
        FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, "FREE VERSION", x + 38, y + 29.5f, 5, accent(210));

        // Разделитель под шапкой
        RenderUtil.Rect.draw(ctx, x + 10, y + 41, SIDEBAR_W - 20, 1, text(22));

        // Низ сайдбара
        FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, "v1.0", x + 10, y + height - 12, 5, text(110));
        String user = mc.getSession() != null ? mc.getSession().getUsername() : "";
        if (!user.isEmpty()) {
            float uw = FontDraw.getWidth(FontDraw.FontType.MEDIUM, user, 5);
            FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(user, SIDEBAR_W - 30, 5), x + SIDEBAR_W - 10 - Math.min(uw, SIDEBAR_W - 30), y + height - 12, 5, text(110));
        }
    }
}
