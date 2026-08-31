package ru.strange.client.ui.clickgui.mouse;

import ru.strange.client.module.Theme;
import ru.strange.client.module.ThemeManager;
import ru.strange.client.module.api.Category;
import ru.strange.client.ui.clickgui.GuiScreen;
import ru.strange.client.ui.clickgui.render.GuiRenderTheme;

public class GuiMouseClickedTheme extends GuiScreen {
    public static boolean clickedTheme(double mouseX, double mouseY) {
        if (selectedCategories != Category.Theme) return false;

        for (int index = 0; index < themes.length; index++) {
            Theme theme = themes[index];
            float drawX = GuiRenderTheme.themeX(index);
            float drawY = GuiRenderTheme.themeY(index);

            if (isHovered(mouseX, mouseY, drawX, drawY, GuiRenderTheme.themeCardW(), GuiRenderTheme.THEME_CARD_H)) {
                ThemeManager.setTheme(theme);
                return true;
            }
        }
        return false;
    }
}
