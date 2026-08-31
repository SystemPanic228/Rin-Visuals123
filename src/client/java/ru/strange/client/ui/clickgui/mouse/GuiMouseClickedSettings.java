package ru.strange.client.ui.clickgui.mouse;

import ru.strange.client.module.api.setting.Setting;
import ru.strange.client.module.api.setting.impl.*;
import ru.strange.client.ui.clickgui.GuiScreen;
import ru.strange.client.ui.clickgui.render.GuiRenderSettings;

public class GuiMouseClickedSettings extends GuiScreen {
    public static boolean clickedSettings(java.util.List<Setting> settings, double mouseX, double mouseY, float x, float y, float rowW, int button) {
        float up = 0;
        for (Setting setting : settings) {
            float rowY = y + up;

            if (setting instanceof BooleanSetting s) {
                if (button == 0 && isHovered(mouseX, mouseY, x, rowY, rowW, ROW_H)) {
                    s.set(!s.get());
                    return true;
                }
            }

            if (setting instanceof SliderSetting s) {
                if (button == 0 && isHovered(mouseX, mouseY, x + 2, rowY + 10, rowW - 4, 11)) {
                    s.sliding = true;
                    return true;
                }
            }

            if (setting instanceof ModeSetting s) {
                float bx = x + rowW - MODE_BOX_W;
                if (button == 0 && isHovered(mouseX, mouseY, bx, rowY + 2, MODE_BOX_W, BOX_H)) {
                    s.opened = !s.opened;
                    return true;
                }
                if (s.opened && button == 0) {
                    float oy = rowY + 2 + BOX_H + 2;
                    for (int i = 0; i < s.modes.size(); i++) {
                        if (isHovered(mouseX, mouseY, bx, oy + i * DROPDOWN_ROW_H, MODE_BOX_W, DROPDOWN_ROW_H)) {
                            s.currentMode = s.modes.get(i);
                            s.triggerAutoSave();
                            return true;
                        }
                    }
                }
            }

            if (setting instanceof MultiBooleanSetting s) {
                float bx = x + rowW - MODE_BOX_W;
                if (button == 0 && isHovered(mouseX, mouseY, bx, rowY + 2, MODE_BOX_W, BOX_H)) {
                    s.opened = !s.opened;
                    return true;
                }
                if (s.opened && button == 0) {
                    float oy = rowY + 2 + BOX_H + 2;
                    for (int i = 0; i < s.settings.size(); i++) {
                        if (isHovered(mouseX, mouseY, bx, oy + i * DROPDOWN_ROW_H, MODE_BOX_W, DROPDOWN_ROW_H)) {
                            s.settings.get(i).set(!s.settings.get(i).get());
                            return true;
                        }
                    }
                }
            }

            if (setting instanceof BindSettings s) {
                float bx = x + rowW - BIND_BOX_W;
                if (isHovered(mouseX, mouseY, bx, rowY + 2, BIND_BOX_W, BOX_H)) {
                    s.active = !s.active;
                    return true;
                }
            }

            if (setting instanceof StringSetting s) {
                float bx = x + rowW - STRING_BOX_W;
                if (isHovered(mouseX, mouseY, bx, rowY + 2, STRING_BOX_W, BOX_H)) {
                    s.active = true;
                    return true;
                } else {
                    s.active = false;
                }
            }

            if (setting instanceof HueSetting s) {
                float pvx = x + rowW - 20;
                if (button == 0 && isHovered(mouseX, mouseY, pvx, rowY + 2.5f, 18, 11)) {
                    s.opened = !s.opened;
                    return true;
                }
                if (s.opened && button == 0) {
                    float px = x + 2;
                    float py = rowY + ROW_H + 1;
                    float pw = rowW - 4;
                    float svX = px + 5, svY = py + 5, svW = pw - 10;
                    float hbX = px + 5, hbY = py + GuiRenderSettings.hueBarY(), hbW = pw - 10;

                    if (isHovered(mouseX, mouseY, svX, svY, svW, GuiRenderSettings.hueSVH())) {
                        s.colorSliding = true;
                        return true;
                    }
                    if (isHovered(mouseX, mouseY, hbX, hbY - 1.5f, hbW, GuiRenderSettings.hueBarH() + 3)) {
                        s.sliding = true;
                        return true;
                    }
                }
            }

            up += settingHeight(setting);
        }
        return false;
    }
}
