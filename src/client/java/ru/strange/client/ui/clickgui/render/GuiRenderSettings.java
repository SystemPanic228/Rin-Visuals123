package ru.strange.client.ui.clickgui.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import ru.strange.client.Strange;
import ru.strange.client.module.api.setting.Setting;
import ru.strange.client.module.api.setting.impl.*;
import ru.strange.client.ui.clickgui.GuiScreen;
import ru.strange.client.utils.math.MathHelper;
import ru.strange.client.utils.other.KeyUtil;
import ru.strange.client.utils.render.FontDraw;
import ru.strange.client.utils.render.RenderUtil;

import java.awt.*;

public class GuiRenderSettings extends GuiScreen {

    // Геометрия пикера цвета (должна совпадать с GuiMouseClickedSettings)
    public static float huePanelH() { return 68f; }
    public static float hueSVH() { return 48f; }
    public static float hueBarY() { return 58f; }
    public static float hueBarH() { return 5f; }

    public static void renderSettings(DrawContext ctx, java.util.List<Setting> settings, float x, float y, float rowW, double mouseX, double mouseY) {
        float up = 0;
        for (Setting setting : settings) {
            float rowY = y + up;

            if (setting instanceof BooleanSetting s) {
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.name, rowW - SWITCH_W - 8, 6), x + 2, rowY + 10.5f, 6, text(220));
                GuiRenderModule.drawSwitch(ctx, x + rowW - SWITCH_W, rowY + (ROW_H - SWITCH_H) / 2f, bool(s));
            }

            if (setting instanceof SliderSetting s) {
                float trackX = x + 2;
                float trackW = rowW - 4;

                if (s.sliding) {
                    float frac = MathHelper.clamp((float) (mouseX - trackX) / trackW, 0f, 1f);
                    s.current = (float) MathHelper.round(frac * (s.maximum - s.minimum) + s.minimum, s.increment);
                    s.triggerAutoSave();
                }
                s.sliderWidth = MathHelper.interpolate(((s.current - s.minimum) / (s.maximum - s.minimum)) * trackW, s.sliderWidth, 0.3);

                String value = String.valueOf(s.get());
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.name, rowW - 30, 6), x + 2, rowY + 9, 6, text(220));
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, value, x + rowW - 2 - FontDraw.getWidth(FontDraw.FontType.MEDIUM, value, 6), rowY + 9, 6, accent(235));

                // Трек + заливка + ползунок
                RenderUtil.Round.draw(ctx, trackX, rowY + 14, trackW, 4, 2, control());
                if (s.sliderWidth > 1) {
                    RenderUtil.Round.draw(ctx, trackX, rowY + 14, MathHelper.clamp(s.sliderWidth, 2, trackW), 4, 2, accent());
                }
                float knobX = trackX + MathHelper.clamp(s.sliderWidth, 0, trackW) - 3.5f;
                RenderUtil.Round.draw(ctx, knobX, rowY + 12.5f, 7, 7, 3.5f, fade(new Color(0xFFFFFF).getRGB()));
            }

            if (setting instanceof ModeSetting s) {
                float bx = x + rowW - MODE_BOX_W;
                float anim = drop(s);
                float optionsH = s.modes.size() * DROPDOWN_ROW_H * anim;

                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.name, rowW - MODE_BOX_W - 8, 6), x + 2, rowY + 10.5f, 6, text(220));

                // Бокс
                RenderUtil.Round.draw(ctx, bx, rowY + 2, MODE_BOX_W, BOX_H, 4, s.opened ? accent(45) : control());
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.get(), MODE_BOX_W - 20, 5), bx + 5, rowY + 10, 5, text(230));
                RenderUtil.Image.draw(ctx, Identifier.of(Strange.rootRes, "/icons/gui/m_d.png"), bx + MODE_BOX_W - 11, rowY + 4, 8, 8, text(160));

                // Выпадающий список
                if (anim > 0.01f) {
                    float oy = rowY + 2 + BOX_H + 2;
                    ctx.enableScissor((int) bx, (int) oy, (int) (bx + MODE_BOX_W), (int) (oy + optionsH));
                    RenderUtil.Round.draw(ctx, bx, oy, MODE_BOX_W, optionsH, 4, control());
                    for (int i = 0; i < s.modes.size(); i++) {
                        float oyy = oy + i * DROPDOWN_ROW_H;
                        boolean hover = isHovered(mouseX, mouseY, bx, oyy, MODE_BOX_W, DROPDOWN_ROW_H);
                        boolean selected = s.modes.get(i).equals(s.currentMode);
                        if (hover) {
                            RenderUtil.Round.draw(ctx, bx + 2, oyy + 1, MODE_BOX_W - 4, DROPDOWN_ROW_H - 2, 3, text(16));
                        }
                        FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.modes.get(i), MODE_BOX_W - 10, 5), bx + 5, oyy + 8, 5, selected ? accent() : text(160));
                    }
                    ctx.disableScissor();
                }
            }

            if (setting instanceof MultiBooleanSetting s) {
                float bx = x + rowW - MODE_BOX_W;
                float anim = drop(s);
                float optionsH = s.settings.size() * DROPDOWN_ROW_H * anim;

                long count = s.settings.stream().filter(BooleanSetting::get).count();
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.name, rowW - MODE_BOX_W - 8, 6), x + 2, rowY + 10.5f, 6, text(220));

                // Бокс
                RenderUtil.Round.draw(ctx, bx, rowY + 2, MODE_BOX_W, BOX_H, 4, s.opened ? accent(45) : control());
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, count + "/" + s.settings.size(), bx + 5, rowY + 10, 5, text(230));
                RenderUtil.Image.draw(ctx, Identifier.of(Strange.rootRes, "/icons/gui/m_d.png"), bx + MODE_BOX_W - 11, rowY + 4, 8, 8, text(160));

                // Выпадающий список с чекбоксами
                if (anim > 0.01f) {
                    float oy = rowY + 2 + BOX_H + 2;
                    ctx.enableScissor((int) bx, (int) oy, (int) (bx + MODE_BOX_W), (int) (oy + optionsH));
                    RenderUtil.Round.draw(ctx, bx, oy, MODE_BOX_W, optionsH, 4, control());
                    for (int i = 0; i < s.settings.size(); i++) {
                        BooleanSetting child = s.settings.get(i);
                        float oyy = oy + i * DROPDOWN_ROW_H;
                        boolean hover = isHovered(mouseX, mouseY, bx, oyy, MODE_BOX_W, DROPDOWN_ROW_H);
                        if (hover) {
                            RenderUtil.Round.draw(ctx, bx + 2, oyy + 1, MODE_BOX_W - 4, DROPDOWN_ROW_H - 2, 3, text(16));
                        }
                        float ca = bool(child);
                        int checkColor = RenderUtil.ColorUtil.interpolate(text(40), accent(), ca);
                        RenderUtil.Round.draw(ctx, bx + 5, oyy + 2.5f, 6, 6, 2, checkColor);
                        FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(child.name, MODE_BOX_W - 20, 5), bx + 15, oyy + 8, 5, ca > 0.5f ? text(235) : text(150));
                    }
                    ctx.disableScissor();
                }
            }

            if (setting instanceof BindSettings s) {
                float bx = x + rowW - BIND_BOX_W;
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.name, rowW - BIND_BOX_W - 8, 6), x + 2, rowY + 10.5f, 6, text(220));

                RenderUtil.Round.draw(ctx, bx, rowY + 2, BIND_BOX_W, BOX_H, 4, s.active ? accent(45) : control());
                if (s.active) {
                    RenderUtil.Border.draw(ctx, bx, rowY + 2, BIND_BOX_W, BOX_H, 4, 0.5f, accent());
                }

                String textS = s.active ? "..." : KeyUtil.getKey(s.get()).toUpperCase();
                textS = trim(textS, BIND_BOX_W - 8, 5);
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, textS, bx + BIND_BOX_W / 2f - FontDraw.getWidth(FontDraw.FontType.MEDIUM, textS, 5) / 2f, rowY + 10, 5, s.active ? accent() : text(220));
            }

            if (setting instanceof StringSetting s) {
                float bx = x + rowW - STRING_BOX_W;
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.name, rowW - STRING_BOX_W - 8, 6), x + 2, rowY + 10.5f, 6, text(220));

                RenderUtil.Round.draw(ctx, bx, rowY + 2, STRING_BOX_W, BOX_H, 4, s.active ? accent(45) : control());
                if (s.active) {
                    RenderUtil.Border.draw(ctx, bx, rowY + 2, STRING_BOX_W, BOX_H, 4, 0.5f, accent());
                }

                String textS = (s.get().isEmpty() && !s.active) ? "..." : s.get() + (s.active ? (System.currentTimeMillis() % 1000 >= 500 ? "" : "_") : "");
                textS = trim(textS, STRING_BOX_W - 8, 5);
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, textS, bx + 4, rowY + 10, 5, text(220));
            }

            if (setting instanceof HueSetting s) {
                float anim = drop(s);

                // Превью цвета
                float pvx = x + rowW - 20;
                FontDraw.drawText(FontDraw.FontType.MEDIUM, ctx, trim(s.name, rowW - 28, 6), x + 2, rowY + 10.5f, 6, text(220));
                RenderUtil.Round.draw(ctx, pvx, rowY + 2.5f, 18, 11, 3, fade(s.getColor()));
                RenderUtil.Border.draw(ctx, pvx, rowY + 2.5f, 18, 11, 3, 0.4f, text(70));

                // Пикер
                if (anim > 0.01f) {
                    float px = x + 2;
                    float py = rowY + ROW_H + 1;
                    float pw = rowW - 4;

                    float svX = px + 5, svY = py + 5, svW = pw - 10;
                    float hbX = px + 5, hbY = py + hueBarY(), hbW = pw - 10;

                    float size = hbW;
                    s.maximum = size;
                    float currentGUI = s.current * (size / s.originalMaximum);

                    if (s.sliding) {
                        float frac = MathHelper.clamp((float) (mouseX - hbX) / size, 0f, 1f);
                        currentGUI = (float) MathHelper.round(frac * (s.maximum - s.minimum) + s.minimum, s.increment);
                        s.current = currentGUI * (s.originalMaximum / s.maximum);
                        s.triggerAutoSave();
                    }
                    s.sliderWidth = MathHelper.interpolate(((currentGUI) - s.minimum) / (s.maximum - s.minimum) * size, s.sliderWidth, 0.15);

                    if (s.colorSliding) {
                        s.saturation = MathHelper.clamp((float) (mouseX - svX) / svW, 0f, 1f);
                        s.brightness = 1f - MathHelper.clamp((float) (mouseY - svY) / hueSVH(), 0f, 1f);
                        s.triggerAutoSave();
                    }

                    float panelH = (huePanelH() + 6) * anim;
                    ctx.enableScissor((int) px, (int) py, (int) (px + pw), (int) (py + panelH));
                    RenderUtil.Round.draw(ctx, px, py, pw, huePanelH() + 6, 5, control());

                    // SV-бокс
                    float hue = currentGUI / size;
                    Color svBase = Color.getHSBColor(hue, 1, 1);
                    RenderUtil.Round.draw(ctx, svX, svY, svW, hueSVH(), 3, svBase);
                    RenderUtil.Image.draw(ctx, Identifier.of(Strange.rootRes, "/icons/gui/c_bg.png"), svX, svY, svW, hueSVH(), 3, fade(new Color(255, 255, 255, 255).getRGB()));

                    float circleX = svX + s.saturation * svW - 3;
                    float circleY = svY + (1.0f - s.brightness) * hueSVH() - 3;
                    RenderUtil.Border.draw(ctx, circleX, circleY, 6, 6, 3, 0.5f, fade(new Color(0xFFFFFF).getRGB()));

                    // Полоса оттенка
                    RenderUtil.Image.draw(ctx, Identifier.of(Strange.rootRes, "/icons/gui/hue.png"), hbX, hbY, hbW, hueBarH(), 2, fade(new Color(255, 255, 255, 255).getRGB()));
                    RenderUtil.Border.draw(ctx, hbX - 3 + s.sliderWidth, hbY - 1.5f, 6, 8, 3, 0.5f, fade(new Color(0xFFFFFF).getRGB()));

                    ctx.disableScissor();
                }
            }

            up += settingHeight(setting);
        }
    }
}
