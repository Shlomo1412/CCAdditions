package net.shlomo1412.cc_additions.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.shlomo1412.cc_additions.menu.PlayerConnectorMenu;
import net.shlomo1412.cc_additions.network.ModNetwork;
import net.shlomo1412.cc_additions.network.PlayerConnectorPacket;

/**
 * GUI screen for the Player Connector.
 * Shows pairing status and provides pair/unpair buttons.
 */
public class PlayerConnectorScreen extends AbstractContainerScreen<PlayerConnectorMenu> {

    // Use vanilla GUI texture
    private static final ResourceLocation BACKGROUND = new ResourceLocation("minecraft", 
        "textures/gui/demo_background.png");

    private Button pairButton;
    private Button unpairButton;

    public PlayerConnectorScreen(PlayerConnectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 248;  // Demo background size
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // Hide labels
        this.titleLabelY = 6;
        this.inventoryLabelY = 10000; // Hide

        int centerX = this.leftPos + this.imageWidth / 2;
        int buttonY = this.topPos + 110;

        // Pair button
        this.pairButton = Button.builder(Component.translatable("gui.cc_additions.player_connector.pair"), 
            button -> {
                ModNetwork.sendToServer(new PlayerConnectorPacket(menu.getBlockPos(), true));
                this.onClose();
            })
            .bounds(centerX - 50, buttonY, 100, 20)
            .build();

        // Unpair button
        this.unpairButton = Button.builder(Component.translatable("gui.cc_additions.player_connector.unpair"), 
            button -> {
                ModNetwork.sendToServer(new PlayerConnectorPacket(menu.getBlockPos(), false));
                this.onClose();
            })
            .bounds(centerX - 50, buttonY, 100, 20)
            .build();

        this.addRenderableWidget(this.pairButton);
        this.addRenderableWidget(this.unpairButton);

        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        boolean isPaired = menu.isPaired();
        boolean isPairedToMe = menu.isPairedTo(this.minecraft.player);

        // Show pair button if not paired
        // Show unpair button if paired to the current player
        this.pairButton.visible = !isPaired;
        this.unpairButton.visible = isPaired && isPairedToMe;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtonVisibility();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Draw status text
        int centerX = this.leftPos + this.imageWidth / 2;
        int textY = this.topPos + 50;

        if (menu.isPaired()) {
            String playerName = menu.getPairedPlayerName();
            boolean online = menu.isPairedPlayerOnline();
            
            Component pairedText = Component.translatable("gui.cc_additions.player_connector.paired_to", playerName);
            guiGraphics.drawCenteredString(this.font, pairedText, centerX, textY, 0xFFFFFF);
            
            Component statusText;
            int statusColor;
            if (online) {
                statusText = Component.translatable("gui.cc_additions.player_connector.online");
                statusColor = 0x55FF55; // Green
            } else {
                statusText = Component.translatable("gui.cc_additions.player_connector.offline");
                statusColor = 0xFF5555; // Red
            }
            guiGraphics.drawCenteredString(this.font, statusText, centerX, textY + 15, statusColor);

            // If not paired to current player, show message
            if (!menu.isPairedTo(this.minecraft.player)) {
                Component notYoursText = Component.translatable("gui.cc_additions.player_connector.not_yours");
                guiGraphics.drawCenteredString(this.font, notYoursText, centerX, textY + 45, 0xFFAA00);
            }
        } else {
            Component notPairedText = Component.translatable("gui.cc_additions.player_connector.not_paired");
            guiGraphics.drawCenteredString(this.font, notPairedText, centerX, textY, 0xAAAAAA);
            
            Component instructionText = Component.translatable("gui.cc_additions.player_connector.click_pair");
            guiGraphics.drawCenteredString(this.font, instructionText, centerX, textY + 15, 0x888888);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(this.font, this.title, this.imageWidth / 2, this.titleLabelY, 0x404040);
    }
}
