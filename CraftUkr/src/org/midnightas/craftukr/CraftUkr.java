package org.midnightas.craftukr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
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
				if (id <= 3 && id > 0) {
					if (getTribeFromPlayer(((Player) sender).getUniqueId()) == null) {
						if (instance.warnedPlayers.contains(((Player) sender).getUniqueId())) {
							instance.TRIBES[id - 1].members.add(((Player) sender).getUniqueId());
							sendMessageWithHeader(sender,
									new String[] { _c + "Вітаємо вас до племена:", _b + instance.TRIBES[id - 1].name });
							instance.warnedPlayers.remove(((Player) sender).getUniqueId());
							if (!instance.config0.contains(((Player) sender).getUniqueId() + "")
									|| !instance.config0.getStringList(((Player) sender).getUniqueId() + "")
											.contains(instance.TRIBES[id - 1].id)) {
								List<String> firstTimePlayers = instance.config0
										.getStringList(((Player) sender).getUniqueId() + "");
								firstTimePlayers.add(instance.TRIBES[id - 1].id + "");
								instance.config0.set(((Player) sender).getUniqueId() + "", firstTimePlayers);
								for (String s : instance.config1.getStringList(instance.TRIBES[id - 1].id + ".kit"))
									((Player) sender).getInventory().addItem(deserializeItem(s));
								instance.giveEffects((Player) sender);
							}
						} else {
							sendMessageWithHeader(sender,
									new String[] {
											"Ви точно хочете брати участь у племені " + instance.TRIBES[id - 1].name
													+ "?",
											"Ви не зможете залишити цього племена!",
											"Напишітьте команду щерез якщо ви впевнині.",
											"Цей поклик показує перемоги кожної плем'ї:", "https://github.com/lvivtotoro/craftua/" });
							instance.warnedPlayers.add(((Player) sender).getUniqueId());
						}
					} else {
						sendMessageWithHeader(sender, new String[] { _c + "Ви вже є у племені!" });
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
	public List<UUID> warnedPlayers = new ArrayList<UUID>();
	public List<UUID> earthCooldown = new ArrayList<UUID>();

	public BukkitRunnable damageFireUnderwater;
	public BukkitRunnable damageAirInCaves;

	public void onEnable() {
		instance = this;
		config = getConfig();
		initConfigs();
		Bukkit.getPluginManager().registerEvents(this, this);
		damageFireUnderwater = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (TribesFunction.getTribeFromPlayer(p.getUniqueId()) == TRIBE_FIRE) {
						if (p.getLocation().getBlock().getType() == Material.WATER) {
							p.damage(1);
						}
					}
				}
			}
		};
		damageFireUnderwater.runTaskTimer(this, 20, 20);
		damageAirInCaves = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (TribesFunction.getTribeFromPlayer(p.getUniqueId()) == TRIBE_AIR) {
						if (p.getLocation().getBlockY() < 16) {
							p.damage(5);
						}
					}
				}
			}
		};
		damageAirInCaves.runTaskTimer(this, 0, 20 * 60);
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
		giveEffects(event.getPlayer());
	}

	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		giveEffects(event.getPlayer());
	}

	public void giveEffects(Player player) {
		Tribe tribe = TribesFunction.getTribeFromPlayer(player.getUniqueId());
		if (tribe == TRIBE_AIR) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
		} else if (tribe == TRIBE_FIRE) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
		} else if (tribe == TRIBE_EARTH) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		}
	}

	@EventHandler
	public void onRightClickBlock(PlayerInteractEvent event) {
		Tribe tribe = TribesFunction.getTribeFromPlayer(event.getPlayer().getUniqueId());
		if (tribe == TRIBE_FIRE) {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (!event.getPlayer().isSneaking()) {
					Material material = event.getClickedBlock().getType();
					if (material == Material.IRON_ORE) {
						event.getClickedBlock().setType(Material.AIR);
						event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(),
								new ItemStack(Material.IRON_INGOT, 1));
					} else if (material == Material.GOLD_ORE) {
						event.getClickedBlock().setType(Material.AIR);
						event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(),
								new ItemStack(Material.GOLD_INGOT, 1));
					}
				} else {
					if (!day(event.getPlayer().getWorld()))
						event.getClickedBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
				}
			}
		} else if (tribe == TRIBE_EARTH) {
			if (event.getPlayer().isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK
					&& !earthCooldown.contains(event.getPlayer().getUniqueId())) {
				final Player player = event.getPlayer();
				double startX = player.getLocation().getBlockX() - 1;
				double startY = player.getLocation().getBlockY() - 1;
				double startZ = player.getLocation().getBlockZ() - 1;
				World world = player.getWorld();
				Material material = Material.BEDROCK;
				for (int x = 0; x < 3; x++) {
					for (int z = 0; z < 3; z++) {
						for (int y = 0; y <= 3; y++) {
							Location loc = new Location(world, startX + x, startY + y, startZ + z);
							if (y != 3 && y != 0) {
								if ((x >= 0 && z == 0) || (x >= 0 && z == 2) || (x == 0 && z >= 0)
										|| (x == 2 && z >= 0))
									loc.getBlock().setType(material);
							} else {
								loc.getBlock().setType(material);
							}
						}
					}
				}
				earthCooldown.add(player.getUniqueId());
				new BukkitRunnable() {
					public void run() {
						double startX = player.getLocation().getBlockX() - 1;
						double startY = player.getLocation().getBlockY() - 1;
						double startZ = player.getLocation().getBlockZ() - 1;
						World world = player.getWorld();
						Material material = Material.AIR;
						for (int x = 0; x < 3; x++) {
							for (int z = 0; z < 3; z++) {
								for (int y = 0; y <= 3; y++) {
									Location loc = new Location(world, startX + x, startY + y, startZ + z);
									if (y != 3 && y != 0) {
										if ((x >= 0 && z == 0) || (x >= 0 && z == 2) || (x == 0 && z >= 0)
												|| (x == 2 && z >= 0))
											loc.getBlock().setType(material);
									} else {
										loc.getBlock().setType(material);
									}
								}
							}
						}
						new BukkitRunnable() {
							public void run() {
								earthCooldown.remove(player.getUniqueId());
							}
						}.runTaskLater(CraftUkr.this, 24000);
					}
				}.runTaskLater(this, 20 * 60l);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		Tribe tribe = TribesFunction.getTribeFromPlayer(event.getPlayer().getUniqueId());
		if (tribe == TRIBE_AIR) {
			Player player = event.getPlayer();
			if (player.isFlying()) {
				return;
			}
			if ((player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
					&& (!hovering(player.getLocation()))) {
				Vector dir = player.getLocation().getDirection().multiply(0.25);
				player.setVelocity(new Vector(dir.getX(), player.getVelocity().getY() * 0.5, dir.getZ()));
			}
		} else if (tribe == TRIBE_FIRE) {
			if (event.getPlayer().getLocation().getBlock().getType() == Material.WATER) {
				event.getPlayer().damage(1);
			}
		}
	}

	@EventHandler
	public void onFallDamageEvent(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Tribe tribe = TribesFunction.getTribeFromPlayer(player.getUniqueId());
			if (tribe == TRIBE_AIR) {
				if (event.getCause() == DamageCause.FALL) {
					event.setDamage(event.getDamage() * 0.1);
				}
			} else if (tribe == TRIBE_EARTH) {
				if (event.getCause() == DamageCause.FALL) {
					event.setDamage(event.getDamage() * 1.5);
				}
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

	public boolean day(World world) {
		long time = world.getTime();
		return time < 12300 || time > 23850;
	}

	@SafeVarargs
	public static <T> T choose(T... t) {
		return t[new Random().nextInt(t.length)];
	}

}
