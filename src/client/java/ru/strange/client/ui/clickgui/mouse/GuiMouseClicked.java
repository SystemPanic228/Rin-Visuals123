package ru.strange.client.ui.clickgui.mouse;

import ru.strange.client.ui.clickgui.GuiScreen;

public class GuiMouseClicked extends GuiScreen {
    public static boolean mouseClickedGui(double pMouseX, double pMouseY, int pButton) {
        if (GuiMouseClickedCategory.clickedCategory(pMouseX, pMouseY)) {
            return true;
        }
        // Пока окно закрывается или старый контент доигрывает исчезновение —
        // клики по контенту игнорируем, иначе они попадают по ещё не доехавшим карточкам.
        if (isClosing() || isSwitchLeaving()) {
            return false;
        }
        if (GuiMouseClickedModule.clickedModule(pMouseX, pMouseY, pButton)) {
            return true;
        }
        if (GuiMouseClickedTheme.clickedTheme(pMouseX, pMouseY)) {
            return true;
        }

        return false;
    }
}
