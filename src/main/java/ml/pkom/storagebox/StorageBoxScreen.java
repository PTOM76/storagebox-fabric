package ml.pkom.storagebox;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class StorageBoxScreen extends HandledScreen<StorageBoxScreenHandler> {

    public static Identifier GUI = StorageBoxMod.id("textures/item/itemselect.png");

    public StorageBoxScreen(StorageBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);

        //GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        //MinecraftClient.getInstance().getTextureManager().bindTexture(GUI);
        drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        x = (this.width - this.backgroundWidth) / 2;
        y = (this.height - this.backgroundHeight) / 2;
        this.textRenderer.draw(matrices, new TranslatableText("item.storagebox.storage"), 8, 20, 4210752);
        this.textRenderer.draw(matrices, new TranslatableText("item.storagebox.storagebox"), 8, 6, 4210752);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
