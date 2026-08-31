package ru.strange.client.ui.clickgui.render;

import me.x150.renderer.fontng.GlyphBuffer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import ru.strange.client.ui.clickgui.GuiScreen;

public class GuiRender extends GuiScreen {
    public static void renderGui(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        x = mc.getWindow().getScaledWidth() / 2f - width / 2f;
        y = mc.getWindow().getScaledHeight() / 2f - height / 2f;

        updateAnimations();

        float scale = GuiScreen.windowScale;
        float dy = GuiScreen.windowOffsetY;
        // В покое трансформацию не применяем вовсе — рендер остаётся
        // попиксельно ровным, без субпиксельного смещения текста.
        boolean animated = scale < 0.9999f || scale > 1.0001f || dy != 0f;

        int localX = mouseX;
        int localY = mouseY;

        Matrix3x2fStack matrices = context.getMatrices();

        if (animated) {
            if (scale < 0.001f) scale = 0.001f;
            float cx = x + width / 2f;
            float cy = y + height / 2f;

            // Мышь переводим в локальные координаты окна, чтобы ховер
            // работал корректно, пока окно масштабируется анимацией.
            localX = (int) Math.round((mouseX - cx) / scale + cx);
            localY = (int) Math.round((mouseY - cy - dy) / scale + cy);

            matrices.pushMatrix();
            matrices.translate(cx, cy);
            matrices.scale(scale, scale);
            matrices.translate(-cx, -cy);
            matrices.translate(0, dy);
        }

        contentAlpha = 1f;
        GlyphBuffer.alphaMultiplier = alphaFactor();

        GuiRenderBackGround.renderBackGround(context, localX, localY);
        GuiRenderCategory.renderCategory(context, localX, localY);
        GuiRenderModule.renderModule(context, localX, localY);
        GuiRenderTheme.renderTheme(context, localX, localY);

        contentAlpha = 1f;
        GlyphBuffer.alphaMultiplier = 1f;

        if (animated) {
            matrices.popMatrix();
        }
    }
}
