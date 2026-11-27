package com.example.hitboxexpander;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HitboxExpanderClient implements ClientModInitializer {
    private static boolean enabled = true;
    private static double expandAmount = 0.5;
    private static final Map<UUID, Box> originalBoxes = new HashMap<>();
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hitboxexpander.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.hitboxexpander"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed()) {
                enabled = !enabled;
                client.player.sendMessage(Text.literal("Hitbox: " + (enabled ? "ON" : "OFF")), true);
                restoreAllBoxes();
            }
            if (enabled) applyHitboxes(client);
        });
    }

    private static void applyHitboxes(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player || !(entity instanceof LivingEntity)) continue;
            if (!originalBoxes.containsKey(entity.getUuid())) {
                originalBoxes.put(entity.getUuid(), entity.getBoundingBox());
            }
            Box orig = originalBoxes.get(entity.getUuid());
            entity.setBoundingBox(new Box(
                orig.minX - expandAmount, orig.minY - expandAmount, orig.minZ - expandAmount,
                orig.maxX + expandAmount, orig.maxY + expandAmount, orig.maxZ + expandAmount
            ));
        }
    }

    private static void restoreAllBoxes() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) { originalBoxes.clear(); return; }
        for (Entity entity : client.world.getEntities()) {
            if (originalBoxes.containsKey(entity.getUuid())) {
                entity.setBoundingBox(originalBoxes.get(entity.getUuid()));
            }
        }
        originalBoxes.clear();
    }
}
