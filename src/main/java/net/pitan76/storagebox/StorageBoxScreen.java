package net.pitan76.storagebox;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class StorageBoxScreen extends AbstractContainerScreen<StorageBoxScreenHandler> {

    public static Identifier GUI = StorageBoxMod.id("textures/item/itemselect.png");

    public StorageBoxScreen(StorageBoxScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, GUI, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);
        titleLabelX = (this.width - this.imageWidth) / 2;
        titleLabelY = (this.height - this.imageHeight) / 2;
        context.text(font, Component.translatable("item.storagebox.storage"), 8, 20, -12566464, false);
        context.text(font, Component.translatable("item.storagebox.storagebox"), 8, 6, -12566464, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.extractTooltip(context, mouseX, mouseY);
    }
}
