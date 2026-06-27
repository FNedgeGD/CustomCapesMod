package com.example.customcapes;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import java.awt.Desktop;
import java.io.File;

public class GuiOpenFolder extends GuiScreen {

	private final GuiScreen parentScreen;

	// 必须提供 (GuiScreen) 构造函数，供 Forge 使用
	public GuiOpenFolder(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	@Override
	public void initGui() {
		File folder = CustomCapesMod.capeFolder;
		boolean success = false;
		String statusMessage;

		if (folder != null && folder.exists()) {
			try {
				Desktop.getDesktop().open(folder);
				success = true;
				statusMessage = "已打开文件夹: " + folder.getAbsolutePath();
				FMLLog.log(Level.INFO, "[CustomCapes] Opened folder: %s", folder.getAbsolutePath());
			} catch (Exception e) {
				statusMessage = "无法自动打开文件夹，请手动前往：" + folder.getAbsolutePath();
				FMLLog.log(Level.ERROR, "[CustomCapes] Failed to open folder: %s", e.getMessage());
			}
		} else {
			statusMessage = "文件夹不存在！路径：" + (folder != null ? folder.getAbsolutePath() : "null");
			FMLLog.log(Level.WARN, "[CustomCapes] Folder does not exist.");
		}

		// 如果玩家在线，发送聊天消息
		if (this.mc.thePlayer != null) {
			EnumChatFormatting color = success ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
			this.mc.thePlayer.addChatMessage(new ChatComponentText(color + "[CustomCapes] " + statusMessage));
		}

		// 返回前一个界面（Mods 列表）
		this.mc.displayGuiScreen(this.parentScreen);
	}
}