package com.example.customcapes;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CommandCapesInfo extends CommandBase {

	@Override
	public String getCommandName() {
		return "capesinfo";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/capesinfo - 显示披风模组诊断信息";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "=== CustomCapes 诊断信息 ==="));
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.WHITE + "模组状态: " + (CustomCapesMod.enabled ? "§a启用" : "§c禁用")));
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.WHITE + "文件夹: " + CustomCapesMod.capeFolder.getAbsolutePath()));
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.WHITE + CapeTextureManager.getStatus()));
		// 以下两行改用青色
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "PlayerInfo字段: " + CapeRenderHandler.playerInfoFieldName));
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "LocationCape字段: " + CapeRenderHandler.locationCapeFieldName));

		if (CapeRenderHandler.playerInfoFieldName.equals("unknown") || CapeRenderHandler.locationCapeFieldName.equals("unknown")) {
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "警告：未找到必要字段，披风可能无法工作！"));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
}