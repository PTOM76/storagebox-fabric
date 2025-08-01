package net.pitan76.storagebox;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.fabric.api.client.screen.ContainerScreenFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class StorageBoxScreen extends ContainerScreen<StorageBoxScreenHandler> {

    public static ContainerScreenFactory<StorageBoxScreenHandler> FACTORY = (container) 
            -> new StorageBoxScreen(container, MinecraftClient.getInstance().player.inventory, new LiteralText(""));

    public static Identifier GUI = StorageBoxMod.id("textures/item/itemselect.png");

    public StorageBoxScreen(StorageBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.containerWidth = 176;
        this.containerHeight = 166;
    }

    @Override
    protected void drawBackground(float delta, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        MinecraftClient.getInstance().getTextureManager().bindTexture(GUI);

        int i = (this.width - this.containerWidth) / 2;
        int j = (this.height - this.containerHeight) / 2;
        this.blit(i, j, 0, 0, this.containerWidth, this.containerHeight);
    }

    @Override
    protected void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);
        x = (this.width - this.containerWidth) / 2;
        y = (this.height - this.containerHeight) / 2;
        font.draw(new TranslatableText("item.storagebox.storage").asFormattedString(), 8, 20, 4210752);
        font.draw(new TranslatableText("item.storagebox.storagebox").asFormattedString(), 8, 6, 4210752);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        super.render(mouseX, mouseY, delta);
        this.drawMouseoverTooltip(mouseX, mouseY);
    }
}
