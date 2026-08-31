package ru.strange.client.ui.clickgui.mouse;

import ru.strange.client.module.api.Category;
import ru.strange.client.module.api.Module;
import ru.strange.client.ui.clickgui.GuiScreen;

public class GuiMouseClickedModule extends GuiScreen {
    public static boolean clickedModule(double mouseX, double mouseY, int button) {
        if (selectedCategories == Category.Theme || modules == null) return false;

        float yDown = 0;
        float scrollY = scroll.getScroll();
        float cardW = cardW();

        for (Module module : modules) {
            float cardH = moduleHeight(module);
            float drawY = modulesY() + yDown + scrollY;

            if (isHovered(mouseX, mouseY, contentX(), drawY, cardW, MODULE_H)) {
                if (button == 0) {
                    module.toggle();
                    return true;
                }

                if (button == 1) {
                    if (!module.getSettingsForGUI().isEmpty()) {
                        module.open = !module.open;
                    }
                    return true;
                }

                if (button == 2) {
                    module.binding = true;
                    module.displayName = "Нажмите кнопку";
                    return true;
                }
            }

            if (!module.getSettingsForGUI().isEmpty() && open(module) > 0.5f) {
                if (GuiMouseClickedSettings.clickedSettings(visibleSettings(module), mouseX, mouseY, contentX() + 6, drawY + MODULE_H + 2, cardW - 12, button)) {
                    return true;
                }
            }

            yDown += cardH + MODULE_GAP;
        }
        return false;
    }
}
