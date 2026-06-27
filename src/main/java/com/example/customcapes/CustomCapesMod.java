package com.example.customcapes;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.Properties;

@Mod(modid = CustomCapesMod.MODID, version = CustomCapesMod.VERSION, clientSideOnly = true)
public class CustomCapesMod {
    public static final String MODID = "customcapes";
    public static final String VERSION = "1.0.0";

    public static File capeFolder;
    public static boolean enabled = true;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() != Side.CLIENT) return;

        capeFolder = new File(Minecraft.getMinecraft().mcDataDir, "customcapes");
        if (!capeFolder.exists()) {
            capeFolder.mkdirs();
            FMLLog.log(Level.INFO, "[CustomCapes] Created folder at %s", capeFolder.getAbsolutePath());
        }

        File configFile = new File(capeFolder, "capes.properties");
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        boolean configEnabled = true;
        if (configFile.exists()) {
            configEnabled = readConfigEnabled(configFile);
        }

        boolean hasImages = hasPngFiles(capeFolder);
        enabled = configEnabled && hasImages;

        FMLLog.log(Level.INFO, "[CustomCapes] Enabled: %b (config: %b, images: %b)", enabled, configEnabled, hasImages);

        if (enabled) {
            CapeTextureManager.loadAllTextures(capeFolder);
            MinecraftForge.EVENT_BUS.register(new CapeRenderHandler());
            ClientCommandHandler.instance.registerCommand(new CommandReloadCapes());
        } else {
            if (!configEnabled) {
                FMLLog.log(Level.WARN, "[CustomCapes] Disabled by config.");
            } else if (!hasImages) {
                FMLLog.log(Level.WARN, "[CustomCapes] No PNG images in %s, disabled.", capeFolder.getAbsolutePath());
            }
        }
    }

    private void createDefaultConfig(File configFile) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8"));
            pw.println("enabled=true");
            FMLLog.log(Level.INFO, "[CustomCapes] Created default config at %s", configFile.getAbsolutePath());
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, "[CustomCapes] Failed to create config: %s", e.getMessage());
        } finally {
            if (pw != null) pw.close();
        }
    }

    private boolean readConfigEnabled(File configFile) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(configFile);
            Properties props = new Properties();
            props.load(fis);
            return Boolean.parseBoolean(props.getProperty("enabled", "true"));
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, "[CustomCapes] Failed to read config: %s", e.getMessage());
            return true;
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (IOException ignored) {}
            }
        }
    }

    private boolean hasPngFiles(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return false;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".png")) {
                return true;
            }
        }
        return false;
    }
}