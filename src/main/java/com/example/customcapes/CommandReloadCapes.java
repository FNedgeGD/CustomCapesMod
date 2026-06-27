package com.example.customcapes;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CommandReloadCapes extends CommandBase {

	@Override
	public String getCommandName() {
		return "reloadcapes";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/reloadcapes - 重新加载披风纹理";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (!CustomCapesMod.enabled) {
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[CustomCapes] 模组已禁用。"));
			return;
		}

		CapeTextureManager.reload(CustomCapesMod.capeFolder);
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[CustomCapes] 披风已重新加载。"));
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
}