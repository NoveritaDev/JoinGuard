package me.noverita.joinguard;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener, CommandExecutor {
    private final List<String> records = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::writeToFile, 0, 288000);
        getCommand("joinguard").setExecutor(this);
    }

    @Override
    public void onDisable() {
        writeToFile();
    }

    private void writeToFile() {
        if (records.size() > 0) {
            File output = new File(getDataFolder() + "/" + System.currentTimeMillis() + ".log");
            getDataFolder().mkdirs();
            try (PrintWriter pw = new PrintWriter(output)) {
                for (String record : records) {
                    pw.print(record);
                }
                records.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 84));
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            PotionEffect effect = player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            if (effect != null && effect.getAmplifier() == 84) {
                double damage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
                records.add(String.format("%s,%s,%.1f,%s,%s,%s",
                        player.getUniqueId(),
                        player.getName(),
                        damage,
                        Clock.systemUTC().instant().toString(),
                        player.getLocation(),
                        event.getCause()
                ));
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String record: records) {
            sb.append(record);
            sb.append('\n');
        }
        sender.sendMessage(sb.toString());
        return true;
    }
}
