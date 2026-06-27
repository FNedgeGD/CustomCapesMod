package com.example.customcapes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class CapeRenderHandler {
	public static String playerInfoFieldName = "unknown";
	public static String locationCapeFieldName = "unknown";

	private static final Field PLAYER_INFO_FIELD;
	private static final Field LOCATION_CAPE_FIELD;

	private final Map<AbstractClientPlayer, ResourceLocation> originalCapes =
			new WeakHashMap<AbstractClientPlayer, ResourceLocation>();

	static {
		PLAYER_INFO_FIELD = findPlayerInfoField();
		if (PLAYER_INFO_FIELD != null) {
			PLAYER_INFO_FIELD.setAccessible(true);
			playerInfoFieldName = PLAYER_INFO_FIELD.getName();
			FMLLog.log(Level.INFO, "[CustomCapes] Found playerInfo field: %s", playerInfoFieldName);
		} else {
			FMLLog.log(Level.ERROR, "[CustomCapes] CRITICAL: playerInfo field not found!");
		}

		LOCATION_CAPE_FIELD = findCapeFieldWithFallback();
		if (LOCATION_CAPE_FIELD != null) {
			LOCATION_CAPE_FIELD.setAccessible(true);
			locationCapeFieldName = LOCATION_CAPE_FIELD.getName();
			FMLLog.log(Level.INFO, "[CustomCapes] Found locationCape field: %s (type: %s)",
					locationCapeFieldName, LOCATION_CAPE_FIELD.getType().getSimpleName());
		} else {
			FMLLog.log(Level.ERROR, "[CustomCapes] CRITICAL: locationCape field not found!");
		}

		if (PLAYER_INFO_FIELD == null || LOCATION_CAPE_FIELD == null) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					if (Minecraft.getMinecraft().thePlayer != null) {
						Minecraft.getMinecraft().thePlayer.addChatMessage(
								new ChatComponentText(EnumChatFormatting.RED
										+ "[CustomCapes] 字段未找到！模组无法工作。"));
					}
				}
			});
		}
	}

	private static Field findPlayerInfoField() {
		String[] hardcoded = new String[]{"field_175157_a", "playerInfo"};
		for (int i = 0; i < hardcoded.length; i++) {
			try {
				Field f = AbstractClientPlayer.class.getDeclaredField(hardcoded[i]);
				if (f.getType() == NetworkPlayerInfo.class) return f;
			} catch (NoSuchFieldException ignored) {}
		}
		Field[] fields = AbstractClientPlayer.class.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getType() == NetworkPlayerInfo.class) return fields[i];
		}
		return null;
	}

	private static Field findCapeFieldWithFallback() {
		// ---- 硬编码优先（正确字段：field_178862_f） ----
		String primaryName = "field_178862_f";
		try {
			Field f = NetworkPlayerInfo.class.getDeclaredField(primaryName);
			if (f.getType() == ResourceLocation.class) {
				FMLLog.log(Level.INFO, "[CustomCapes] Found cape field by hardcoded name: %s", primaryName);
				return f;
			}
		} catch (NoSuchFieldException e) {
			FMLLog.log(Level.INFO, "[CustomCapes] Hardcoded field %s not found, falling back to dynamic search.", primaryName);
		}

		// ---- 动态兜底 ----
		// 尝试其他已知正确名称
		String[] knownNames = new String[]{"locationCape", "field_178867_d", "capeLocation"};
		for (int i = 0; i < knownNames.length; i++) {
			try {
				Field f = NetworkPlayerInfo.class.getDeclaredField(knownNames[i]);
				if (f.getType() == ResourceLocation.class) {
					FMLLog.log(Level.INFO, "[CustomCapes] Found cape field by alternate name: %s", knownNames[i]);
					return f;
				}
			} catch (NoSuchFieldException ignored) {}
		}

		// 收集所有 ResourceLocation 字段并智能选择
		Field[] fields = NetworkPlayerInfo.class.getDeclaredFields();
		List<Field> resourceFields = new ArrayList<Field>();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getType() == ResourceLocation.class) {
				resourceFields.add(fields[i]);
				FMLLog.log(Level.DEBUG, "[CustomCapes] ResourceLocation field #%d: %s", resourceFields.size() - 1, fields[i].getName());
			}
		}

		if (resourceFields.size() == 0) {
			FMLLog.log(Level.ERROR, "[CustomCapes] No ResourceLocation fields found in NetworkPlayerInfo!");
			return null;
		}

		if (resourceFields.size() == 1) {
			FMLLog.log(Level.WARN, "[CustomCapes] Only one ResourceLocation field (%s). Cannot safely determine cape field.", resourceFields.get(0).getName());
			return null;
		}

		// 优先选择名称含 "cape" 且不含 "skin" 的
		Field capeField = null;
		for (int i = 0; i < resourceFields.size(); i++) {
			Field f = resourceFields.get(i);
			String name = f.getName().toLowerCase();
			if (name.contains("cape") && !name.contains("skin")) {
				capeField = f;
				FMLLog.log(Level.INFO, "[CustomCapes] Selected cape field by name: %s", f.getName());
				break;
			}
		}

		// 如果名称规则找不到，则取第二个字段（通常第一个是 skin）
		if (capeField == null) {
			capeField = resourceFields.get(1);
			FMLLog.log(Level.INFO, "[CustomCapes] Using second ResourceLocation field as cape: %s (first is %s)",
					capeField.getName(), resourceFields.get(0).getName());
		}

		return capeField;
	}

	@SubscribeEvent
	public void onPreRenderPlayer(RenderPlayerEvent.Pre event) {
		AbstractClientPlayer player = (AbstractClientPlayer) event.entityPlayer;

		// 只修改本地玩家自己的披风
		if (player != Minecraft.getMinecraft().thePlayer) {
			return;
		}

		ResourceLocation customCape = CapeTextureManager.getCape(player);
		if (customCape == null) return;
		if (PLAYER_INFO_FIELD == null || LOCATION_CAPE_FIELD == null) return;

		try {
			NetworkPlayerInfo info = (NetworkPlayerInfo) PLAYER_INFO_FIELD.get(player);
			if (info == null) return;

			ResourceLocation original = (ResourceLocation) LOCATION_CAPE_FIELD.get(info);
			originalCapes.put(player, original);
			LOCATION_CAPE_FIELD.set(info, customCape);
		} catch (Exception e) {
			FMLLog.log(Level.ERROR, "[CustomCapes] Failed to set cape for %s: %s", player.getName(), e.getMessage());
		}
	}

	@SubscribeEvent
	public void onPostRenderPlayer(RenderPlayerEvent.Post event) {
		AbstractClientPlayer player = (AbstractClientPlayer) event.entityPlayer;

		if (player != Minecraft.getMinecraft().thePlayer) {
			return;
		}

		if (!originalCapes.containsKey(player)) return;

		try {
			NetworkPlayerInfo info = (NetworkPlayerInfo) PLAYER_INFO_FIELD.get(player);
			if (info != null) {
				LOCATION_CAPE_FIELD.set(info, originalCapes.get(player));
			}
		} catch (Exception e) {
			FMLLog.log(Level.ERROR, "[CustomCapes] Failed to restore cape for %s", player.getName());
		} finally {
			originalCapes.remove(player);
		}
	}
}