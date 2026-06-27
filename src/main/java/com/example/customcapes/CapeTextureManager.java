package com.example.customcapes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapeTextureManager {
    private static final Map<String, ResourceLocation> textures = new HashMap<String, ResourceLocation>();
    private static final List<ResourceLocation> loadedResources = new ArrayList<ResourceLocation>();
    private static ResourceLocation defaultCape = null;

    private static Method deleteTextureMethod = null;
    private static boolean methodSearched = false;

    private static void ensureMethod() {
        if (methodSearched) return;
        methodSearched = true;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null) {
                Class<?> textureManagerClass = mc.getTextureManager().getClass();
                try {
                    deleteTextureMethod = textureManagerClass.getMethod("deleteTexture", ResourceLocation.class);
                } catch (NoSuchMethodException e) {
                    deleteTextureMethod = textureManagerClass.getMethod("func_110550_d", ResourceLocation.class);
                }
            }
        } catch (Exception e) {
            FMLLog.log(Level.WARN, "[CustomCapes] Failed to find deleteTexture method.");
        }
    }

    public static void loadAllTextures(File folder) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            FMLLog.log(Level.WARN, "[CustomCapes] Skipping texture loading - Minecraft instance is null.");
            return;
        }

        File[] pngFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        });

        if (pngFiles == null || pngFiles.length == 0) {
            FMLLog.log(Level.INFO, "[CustomCapes] No PNG files found in %s", folder.getAbsolutePath());
            return;
        }

        FMLLog.log(Level.INFO, "[CustomCapes] Found %d PNG file(s) in %s", pngFiles.length, folder.getAbsolutePath());
        for (int i = 0; i < pngFiles.length; i++) {
            File file = pngFiles[i];
            String name = file.getName().replaceAll("(?i)\\.png$", "");
            try {
                BufferedImage image = ImageIO.read(file);
                DynamicTexture dynTex = new DynamicTexture(image);
                ResourceLocation res = new ResourceLocation(CustomCapesMod.MODID, "capes/" + name);
                mc.getTextureManager().loadTexture(res, dynTex);
                textures.put(name, res);
                loadedResources.add(res);

                if (name.equalsIgnoreCase("default")) {
                    defaultCape = res;
                }
                FMLLog.log(Level.INFO, "[CustomCapes] Loaded: %s from %s", name, file.getName());
            } catch (Exception e) {
                FMLLog.log(Level.ERROR, "[CustomCapes] Failed to load %s: %s", file.getName(), e.getMessage());
            }
        }
        FMLLog.log(Level.INFO, "[CustomCapes] Total textures loaded: %d (default: %s)",
                textures.size(), defaultCape != null ? "yes" : "no");
    }

    public static void clearAll() {
        ensureMethod();
        if (deleteTextureMethod != null) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null) {
                for (int i = 0; i < loadedResources.size(); i++) {
                    try {
                        deleteTextureMethod.invoke(mc.getTextureManager(), loadedResources.get(i));
                    } catch (Exception e) {
                        FMLLog.log(Level.ERROR, "[CustomCapes] Failed to delete texture: %s", loadedResources.get(i));
                    }
                }
            }
        }
        loadedResources.clear();
        textures.clear();
        defaultCape = null;
    }

    public static void reload(File folder) {
        FMLLog.log(Level.INFO, "[CustomCapes] Reloading from %s", folder.getAbsolutePath());
        clearAll();
        loadAllTextures(folder);
    }

    public static ResourceLocation getCape(AbstractClientPlayer player) {
        ResourceLocation cape = textures.get(player.getUniqueID().toString());
        if (cape != null) return cape;
        cape = textures.get(player.getName());
        if (cape != null) return cape;
        return defaultCape;
    }

    public static String getStatus() {
        return String.format("Textures: %d (default: %s)",
                textures.size(), defaultCape != null ? "yes" : "no");
    }
}