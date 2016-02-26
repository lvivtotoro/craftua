package org.midnightas.craftukr;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class CraftUkr extends JavaPlugin implements Listener {

	public static class FactionsFunction implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			TribesFunction.sendMessageWithHeader(sender,
					new String[] { "Тут Фракції не підтримуються.", "Замість Фракції є Плем'ї:", "/t або /tribes" });
			return true;
		}
	}

	public static class TribesFunction implements CommandExecutor {
		public static void sendMessageWithHeader(CommandSender sender, String string) {
			sender.sendMessage(new String[] { _c + "===" + _7 + " Плем'я " + _c + "===", string });
		}

		public static void sendMessageWithHeader(CommandSender sender, String[] string) {
			List<String> lines = new LinkedList<String>(Arrays.asList(string));
			lines.add(0, _c + "===" + _7 + " Плем'я " + _c + "===");
			sender.sendMessage(lines.toArray(new String[lines.size()]));
		}

		public static Tribe getTribeFromPlayer(UUID id) {
			for (Tribe tribe : instance.TRIBES) {
				if (tribe.members.contains(id))
					return tribe;
			}
			return null;
		}

		public static Tribe getTribeFromClaimedChunk(Chunk c) {
			for (Tribe tribe : instance.TRIBES) {
				if (tribe.chunks.contains(c))
					return tribe;
			}
			return null;
		}

		public static Tribe getTribeFromClaimedChunk(int x, int z, World w) {
			for (Tribe tribe : instance.TRIBES) {
				if (tribe.chunks.contains(w.getChunkAt(x, z)))
					return tribe;
			}
			return null;
		}

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if (!(sender instanceof Player)) {
				return true;
			}
			if (args[0].equalsIgnoreCase("join")) {
				int id;
				try {
					id = Integer.parseInt(args[1]);
				} catch (NumberFormatException exception) {
					if (args[1].toLowerCase().startsWith("e") || args[1].toLowerCase().startsWith("з")) {
						id = 0;
					} else if (args[1].toLowerCase().startsWith("f") || args[1].toLowerCase().startsWith("в")) {
						id = 1;
					} else if (args[1].toLowerCase().startsWith("a") || args[1].toLowerCase().startsWith("п")) {
						id = 2;
					} else {
						sendMessageWithHeader(sender, new String[] { _c + "Є тільки 3 племена:", _e + "1 - Земля",
								_e + "2 - Вогонь", _e + "3 - Повітря", });
						return true;
					}
				}
				Bukkit.broadcastMessage(id + "");
				if (id <= 3 && id > 0) {
					if (!instance.TRIBES[id - 1].members.contains(((Player) sender).getUniqueId())) {
						instance.TRIBES[id - 1].members.add(((Player) sender).getUniqueId());
						sendMessageWithHeader(sender,
								new String[] { _c + "Вітаємо вас до племена:", _b + instance.TRIBES[id - 1].name });
						if (!instance.config0.contains(((Player) sender).getUniqueId() + "")
								|| !instance.config0.getStringList(((Player) sender).getUniqueId() + "")
										.contains(instance.TRIBES[id - 1].id)) {
							List<String> firstTimePlayers = instance.config0
									.getStringList(((Player) sender).getUniqueId() + "");
							firstTimePlayers.add(instance.TRIBES[id - 1].id + "");
							instance.config0.set(((Player) sender).getUniqueId() + "", firstTimePlayers);
							for (String s : instance.config1.getStringList(instance.TRIBES[id - 1].id + ".kit"))
								((Player) sender).getInventory().addItem(deserializeItem(s));
						}
					} else {
						sendMessageWithHeader(sender, new String[] { _c + "Ви вже є у цьому племені." });
					}
				} else {
					sendMessageWithHeader(sender, "Є тільки 3 племена!");
				}
			} else if (args[0].equalsIgnoreCase("spawn")) {
				if (getTribeFromPlayer(((Player) sender).getUniqueId()) != null) {
					((Player) sender).teleport(deserialize(instance.config1.getString(
							getTribeFromPlayer(((Player) sender).getUniqueId()).id + ".spawn", "0|0|0|world")));
				} else {
					((Player) sender).teleport(deserialize(instance.config1.getString("spawn")));
				}
			}
			return true;
		}
	}

	public static final ChatColor _c = ChatColor.RED;
	public static final ChatColor _l = ChatColor.BOLD;
	public static final ChatColor _e = ChatColor.YELLOW;
	public static final ChatColor _7 = ChatColor.GRAY;
	public static final ChatColor _b = ChatColor.AQUA;

	public File config0f;
	public FileConfiguration config0;
	public File config1f;
	public FileConfiguration config1;

	public static FileConfiguration config;
	private static CraftUkr instance;

	public Tribe TRIBE_EARTH = new Tribe("Земля", 0);
	public Tribe TRIBE_FIRE = new Tribe("Вогонь", 1);
	public Tribe TRIBE_AIR = new Tribe("Повітря", 2);
	public Tribe[] TRIBES = new Tribe[] { TRIBE_EARTH, TRIBE_FIRE, TRIBE_AIR };

	public void onEnable() {
		instance = this;
		config = getConfig();
		initConfigs();
		Bukkit.getPluginManager().registerEvents(this, this);
		getCommand("t").setExecutor(new TribesFunction());
		getCommand("t").setAliases(Arrays.asList("tribes"));
		getCommand("f").setExecutor(new FactionsFunction());
		getCommand("spawn").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
				((Player) sender).chat("/t spawn");
				return true;
			}
		});
	}

	public void onDisable() {
		saveConfig();
		saveConfigs();
	}

	@EventHandler
	public void motd(ServerListPingEvent e) {
		String[] motds = new String[] { "Провідний Український сервер.", "Купа модифікацій!" };
		e.setMotd(_c + "" + _l + "Крафт" + _e + "UA " + _7 + "- " + _e + choose(motds));
	}

	public void initConfigs() {
		config0f = new File(getDataFolder(), "first_time_config.yml");
		config0 = YamlConfiguration.loadConfiguration(config0f);
		config0.options().copyDefaults(true);
		config1f = new File(getDataFolder(), "tribes.yml");
		config1 = YamlConfiguration.loadConfiguration(config1f);
		config0.options().copyDefaults(true);
		saveConfigs();
	}

	public void saveConfigs() {
		try {
			config0.save(config0f);
			config1.save(config1f);
			saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (TribesFunction.getTribeFromPlayer(event.getPlayer().getUniqueId()) == TRIBE_AIR) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		if (TribesFunction.getTribeFromPlayer(event.getPlayer().getUniqueId()) == TRIBE_AIR) {
			Player player = event.getPlayer();
			if (player.isFlying()) {
				return;
			}
			if ((player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
					&& (!hovering(player.getLocation()))) {
				Vector dir = player.getLocation().getDirection().multiply(0.25);
				player.setVelocity(new Vector(dir.getX(), player.getVelocity().getY() * 0.5, dir.getZ()));
			}
		}
	}

	private boolean hovering(Location loc) {
		Block lower = loc.getBlock().getRelative(BlockFace.DOWN);
		if ((lower.getRelative(BlockFace.NORTH).getType() != Material.AIR)
				|| (lower.getRelative(BlockFace.SOUTH).getType() != Material.AIR)
				|| (lower.getRelative(BlockFace.EAST).getType() != Material.AIR)
				|| (lower.getRelative(BlockFace.WEST).getType() != Material.AIR)
				|| (lower.getRelative(BlockFace.DOWN).getType() != Material.AIR)) {
			return true;
		}
		return false;
	}

	public static String serializeItem(ItemStack is) {
		String str = is.getType().getId() + ":" + is.getAmount() + ":" + is.getDurability();
		return str;
	}

	public static ItemStack deserializeItem(String str) {
		String[] args = str.split(":");
		return new ItemStack(Material.getMaterial(Integer.parseInt(args[0])), Integer.parseInt(args[1]),
				Short.parseShort(args[2]));
	}

	public static Location deserialize(String s) {
		String[] splittedString = s.split("\\|");
		return new Location(Bukkit.getWorld(splittedString[3]), Integer.parseInt(splittedString[0]),
				Integer.parseInt(splittedString[1]), Integer.parseInt(splittedString[2]));
	}

	@SafeVarargs
	public static <T> T choose(T... t) {
		return t[new Random().nextInt(t.length)];
	}

}
