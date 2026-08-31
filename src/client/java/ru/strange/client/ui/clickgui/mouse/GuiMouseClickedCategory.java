package ru.strange.client.ui.clickgui.mouse;

import ru.strange.client.Strange;
import ru.strange.client.module.api.Category;
import ru.strange.client.ui.clickgui.GuiScreen;

public class GuiMouseClickedCategory extends GuiScreen {
    public static boolean clickedCategory(double mouseX, double mouseY) {
        int i = 0;
        for (Category category : categories) {
            float rowY = categoryY(i);
            if (isHovered(mouseX, mouseY, x + 6, rowY, SIDEBAR_W - 12, CAT_ROW_H)) {
                if (selectedCategories != category) {
                    Category from = selectedCategories;
                    selectedCategories = category;
                    GuiScreen.modules = Strange.get.manager.getType(GuiScreen.selectedCategories);
                    beginCategorySwitch(from);
                }
                return true;
            }
            i++;
        }
        return false;
    }
}
