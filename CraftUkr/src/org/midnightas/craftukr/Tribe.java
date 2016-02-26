package org.midnightas.craftukr;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.World;

public class Tribe {
	
	public String name;
	public final List<Chunk> chunks = new ArrayList<Chunk>();
	public final List<UUID> members = new ArrayList<UUID>();
	public UUID owner;
	public final List<UUID> moderators = new ArrayList<UUID>();
	public final int id;
	
	public Tribe(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	public void claim(Chunk c) {
		chunks.add(c);
	}
	
	public void claim(World w, int x, int z) {
		chunks.add(w.getChunkAt(x, z));
	}
	
}
