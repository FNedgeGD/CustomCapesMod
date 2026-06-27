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
        return "/reloadcapes - 重载披风并显示诊断信息";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!CustomCapesMod.enabled) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[CustomCapes] 模组已禁用。"));
            return;
        }

        CapeTextureManager.reload(CustomCapesMod.capeFolder);
        String status = CapeTextureManager.getStatus();
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[CustomCapes] 重载完成！"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.WHITE + status));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.WHITE + "文件夹: " + CustomCapesMod.capeFolder.getAbsolutePath()));

        // 关键诊断信息
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "PlayerInfo字段: " + CapeRenderHandler.playerInfoFieldName));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "LocationCape字段: " + CapeRenderHandler.locationCapeFieldName));

        // 如果字段为 "unknown"，说明未找到，提示错误
        if (CapeRenderHandler.playerInfoFieldName.equals("unknown") || CapeRenderHandler.locationCapeFieldName.equals("unknown")) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "错误：未找到必要字段！披风无法工作。请查看fml-client-latest.log。"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}