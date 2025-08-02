package net.pitan76.storagebox;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class StorageBoxScreen extends HandledScreen {

    public static Identifier GUI = StorageBoxMod.id("textures/item/itemselect.png");

    public StorageBoxScreen(StorageBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    public StorageBoxScreen(PlayerInventory inventory, Text title) {
        super(new StorageBoxScreenHandler(inventory));
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void drawBackground(float delta, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        MinecraftClient.getInstance().getTextureManager().bindTexture(GUI);

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);
        x = (this.width - this.backgroundWidth) / 2;
        y = (this.height - this.backgroundHeight) / 2;
        textRenderer.draw(new TranslatableText("item.storagebox.storage").asFormattedString(), 8, 20, 4210752);
        textRenderer.draw(new TranslatableText("item.storagebox.name").asFormattedString(), 8, 6, 4210752);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        super.render(mouseX, mouseY, delta);
        this.renderTooltip(mouseX, mouseY);
    }
}
