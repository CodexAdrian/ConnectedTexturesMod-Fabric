package team.chisel.ctm.client.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

public class CTMConfigScreen extends Screen {
    private final Screen parent;
    private final ConfigManager configManager;

    public CTMConfigScreen(Screen parent, ConfigManager configManager) {
        super(Text.translatable("screen.ctm.config.title"));
        this.parent = parent;
        this.configManager = configManager;
    }

    @Override
    protected void init() {
        addDrawableChild(new ButtonWidget(width / 2 - 90 - 75, height / 2 - 10, 150, 20, getBooleanOptionText("options.ctm.disable_ctm", configManager.getConfig().disableCTM),
                (button) -> {
                    boolean value = !configManager.getConfig().disableCTM;
                    button.setMessage(getBooleanOptionText("options.ctm.disable_ctm", value));
                    configManager.getConfig().disableCTM = value;
                },
                (button, matrices, mouseX, mouseY) -> {
                    renderWrappedTooltip(matrices, Text.translatable("options.ctm.disable_ctm.tooltip"), mouseX, mouseY);
                }
        ));

        addDrawableChild(new ButtonWidget(width / 2 + 90 - 75, height / 2 - 10, 150, 20, getBooleanOptionText("options.ctm.connect_inside_ctm", configManager.getConfig().connectInsideCTM),
                (button) -> {
                    boolean value = !configManager.getConfig().connectInsideCTM;
                    button.setMessage(getBooleanOptionText("options.ctm.connect_inside_ctm", value));
                    configManager.getConfig().connectInsideCTM = value;
                },
                (button, matrices, mouseX, mouseY) -> renderWrappedTooltip(matrices, Text.translatable("options.ctm.connect_inside_ctm.tooltip"), mouseX, mouseY)
        ));

        addDrawableChild(new ButtonWidget(width / 2 - 100, (int) (height * 0.8F), 200, 20, ScreenTexts.DONE, (button) -> close()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, (int) (height * 0.15F), 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void removed() {
        configManager.onConfigChange();
    }

    private void renderWrappedTooltip(MatrixStack matrices, StringVisitable text, int x, int y) {
        renderOrderedTooltip(matrices, textRenderer.wrapLines(text, width - x - 4), x, y);
    }

    private static Text getBooleanOptionText(String key, boolean value) {
        return Text.translatable(key, ScreenTexts.onOrOff(value));
    }
}
